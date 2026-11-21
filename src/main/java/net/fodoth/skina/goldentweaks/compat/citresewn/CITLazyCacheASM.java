package net.fodoth.skina.goldentweaks.compat.citresewn;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * CIT Resewn 1.2.2 eagerly allocates four caches and four bound functions per stack.
 * Move each complete initializer into its getter so unused stacks allocate neither.
 * Reflection cannot alter constructor code; an injection after construction would
 * still allocate everything. Preserve CIT's original factories and cache behaviour.
 */
public final class CITLazyCacheASM {

    private CITLazyCacheASM() {
    }

    public static void patch(ClassNode node) {
        for (String type : List.of("Item", "Enchantment", "Armor", "Elytra")) {
            String fieldName = "citresewn$cacheType" + type;
            MethodNode getter = node.methods.stream()
                    .filter(method -> method.name.equals("citresewn$getCacheType" + type))
                    .findFirst().orElse(null);
            if (getter == null || getter.instructions.size() == 0) {
                throw new IllegalStateException("Missing CIT cache getter: " + fieldName);
            }

            List<InsnList> initializers = new ArrayList<>();
            for (MethodNode method : node.methods) {
                if (!method.name.equals("<init>")) {
                    continue;
                }
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (!(insn instanceof FieldInsnNode field) || insn.getOpcode() != Opcodes.PUTFIELD
                            || !field.owner.equals(node.name) || !field.name.equals(fieldName)) {
                        continue;
                    }
                    // Exact upstream initializer: ALOAD 0, NEW, DUP, GETSTATIC,
                    // DUP, requireNonNull, POP, invokedynamic, <init>, PUTFIELD.
                    AbstractInsnNode start = insn;
                    int[] expected = {Opcodes.ALOAD, Opcodes.NEW, Opcodes.DUP, Opcodes.GETSTATIC,
                            Opcodes.DUP, Opcodes.INVOKESTATIC, Opcodes.POP,
                            Opcodes.INVOKEDYNAMIC, Opcodes.INVOKESPECIAL, Opcodes.PUTFIELD};
                    List<AbstractInsnNode> code = new ArrayList<>();
                    code.add(start);
                    while (code.size() < expected.length && start.getPrevious() != null) {
                        start = start.getPrevious();
                        if (start.getOpcode() >= 0) {
                            code.add(0, start);
                        }
                    }
                    for (int i = 0; i < expected.length; i++) {
                        if (code.size() != expected.length || code.get(i).getOpcode() != expected[i]) {
                            throw new IllegalStateException("Unsupported CIT initializer: " + fieldName);
                        }
                    }
                    if (((VarInsnNode) code.get(0)).var != 0) {
                        throw new IllegalStateException("Unsupported CIT cache receiver");
                    }
                    InsnList copy = new InsnList();
                    for (AbstractInsnNode original : code) {
                        copy.add(original.clone(new HashMap<>()));
                    }
                    initializers.add(copy);
                    for (AbstractInsnNode original : code) {
                        method.instructions.remove(original);
                    }
                }
            }
            if (initializers.isEmpty()) {
                throw new IllegalStateException("Missing CIT cache initializer: " + fieldName);
            }
            FieldInsnNode read = null;
            for (AbstractInsnNode insn : getter.instructions) {
                if (insn instanceof FieldInsnNode field && field.name.equals(fieldName)
                        && field.getOpcode() == Opcodes.GETFIELD) {
                    read = field;
                    break;
                }
            }
            if (read == null) {
                throw new IllegalStateException("Unsupported CIT cache getter: " + fieldName);
            }
            node.fields.stream().filter(field -> field.name.equals(fieldName))
                    .forEach(field -> field.access &= ~Opcodes.ACC_FINAL);
            InsnList lazy = new InsnList();
            LabelNode ready = new LabelNode();
            lazy.add(new VarInsnNode(Opcodes.ALOAD, 0));
            lazy.add(new FieldInsnNode(Opcodes.GETFIELD, node.name, fieldName, read.desc));
            lazy.add(new JumpInsnNode(Opcodes.IFNONNULL, ready));
            lazy.add(initializers.get(0));
            lazy.add(ready);
            lazy.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            getter.instructions.insert(lazy);
            // Per-stack cache identity must also be stable if consumers race.
            getter.access |= Opcodes.ACC_SYNCHRONIZED;
            getter.maxStack = Math.max(getter.maxStack, 5);
        }
    }
}
