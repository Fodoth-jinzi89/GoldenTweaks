package net.fodoth.skina.goldentweaks.compat.flavorimmerseddailyargo;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * flavor_immersed_daily_argo 的两个物品把「打开界面」写在了同一个方法里：
 *
 * <pre>{@code
 * public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
 *     if (level.isClientSide) {
 *         Minecraft.getInstance().setScreen(new FishingHandbookScreen());   // ← 客户端专属
 *     }
 *     return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
 * }
 * }</pre>
 *
 * <p>这段代码在专用服务器上永远不会执行，但只要 {@code ModItems} 注册物品时执行到
 * {@code new FishingHandbookItem(...)}，JVM 加载该类就会去链接 {@code Screen}/{@code Minecraft}，
 * 被 RuntimeDistCleaner 拦下：</p>
 *
 * <pre>
 * java.lang.NoClassDefFoundError: net/minecraft/client/gui/screens/Screen
 *     at com.flavor_immersed_daily_argo.all.ModItems.lambda$static$166(ModItems.java:1050)
 *     at net.neoforged.neoforge.registries.DeferredRegister.lambda$register$0(DeferredRegister.java:215)
 *     ... at net.neoforged.neoforge.registries.RegisterEvent.register(RegisterEvent.java:48)
 * </pre>
 *
 * <p>（latest-10.29-4.log）它在注册阶段抛出，会连累后面所有模组的物品注册，于是 ae2 / myotus /
 * aeronautics 再读自己的物品就全是 "Trying to access unbound value"——所以先修这里。</p>
 *
 * <p>处理方式：专用服务器上只删掉 {@code if (level.isClientSide) { ... }} 的<b>块</b>，判空条件本身
 * （{@code aload}/{@code getfield isClientSide}/{@code ifeq}，只引用 Level，出入栈配平）留着不动。
 * 块本身也出入栈配平、内部没有跳转（只有行号/标签），删掉后服务端等价于「什么都不做」，客户端字节码不受影响。</p>
 *
 * <p>注意：javac 会在块首插一个标注语句起点的 {@link LabelNode}（配 {@link LineNumberNode}），
 * 所以「块里出现 LabelNode 就放弃」的写法会让这个补丁永远不生效——真正要拒绝的是被块外跳转（或
 * try/catch）当靶子的标签，以及 {@link FrameNode}。</p>
 */
public final class ClientOnlyItemASM {

    private static final Set<String> TARGETS = Set.of(
            "com.flavor_immersed_daily_argo.item.FishingHandbookItem",
            "com.flavor_immersed_daily_argo.item.RangerClipboardItem"
    );

    private ClientOnlyItemASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {

        if (FMLEnvironment.dist.isClient() || !TARGETS.contains(targetClassName)) {
            return;
        }

        List<String> stripped = new ArrayList<>();

        for (MethodNode method : node.methods) {
            if (stripClientSideGuardBlock(method)) {
                stripped.add(method.name + method.desc);
            }
        }

        if (stripped.isEmpty()) {
            GoldenTweaks.LOGGER.warn(
                    "[GT] {}: no isClientSide-guarded block found in flavor_immersed_daily_argo;"
                            + " server dist guard not applied",
                    targetClassName
            );
        } else {
            GoldenTweaks.LOGGER.warn(
                    "[GT] Dropped isClientSide-guarded client block(s) in flavor_immersed_daily_argo {}"
                            + " (dedicated server): {}",
                    targetClassName, String.join(", ", stripped)
            );
        }
    }

    private static boolean stripClientSideGuardBlock(MethodNode method) {

        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {

            if (!(insn instanceof JumpInsnNode jump) || jump.getOpcode() != Opcodes.IFEQ) {
                continue;
            }

            if (!(jump.getPrevious() instanceof FieldInsnNode field)
                    || !"isClientSide".equals(field.name)
                    || !"Z".equals(field.desc)) {
                continue;
            }

            LabelNode end = jump.label;
            List<AbstractInsnNode> block = new ArrayList<>();

            for (AbstractInsnNode node = jump.getNext(); node != null && node != end; node = node.getNext()) {
                block.add(node);
            }

            if (block.isEmpty()) {
                continue;
            }

            Set<LabelNode> inner = labelsOf(block);

            if (isTargetedFromOutside(method, block, inner) || touchesTryCatch(method, inner)) {
                continue;
            }

            if (!isStackNeutral(block)) {
                continue;
            }

            block.forEach(method.instructions::remove);

            method.localVariables = null;
            method.visibleLocalVariableAnnotations = null;
            method.invisibleLocalVariableAnnotations = null;

            return true;
        }

        return false;
    }

    private static Set<LabelNode> labelsOf(List<AbstractInsnNode> block) {

        Set<LabelNode> labels = new HashSet<>();

        for (AbstractInsnNode node : block) {
            if (node instanceof LabelNode label) {
                labels.add(label);
            }
        }

        return labels;
    }

    /** 块外的跳转有没有跳进块里（块外的 try/catch 一并算）。 */
    private static boolean isTargetedFromOutside(MethodNode method, List<AbstractInsnNode> block, Set<LabelNode> inner) {

        if (inner.isEmpty()) {
            return false;
        }

        Set<AbstractInsnNode> members = new HashSet<>(block);

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {

            if (node instanceof JumpInsnNode jump && !members.contains(jump) && inner.contains(jump.label)) {
                return true;
            }
        }

        return false;
    }

    private static boolean touchesTryCatch(MethodNode method, Set<LabelNode> inner) {

        for (TryCatchBlockNode tryCatch : method.tryCatchBlocks) {

            if (inner.contains(tryCatch.start)
                    || inner.contains(tryCatch.end)
                    || inner.contains(tryCatch.handler)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isStackNeutral(List<AbstractInsnNode> block) {

        int delta = 0;

        for (AbstractInsnNode node : block) {

            if (node instanceof LabelNode || node instanceof LineNumberNode) {
                continue;
            }

            if (node instanceof FrameNode || node instanceof JumpInsnNode) {
                return false;
            }

            int step = stackDelta(node);

            if (step == Integer.MIN_VALUE) {
                return false;
            }

            delta += step;
        }

        return delta == 0;
    }

    private static int stackDelta(AbstractInsnNode node) {

        if (node instanceof MethodInsnNode call) {

            int delta = call.getOpcode() == Opcodes.INVOKESTATIC ? 0 : -1;

            for (Type argument : Type.getArgumentTypes(call.desc)) {
                delta -= argument.getSize();
            }

            return delta + Type.getReturnType(call.desc).getSize();
        }

        if (node instanceof FieldInsnNode field) {

            int delta = field.getOpcode() == Opcodes.GETSTATIC ? 0 : -1;

            return delta + Type.getType(field.desc).getSize();
        }

        if (node instanceof TypeInsnNode type) {
            return type.getOpcode() == Opcodes.NEW ? 1 : Integer.MIN_VALUE;
        }

        if (node instanceof InsnNode insn) {

            return switch (insn.getOpcode()) {
                case Opcodes.NOP -> 0;
                case Opcodes.ACONST_NULL, Opcodes.DUP -> 1;
                case Opcodes.POP -> -1;
                default -> Integer.MIN_VALUE;
            };
        }

        return Integer.MIN_VALUE;
    }
}
