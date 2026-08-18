package net.fodoth.skina.goldentweaks.compat.ftbquests;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class FTBQuestsLangSplitterASM {

    private static final String TARGET =
            "dev.ftb.mods.ftbquests.quest.translation.TranslationManager";
    private static final String PERMISSIONS_HELPER =
            "dev/ftb/mods/ftbquests/integration/PermissionsHelper";
    private static final String OLD_DESCRIPTOR =
            "(Lnet/minecraft/server/level/ServerPlayer;Z)Z";
    private static final String NEW_DESCRIPTOR =
            "(Lnet/minecraft/world/entity/player/Player;)Z";

    private FTBQuestsLangSplitterASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {
        if (!TARGET.equals(targetClassName)) {
            return;
        }

        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call)
                        || call.getOpcode() != Opcodes.INVOKESTATIC
                        || !PERMISSIONS_HELPER.equals(call.owner)
                        || !"hasEditorPermission".equals(call.name)
                        || !OLD_DESCRIPTOR.equals(call.desc)) {
                    continue;
                }

                method.instructions.insertBefore(call, new InsnNode(Opcodes.POP));
                call.desc = NEW_DESCRIPTOR;
                GoldenTweaks.LOGGER.warn(
                        "[GT] Remapped FTB Quests Lang Splitter editor permission call"
                );
            }
        }
    }
}
