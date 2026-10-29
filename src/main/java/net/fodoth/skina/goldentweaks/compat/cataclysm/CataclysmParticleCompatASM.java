package net.fodoth.skina.goldentweaks.compat.cataclysm;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * traveloptics 4.4.0.1 的依赖声明是 {@code cataclysm [2.60.,)}，它按灾变旧版 API 把粒子直接编译成
 * 对 {@code ModParticle.SANDSTORM} 的 {@code getstatic}（例如 Summon Desert Dwellers、Sword of the
 * Ancients、Summoned Wadjet/Koboleton/Kobolediator 出沙暴时）。灾变 3.33 把该粒子连同字段一起移除
 * （{@code SandStormParticle} 类与贴图还在，但不再注册），于是 traveloptics 执行到那里时字段解析失败，
 * 抛 {@code NoSuchFieldError} 把服务器 tick 打断。
 *
 * <p>这里在灾变 {@code ModParticle} 的 {@code <clinit>} 末尾补回该字段，并别名到一个仍然注册的粒子，
 * 让 traveloptics 的 {@code ModParticle.SANDSTORM.get()} 重新可用（视觉退化为别名粒子，而不是崩溃）。
 * 别名字段在补丁时按存在于 {@code ModParticle} 的字段动态挑选，灾变后续再移除某个粒子也不会再次崩。</p>
 */
public final class CataclysmParticleCompatASM {

    private static final String TARGET =
            "com.github.L_Ender.cataclysm.init.ModParticle";
    private static final String TARGET_INTERNAL =
            "com/github/L_Ender/cataclysm/init/ModParticle";
    private static final String HOLDER_DESC =
            "Lnet/neoforged/neoforge/registries/DeferredHolder;";
    private static final String REMOVED_FIELD = "SANDSTORM";

    /**
     * 别名优先级：沙暴视觉最接近 {@code DUST_BLAST}，其余为同类可用的 SimpleParticleType 兜底。
     */
    private static final String[] ALIAS_CANDIDATES = {
            "DUST_BLAST",
            "STORM",
            "PHANTOM_EMITTER",
            "SPARK",
            "SOUL_LAVA",
            "CURSED_FLAME"
    };

    private CataclysmParticleCompatASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {

        if (!TARGET.equals(targetClassName)) {
            return;
        }

        if (hasField(node, REMOVED_FIELD)) {
            return;
        }

        String alias = pickAlias(node);

        if (alias == null) {
            GoldenTweaks.LOGGER.warn(
                    "[GT] cataclysm ModParticle: no particle available to alias {} to; "
                            + "traveloptics will still hit NoSuchFieldError",
                    REMOVED_FIELD
            );
            return;
        }

        MethodNode clinit = findClinit(node);
        AbstractInsnNode lastReturn = clinit == null ? null : lastReturn(clinit);

        if (lastReturn == null) {
            GoldenTweaks.LOGGER.warn(
                    "[GT] cataclysm ModParticle: no static initialiser to seed {}; left untouched",
                    REMOVED_FIELD
            );
            return;
        }

        node.fields.add(new FieldNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                REMOVED_FIELD,
                HOLDER_DESC,
                null,
                null
        ));

        InsnList injected = new InsnList();
        injected.add(new FieldInsnNode(Opcodes.GETSTATIC, TARGET_INTERNAL, alias, HOLDER_DESC));
        injected.add(new FieldInsnNode(Opcodes.PUTSTATIC, TARGET_INTERNAL, REMOVED_FIELD, HOLDER_DESC));
        clinit.instructions.insertBefore(lastReturn, injected);

        GoldenTweaks.LOGGER.warn(
                "[GT] cataclysm ModParticle: restored {} (aliased to {}) for traveloptics",
                REMOVED_FIELD,
                alias
        );
    }

    private static boolean hasField(ClassNode node, String name) {

        for (FieldNode field : node.fields) {
            if (name.equals(field.name)) {
                return true;
            }
        }

        return false;
    }

    private static String pickAlias(ClassNode node) {

        for (String candidate : ALIAS_CANDIDATES) {
            for (FieldNode field : node.fields) {
                if (candidate.equals(field.name) && HOLDER_DESC.equals(field.desc)) {
                    return candidate;
                }
            }
        }

        for (FieldNode field : node.fields) {
            if (HOLDER_DESC.equals(field.desc) && !REMOVED_FIELD.equals(field.name)) {
                return field.name;
            }
        }

        return null;
    }

    private static MethodNode findClinit(ClassNode node) {

        for (MethodNode method : node.methods) {
            if ("<clinit>".equals(method.name)) {
                return method;
            }
        }

        return null;
    }

    private static AbstractInsnNode lastReturn(MethodNode clinit) {

        AbstractInsnNode lastReturn = null;

        for (AbstractInsnNode insn = clinit.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == Opcodes.RETURN) {
                lastReturn = insn;
            }
        }

        return lastReturn;
    }
}
