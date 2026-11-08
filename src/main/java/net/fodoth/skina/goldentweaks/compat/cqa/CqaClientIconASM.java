package net.fodoth.skina.goldentweaks.compat.cqa;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.ClientDistRefs;
import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Certain Questing Additions 的 {@code IconMixin} 没有做 dist 隔离：它在 {@code Icon.getIcon} 的 HEAD
 * 挂了一个「实体图标」回调，而这个回调无条件调用
 * {@code ru.hollowhorizon.additions.questing.client.EntityIcon}。后者的 {@code draw}/{@code render}/
 * {@code drawEntity} 方法体里全是 {@code net.minecraft.client.*}（{@code GuiGraphics}、
 * {@code MultiBufferSource}、{@code EntityRenderDispatcher}…）。
 *
 * <p>专用服务器上 RuntimeDistCleaner 不提供这些类，于是第一次
 * {@code Icons.<clinit>() → Icon.getIcon("missing")} 就会在链接 {@code EntityIcon} 时抛出
 * {@code NoClassDefFoundError: net/minecraft/client/renderer/MultiBufferSource}，把
 * ftbquests / ftbchunks / extraquests / ftbquest_slot_rewards 一起打挂：</p>
 *
 * <pre>
 * java.lang.NoClassDefFoundError: net/minecraft/client/renderer/MultiBufferSource
 *     at dev.ftb.mods.ftblibrary.icon.Icon.cqa$getEntityIconOrFallback(Icon.java:530)
 *     at dev.ftb.mods.ftblibrary.icon.Icon.handler$fee000$certain_questing_additions$cqa$getEntityIcon(Icon.java:518)
 *     ... at dev.ftb.mods.ftbquests.quest.QuestObjectBase.&lt;clinit&gt;(QuestObjectBase.java:47)
 * </pre>
 *
 * <p>（latest-10.29-2.log）</p>
 *
 * <p>图标实体本身（{@code isEntityIconString} / {@code fromIconString} / NBT 往返）不碰客户端，所以这里
 * 只把「专用服务器上根本跑不到」的客户端代码清空：服务端永远不画图标，但实体图标的解析、保存、同步
 * 保持原样。清空后这些类不再引用被剥离的类，重算栈帧时也不会再去读 {@code GuiGraphics} 的层级。</p>
 *
 * <p>静态初始化链也要一起处理：{@code EntityIcon.<clinit>} 会读
 * {@code EntityIconLighting.FLAT_LIGHT}，而 {@code EntityIconLighting.<clinit>} 里是
 * {@code LightTexture.pack(...)}，所以 {@code EntityIconLighting} 同样要清（latest-10.29-3.log）。它清空后
 * {@code FLAT_LIGHT}/{@code FULL_BRIGHT_LIGHT} 保持默认值——反正只有已被清空的渲染代码会读它们。</p>
 */
public final class CqaClientIconASM {

    private static final Set<String> TARGETS = Set.of(
            "ru.hollowhorizon.additions.questing.client.EntityIcon",
            "ru.hollowhorizon.additions.questing.client.EntityIconLighting",
            "ru.hollowhorizon.additions.questing.client.InvalidEntityIcon"
    );

    private CqaClientIconASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {

        if (FMLEnvironment.dist.isClient() || !TARGETS.contains(targetClassName)) {
            return;
        }

        List<String> neutralized = new ArrayList<>();

        for (MethodNode method : node.methods) {
            if (referencesStrippedType(method)) {
                neutralize(method);
                neutralized.add(method.name);
            }
        }

        if (neutralized.isEmpty()) {
            GoldenTweaks.LOGGER.warn(
                    "[GT] {}: no client-only method found in certain_questing_additions;"
                            + " server dist guard not applied",
                    targetClassName
            );
        } else {
            GoldenTweaks.LOGGER.warn(
                    "[GT] Neutralized {} client-only method(s) in certain_questing_additions {}"
                            + " (dedicated server): {}",
                    neutralized.size(), targetClassName, String.join(", ", neutralized)
            );
        }
    }

    private static boolean referencesStrippedType(MethodNode method) {

        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {

            if (insn instanceof TypeInsnNode type) {
                if (ClientDistRefs.isStripped(type.desc)) {
                    return true;
                }
            } else if (insn instanceof FieldInsnNode field) {
                if (ClientDistRefs.isStripped(field.owner) || ClientDistRefs.descriptorHasStripped(field.desc)) {
                    return true;
                }
            } else if (insn instanceof MethodInsnNode call) {
                if (ClientDistRefs.isStripped(call.owner) || ClientDistRefs.descriptorHasStripped(call.desc)) {
                    return true;
                }
            } else if (insn instanceof MultiANewArrayInsnNode array) {
                if (ClientDistRefs.descriptorHasStripped(array.desc)) {
                    return true;
                }
            } else if (insn instanceof LdcInsnNode ldc) {
                if (constantReferencesStripped(ldc.cst)) {
                    return true;
                }
            } else if (insn instanceof InvokeDynamicInsnNode indy) {
                if (constantReferencesStripped(indy.bsm)) {
                    return true;
                }
                for (Object arg : indy.bsmArgs) {
                    if (constantReferencesStripped(arg)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean constantReferencesStripped(Object constant) {

        if (constant instanceof Type type) {
            return ClientDistRefs.descriptorHasStripped(type.getDescriptor());
        }

        if (constant instanceof Handle handle) {
            return ClientDistRefs.isStripped(handle.getOwner())
                    || ClientDistRefs.descriptorHasStripped(handle.getDesc());
        }

        if (constant instanceof ConstantDynamic dynamic) {
            if (ClientDistRefs.descriptorHasStripped(dynamic.getDescriptor())) {
                return true;
            }
            for (int i = 0; i < dynamic.getBootstrapMethodArgumentCount(); i++) {
                if (constantReferencesStripped(dynamic.getBootstrapMethodArgument(i))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void neutralize(MethodNode method) {

        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.localVariables = null;
        method.visibleLocalVariableAnnotations = null;
        method.invisibleLocalVariableAnnotations = null;

        Type returnType = Type.getReturnType(method.desc);

        switch (returnType.getSort()) {
            case Type.VOID -> method.instructions.add(new InsnNode(Opcodes.RETURN));
            case Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> {
                method.instructions.add(new InsnNode(Opcodes.ICONST_0));
                method.instructions.add(new InsnNode(Opcodes.IRETURN));
            }
            case Type.LONG -> {
                method.instructions.add(new InsnNode(Opcodes.LCONST_0));
                method.instructions.add(new InsnNode(Opcodes.LRETURN));
            }
            case Type.FLOAT -> {
                method.instructions.add(new InsnNode(Opcodes.FCONST_0));
                method.instructions.add(new InsnNode(Opcodes.FRETURN));
            }
            case Type.DOUBLE -> {
                method.instructions.add(new InsnNode(Opcodes.DCONST_0));
                method.instructions.add(new InsnNode(Opcodes.DRETURN));
            }
            default -> {
                method.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
                method.instructions.add(new InsnNode(Opcodes.ARETURN));
            }
        }

        method.maxStack = 2;
        method.maxLocals = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
    }
}
