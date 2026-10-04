package net.fodoth.skina.goldentweaks.compat.appliedkjs;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ArrayList;
import java.util.List;

/**
 * applied_kjs 的 {@code AppliedKJSServerState} 用普通 {@code HashMap}/{@code HashSet} 保存 AE2 主机索引
 * （{@code hostsByDimChunk} -> (区块 long -> {@code BlockPos} 集合) 以及 {@code submittedLinks}），
 * 但这些结构是跨线程访问的：
 *
 * <ul>
 *   <li>{@code ChunkEvent.Load}/{@code Unload} 经 {@code onChunkAE2HostsLoaded}/{@code onChunkUnloaded} 写入。
 *       C2ME 等异步区块加载下，这些事件发生在工作线程而非服务器 tick 线程。</li>
 *   <li>{@code collectSnapshots}（由 {@code runScan} -> {@code tick} 调用）在服务器 tick 线程遍历同一个
 *       内层 Map 的 {@code values()} 以及其中的 {@code BlockPos} 集合。</li>
 * </ul>
 *
 * 并发写入与遍历叠加会抛 {@code ConcurrentModificationException}，导致服务器 tick 崩溃。
 * 这里把该类中所有「无参」{@code HashMap}/{@code HashSet} 分配替换为 {@code ConcurrentHashMap} /
 * {@code ConcurrentHashMap.newKeySet()}，使遍历变为弱一致、不再抛 CME；带参的 {@code HashSet(Collection)}
 * 拷贝（仅用于事件 diff，非共享状态）与用于保持顺序的 {@code LinkedHashMap} 保持不变。
 */
public final class AppliedKjsASM {

    private static final String TARGET =
            "com.rollylindenshnizzer.appliedkjs.AppliedKJSServerState";
    private static final String HASH_MAP = "java/util/HashMap";
    private static final String HASH_SET = "java/util/HashSet";
    private static final String CONCURRENT_HASH_MAP = "java/util/concurrent/ConcurrentHashMap";
    private static final String KEY_SET_VIEW = "Ljava/util/concurrent/ConcurrentHashMap$KeySetView;";

    private AppliedKjsASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {

        if (!TARGET.equals(targetClassName)) {
            return;
        }

        int replaced = 0;

        for (MethodNode method : node.methods) {
            replaced += rewriteAllocations(method);
        }

        if (replaced > 0) {
            GoldenTweaks.LOGGER.warn(
                    "[GT] Replaced {} applied_kjs shared collection(s) with concurrent implementations",
                    replaced
            );
        }
    }

    private static int rewriteAllocations(MethodNode method) {

        List<TypeInsnNode> setNews = new ArrayList<>();
        List<AbstractInsnNode> setDups = new ArrayList<>();
        List<MethodInsnNode> setCtors = new ArrayList<>();

        int replaced = 0;

        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {

            if (!(insn instanceof TypeInsnNode type) || type.getOpcode() != Opcodes.NEW) {
                continue;
            }

            AbstractInsnNode dup = type.getNext();

            if (dup == null || dup.getOpcode() != Opcodes.DUP) {
                continue;
            }

            if (!(dup.getNext() instanceof MethodInsnNode ctor)
                    || ctor.getOpcode() != Opcodes.INVOKESPECIAL
                    || !"()V".equals(ctor.desc)) {
                continue;
            }

            if (HASH_MAP.equals(type.desc) && HASH_MAP.equals(ctor.owner)) {
                type.desc = CONCURRENT_HASH_MAP;
                ctor.owner = CONCURRENT_HASH_MAP;
                replaced++;
            } else if (HASH_SET.equals(type.desc) && HASH_SET.equals(ctor.owner)) {
                setNews.add(type);
                setDups.add(dup);
                setCtors.add(ctor);
                replaced++;
            }
        }

        // Remove the NEW/DUP pair first; the ctor node is reused as the static factory call.
        for (int i = 0; i < setCtors.size(); i++) {
            MethodInsnNode ctor = setCtors.get(i);
            ctor.setOpcode(Opcodes.INVOKESTATIC);
            ctor.owner = CONCURRENT_HASH_MAP;
            ctor.name = "newKeySet";
            ctor.desc = "()" + KEY_SET_VIEW;
            ctor.itf = false;

            method.instructions.remove(setNews.get(i));
            method.instructions.remove(setDups.get(i));
        }

        return replaced;
    }
}
