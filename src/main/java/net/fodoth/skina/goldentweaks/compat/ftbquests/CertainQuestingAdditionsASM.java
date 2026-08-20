package net.fodoth.skina.goldentweaks.compat.ftbquests;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class CertainQuestingAdditionsASM {

    private static final String TARGET =
            "dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton$3";

    private CertainQuestingAdditionsASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {
        if (!TARGET.equals(targetClassName)
                || node.fields.stream().anyMatch(field -> "val$name".equals(field.name))) {
            return;
        }

        node.fields.add(new FieldNode(
                Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
                "val$name",
                "Ljava/lang/String;",
                null,
                null
        ));

        MethodNode getName = new MethodNode(
                Opcodes.ACC_PUBLIC,
                "getName",
                "()Lnet/minecraft/network/chat/Component;",
                null,
                null
        );
        getName.instructions.add(new LdcInsnNode("ftbquests.chapter.image"));
        getName.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "net/minecraft/network/chat/Component",
                "translatable",
                "(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                true
        ));
        getName.instructions.add(new InsnNode(Opcodes.ARETURN));
        node.methods.add(getName);

        GoldenTweaks.LOGGER.warn(
                "[GT] Added Certain Questing Additions compatibility anchors for FTB Quests 2101.1.32"
        );
    }
}
