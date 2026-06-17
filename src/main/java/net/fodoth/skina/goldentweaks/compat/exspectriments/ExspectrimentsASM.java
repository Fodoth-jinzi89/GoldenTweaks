package net.fodoth.skina.goldentweaks.compat.exspectriments;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class ExspectrimentsASM {

    private static final String TARGET =
            "io.github.chromonym.exspectriments.ExspBlocks";

    private ExspectrimentsASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {

        if (!TARGET.equals(targetClassName)) {
            return;
        }

        GoldenTweaks.LOGGER.warn("[GT] Rebuilding ExspBlocks");

        for (MethodNode method : node.methods) {

            if (!"<clinit>".equals(method.name)) {
                continue;
            }

            rebuildClinit(method);
        }
    }

    private static void rebuildClinit(MethodNode method) {

        method.instructions.clear();
        method.tryCatchBlocks.clear();

        InsnList insn = new InsnList();

        /*
         * PRINTER_BLOCK =
         * register(new PrinterBlock(Properties.of()), "printer");
         */
        insn.add(new TypeInsnNode(
                Opcodes.NEW,
                "io/github/chromonym/exspectriments/blocks/PrinterBlock"));
        insn.add(new InsnNode(Opcodes.DUP));

        insn.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "net/minecraft/world/level/block/state/BlockBehaviour$Properties",
                "of",
                "()Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;",
                false));

        insn.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "io/github/chromonym/exspectriments/blocks/PrinterBlock",
                "<init>",
                "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V",
                false));

        insn.add(new LdcInsnNode("printer"));

        insn.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "io/github/chromonym/exspectriments/ExspBlocks",
                "register",
                "(Lnet/minecraft/world/level/block/Block;Ljava/lang/String;)Lnet/minecraft/world/level/block/Block;",
                false));

        insn.add(new FieldInsnNode(
                Opcodes.PUTSTATIC,
                "io/github/chromonym/exspectriments/ExspBlocks",
                "PRINTER_BLOCK",
                "Lnet/minecraft/world/level/block/Block;"));

        /*
         * PIGMENT_EXTRACTOR =
         * register(new PigmentExtractorBlock(Properties.of()), "pigment_extractor");
         */
        insn.add(new TypeInsnNode(
                Opcodes.NEW,
                "io/github/chromonym/exspectriments/blocks/PigmentExtractorBlock"));
        insn.add(new InsnNode(Opcodes.DUP));

        insn.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "net/minecraft/world/level/block/state/BlockBehaviour$Properties",
                "of",
                "()Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;",
                false));

        insn.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "io/github/chromonym/exspectriments/blocks/PigmentExtractorBlock",
                "<init>",
                "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V",
                false));

        insn.add(new LdcInsnNode("pigment_extractor"));

        insn.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "io/github/chromonym/exspectriments/ExspBlocks",
                "register",
                "(Lnet/minecraft/world/level/block/Block;Ljava/lang/String;)Lnet/minecraft/world/level/block/Block;",
                false));

        insn.add(new FieldInsnNode(
                Opcodes.PUTSTATIC,
                "io/github/chromonym/exspectriments/ExspBlocks",
                "PIGMENT_EXTRACTOR",
                "Lnet/minecraft/world/level/block/Block;"));

        /*
         * LIQUID_* = Blocks.WATER
         */
        putWater(insn, "LIQUID_TOPAZ");
        putWater(insn, "LIQUID_AMETHYST");
        putWater(insn, "LIQUID_CITRINE");
        putWater(insn, "LIQUID_ONYX");
        putWater(insn, "LIQUID_MOONSTONE");

        insn.add(new InsnNode(Opcodes.RETURN));

        method.instructions.add(insn);

        method.maxStack = 8;
        method.maxLocals = 0;

        GoldenTweaks.LOGGER.warn("[GT] ExspBlocks rebuilt");
    }

    private static void putWater(InsnList insn, String fieldName) {

        insn.add(new FieldInsnNode(
                Opcodes.GETSTATIC,
                "net/minecraft/world/level/block/Blocks",
                "WATER",
                "Lnet/minecraft/world/level/block/Block;"));

        insn.add(new FieldInsnNode(
                Opcodes.PUTSTATIC,
                "io/github/chromonym/exspectriments/ExspBlocks",
                fieldName,
                "Lnet/minecraft/world/level/block/Block;"));
    }
}