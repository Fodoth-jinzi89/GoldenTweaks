package net.fodoth.skina.goldentweaks.compat.ldlib2;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
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

/**
 * ldlib2 的 {@code ItemSlot} 混有大量客户端专属逻辑：{@code xeiPhantom()} 只注册 JEI/REI/EMI 的
 * 拖拽/幽灵槽，渲染/悬浮方法直接引用 {@code net.minecraft.client.*}（{@code GuiGraphics}、
 * {@code Font}、{@code Screen}、{@code AbstractContainerScreen}）。专用服务器上这些类都不存在：
 * <ul>
 *   <li>调用 {@code xeiPhantom()}（neoecoae 存储面板在服务端建 UI 时）会链接客户端类 →
 *       {@code NoClassDefFoundError: Renderable} → 服务器 tick 崩溃；</li>
 *   <li>仅凭 Mixin 触碰该类的字节码，ModLauncher 就会用 {@code COMPUTE_FRAMES} 重写整个类，
 *       而重算栈帧需要读取 {@code net.minecraft.client.gui.screens.Screen} 的层级，
 *       被 {@code RuntimeDistCleaner} 拦下（{@code Attempted to load class .../Screen for invalid dist
 *       DEDICATED_SERVER}）→ 整个 {@code ItemSlot} 无法加载 → 所有引用它的模组
 *       （protection_pixel / backpack_pixel / neoecoae）集体 {@code ClassNotFoundException}。</li>
 * </ul>
 *
 * <p>所以这里在专用服务器上把这类客户端专属方法体整体清空（保留 {@code addSlotToTheMenu} 等
 * 服务端仍需的方法），既让 {@code ItemSlot} 重新可被重算栈帧（不再引用客户端类），
 * 也顺带让 {@code xeiPhantom()} 等 XEI 注册变成空操作。</p>
 */
public final class Ldlib2ASM {

    private static final String TARGET =
            "com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot";
    private static final String CLIENT_PREFIX = "net/minecraft/client/";

    private Ldlib2ASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {

        if (FMLEnvironment.dist.isClient() || !TARGET.equals(targetClassName)) {
            return;
        }

        List<String> neutralized = new ArrayList<>();

        for (MethodNode method : node.methods) {
            if (method.name.startsWith("xei") || referencesClientType(method)) {
                neutralized(method);
                neutralized.add(method.name);
            }
        }

        if (neutralized.isEmpty()) {
            GoldenTweaks.LOGGER.warn(
                    "[GT] ItemSlot: no client-only method found in ldlib2; server dist guard not applied"
            );
        } else {
            GoldenTweaks.LOGGER.warn(
                    "[GT] Neutralized {} client-only method(s) in ldlib2 ItemSlot (dedicated server): {}",
                    neutralized.size(), String.join(", ", neutralized)
            );
        }
    }

    private static boolean referencesClientType(MethodNode method) {

        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {

            if (insn instanceof TypeInsnNode type) {
                if (isClient(type.desc)) {
                    return true;
                }
            } else if (insn instanceof FieldInsnNode field) {
                if (isClient(field.owner) || isClient(field.desc)) {
                    return true;
                }
            } else if (insn instanceof MethodInsnNode call) {
                if (isClient(call.owner) || isClient(call.desc)) {
                    return true;
                }
            } else if (insn instanceof MultiANewArrayInsnNode array) {
                if (isClient(array.desc)) {
                    return true;
                }
            } else if (insn instanceof LdcInsnNode ldc) {
                if (constantIsClient(ldc.cst)) {
                    return true;
                }
            } else if (insn instanceof InvokeDynamicInsnNode indy) {
                if (constantIsClient(indy.bsm)) {
                    return true;
                }
                for (Object arg : indy.bsmArgs) {
                    if (constantIsClient(arg)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean isClient(String internalNameOrDescriptor) {
        return internalNameOrDescriptor != null && internalNameOrDescriptor.contains(CLIENT_PREFIX);
    }

    private static boolean constantIsClient(Object constant) {

        if (constant instanceof Type type) {
            return isClient(type.getDescriptor());
        }

        if (constant instanceof Handle handle) {
            return isClient(handle.getOwner()) || isClient(handle.getDesc());
        }

        if (constant instanceof ConstantDynamic dynamic) {
            if (isClient(dynamic.getDescriptor())) {
                return true;
            }
            for (int i = 0; i < dynamic.getBootstrapMethodArgumentCount(); i++) {
                if (constantIsClient(dynamic.getBootstrapMethodArgument(i))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void neutralized(MethodNode method) {

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
