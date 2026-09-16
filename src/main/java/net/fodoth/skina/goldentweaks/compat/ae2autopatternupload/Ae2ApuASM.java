package net.fodoth.skina.goldentweaks.compat.ae2autopatternupload;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.ClientDistRefs;
import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * ae2_auto_pattern_upload 2.0.0 的 S2C 包 {@code ProvidersListS2CPacket} 在自己的
 * {@code lambda$handle$2}（打开客户端 {@code ProviderSelectScreen}，其父类就是 {@code Screen}）里
 * 引用了客户端类型。专用服务器上 mod 注册包时（{@code ModNetwork.registerPayloadHandlers} 第 14 行
 * 取 {@code ProvidersListS2CPacket.TYPE}）首次链接这个类，验证/解析阶段就会去加载 {@code Screen} →
 * {@code RuntimeDistCleaner} 直接抛异常，整包注册事件失败。
 *
 * <p>S2C 的 handle 在服务器上永远不会执行，所以这里（只在 DEDICATED_SERVER）把方法体清空，
 * 让类里彻底没有客户端引用，链接就安全了；注册照常完成，服务器仍然能发这个包。</p>
 */
public final class Ae2ApuASM {

    private static final String TARGET =
            "com.gali.ae2_auto_pattern_upload.network.ProvidersListS2CPacket";

    private Ae2ApuASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {

        if (FMLEnvironment.dist.isClient() || !TARGET.equals(targetClassName)) {
            return;
        }

        for (MethodNode method : node.methods) {

            if (!referencesClientOnly(method)) {
                continue;
            }

            if (Type.getReturnType(method.desc).getSort() != Type.VOID) {
                GoldenTweaks.LOGGER.warn(
                        "[GT] Skipped non-void client-only method {}{} in ProvidersListS2CPacket",
                        method.name,
                        method.desc
                );
                continue;
            }

            method.instructions.clear();
            method.tryCatchBlocks.clear();
            method.localVariables = null;
            method.visibleLocalVariableAnnotations = null;
            method.invisibleLocalVariableAnnotations = null;
            method.instructions.add(new InsnNode(Opcodes.RETURN));

            GoldenTweaks.LOGGER.warn(
                    "[GT] Emptied client-only packet handler {}{} in ProvidersListS2CPacket",
                    method.name,
                    method.desc
            );
        }
    }

    private static boolean referencesClientOnly(MethodNode method) {

        for (AbstractInsnNode insn : method.instructions) {

            if (insn instanceof TypeInsnNode type && ClientDistRefs.isStripped(type.desc)) {
                return true;
            }

            if (insn instanceof FieldInsnNode field && ClientDistRefs.descriptorHasStripped(field.desc)) {
                return true;
            }

            if (insn instanceof MethodInsnNode call
                    && (ClientDistRefs.isStripped(call.owner) || ClientDistRefs.descriptorHasStripped(call.desc))) {
                return true;
            }

            if (insn instanceof InvokeDynamicInsnNode indy && ClientDistRefs.descriptorHasStripped(indy.desc)) {
                return true;
            }
        }

        return false;
    }
}
