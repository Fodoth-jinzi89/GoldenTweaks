package net.fodoth.skina.goldentweaks.compat.linearbearing;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.ClientDistRefs;
import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.List;

/**
 * linearbearing 1.3.5 的 {@code LinearBearing.<init>} 没有 dist 守卫就注册客户端内容：
 * {@code addListener(ClientModHandler::onClientSetup)}、{@code addListener(ClientModHandler::onModelBake)}、
 * 以及 {@code LinearBearingClient.registerClient(modEventBus)}。这些类的字节码里带
 * {@code net.minecraft.client.resources.model.BakedModel}，专用服务器上解析 lambda 方法句柄 / 调用它们时
 * 会去加载 BakedModel → {@code BootstrapMethodError} →
 * “LinearBearing (linearbearing) has failed to load correctly”，服务器直接起不来。
 *
 * <p>invokedynamic 在调用点之前就求值，Mixin 的 {@code @Redirect}/{@code @WrapOperation} 拦不住，
 * 所以这里在 preApply 直接把整条语句删掉（只在 DEDICATED_SERVER 上）。</p>
 */
public final class LinearbearingASM {

    private static final String TARGET = "com.bearing.linearbearing.LinearBearing";
    private static final String EVENT_BUS = "net/neoforged/bus/api/IEventBus";

    private LinearbearingASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {

        if (FMLEnvironment.dist.isClient() || !TARGET.equals(targetClassName)) {
            return;
        }

        for (MethodNode method : node.methods) {

            if (!"<init>".equals(method.name)) {
                continue;
            }

            int removed = stripClientOnlyStatements(method);

            if (removed > 0) {
                GoldenTweaks.LOGGER.warn(
                        "[GT] Dropped {} client-only statement(s) from LinearBearing.<init>",
                        removed
                );
            }
        }
    }

    private static int stripClientOnlyStatements(MethodNode method) {

        List<AbstractInsnNode> doomed = new ArrayList<>();

        for (AbstractInsnNode insn : method.instructions) {

            if (insn instanceof InvokeDynamicInsnNode indy) {

                // modEventBus.addListener(<客户端 lambda>)
                if (isClientOnlyLambda(indy) && isEventBusAddListener(indy.getNext())) {
                    doomed.add(indy.getPrevious());  // aload modEventBus
                    doomed.add(indy);
                    doomed.add(indy.getNext());      // invokeinterface addListener
                }
            } else if (insn instanceof MethodInsnNode call
                    && ClientDistRefs.classReferencesStripped(call.owner)) {

                // LinearBearingClient.registerClient(modEventBus)
                doomed.add(insn);

                if (insn.getPrevious() instanceof VarInsnNode load && load.getOpcode() == Opcodes.ALOAD) {
                    doomed.add(load);
                }
            }
        }

        doomed.forEach(method.instructions::remove);

        return doomed.size();
    }

    private static boolean isClientOnlyLambda(InvokeDynamicInsnNode indy) {

        if (!indy.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")
                || indy.bsmArgs == null || indy.bsmArgs.length < 2
                || !(indy.bsmArgs[1] instanceof Handle impl)) {
            return false;
        }

        return ClientDistRefs.descriptorHasStripped(impl.getDesc())
                || ClientDistRefs.classReferencesStripped(impl.getOwner());
    }

    private static boolean isEventBusAddListener(AbstractInsnNode insn) {

        return insn instanceof MethodInsnNode call
                && EVENT_BUS.equals(call.owner)
                && "addListener".equals(call.name);
    }
}
