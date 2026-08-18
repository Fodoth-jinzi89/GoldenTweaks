package net.fodoth.skina.goldentweaks.compat.carryon;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class CarryOnAeroCompatASM {

    private static final String TARGET = "tschipp.carryon.common.carry.PickupHandler";

    private CarryOnAeroCompatASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {
        if (!TARGET.equals(targetClassName)) {
            return;
        }

        for (MethodNode method : node.methods) {
            if (!"canCarryGeneral".equals(method.name)
                    || !"(Lnet/minecraft/server/level/ServerPlayer;)Z".equals(method.desc)) {
                continue;
            }

            InsnList anchor = new InsnList();
            anchor.add(new FieldInsnNode(
                    Opcodes.GETSTATIC,
                    "net/minecraft/world/phys/Vec3",
                    "ZERO",
                    "Lnet/minecraft/world/phys/Vec3;"
            ));
            anchor.add(new FieldInsnNode(
                    Opcodes.GETSTATIC,
                    "net/minecraft/world/phys/Vec3",
                    "ZERO",
                    "Lnet/minecraft/world/phys/Vec3;"
            ));
            anchor.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "net/minecraft/world/phys/Vec3",
                    "distanceTo",
                    "(Lnet/minecraft/world/phys/Vec3;)D",
                    false
            ));
            anchor.add(new InsnNode(Opcodes.POP2));
            method.instructions.insert(anchor);

            GoldenTweaks.LOGGER.warn(
                    "[GT] Added Carry On Aeronautics compatibility redirect anchor"
            );
            return;
        }
    }
}
