package net.fodoth.skina.goldentweaks.compat.acceleratedrendering;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public final class GTModelCacheASM {
    private GTModelCacheASM() {
    }

    // These initializers are merged by another mixin, after injection points are collected.
    public static void patch(ClassNode node) {
        int sites = 0;
        for (var method : node.methods) {
            if (!method.name.equals("<init>")) continue;
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (!(insn instanceof MethodInsnNode call) || call.getOpcode() != Opcodes.INVOKESPECIAL
                        || !call.owner.equals("it/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap")
                        || !call.name.equals("<init>") || !call.desc.equals("()V")) continue;
                AbstractInsnNode next = call.getNext();
                while (next != null && next.getOpcode() < 0) next = next.getNext();
                if (!(next instanceof FieldInsnNode field) || field.getOpcode() != Opcodes.PUTFIELD
                        || !field.owner.equals(node.name) || !field.desc.equals("Ljava/util/Map;")
                        || !(field.name.equals("meshes") || field.name.equals("merges"))) continue;
                method.instructions.insertBefore(call, new LdcInsnNode(GoldenTweaksClientConfig.MODEL_MESH_CACHE_INITIAL_CAPACITY));
                call.desc = "(I)V";
                method.maxStack++;
                sites++;
            }
        }
        // Zero sites also permits AcceleratedRendering's item feature to be disabled.
        if (sites != 0 && sites != 2) throw new IllegalStateException("Unexpected mesh initializers: " + sites);
    }
}
