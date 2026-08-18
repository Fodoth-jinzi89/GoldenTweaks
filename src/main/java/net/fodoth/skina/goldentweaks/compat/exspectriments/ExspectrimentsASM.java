package net.fodoth.skina.goldentweaks.compat.exspectriments;

import de.dafuqs.spectrum.api.ink.color.InkColor;
import de.dafuqs.spectrum.api.ink.storage.InkStorage;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;

import java.util.Set;

public final class ExspectrimentsASM {

    private static final String TARGET =
            "io.github.chromonym.exspectriments.ExspBlocks";
    private static final String OLD_INK_API = "de/dafuqs/spectrum/api/energy/";
    private static final String NEW_INK_API = "de/dafuqs/spectrum/api/ink/";
    private static final String PRINTER_BLOCK_ENTITY =
            "io/github/chromonym/exspectriments/entities/PrinterBlockEntity";
    private static final Set<String> INK_API_TARGETS = Set.of(
            "io.github.chromonym.exspectriments.entities.PrinterBlockEntity",
            "io.github.chromonym.exspectriments.screenhandlers.PrinterScreenHandler"
    );

    private ExspectrimentsASM() {
    }

    public static void patch(String targetClassName, ClassNode node) {

        if (INK_API_TARGETS.contains(targetClassName)) {
            remapInkApi(node);
        }

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

    private static void remapInkApi(ClassNode node) {
        ClassNode remapped = new ClassNode();
        node.accept(new ClassRemapper(remapped, new Remapper() {
            @Override
            public String map(String internalName) {
                if ((OLD_INK_API + "InkStorage").equals(internalName)
                        || (OLD_INK_API + "InkStorageBlockEntity").equals(internalName)
                        || (OLD_INK_API + "InkStorageItem").equals(internalName)) {
                    return NEW_INK_API + "storage/"
                            + internalName.substring(OLD_INK_API.length());
                }
                if (internalName.startsWith(OLD_INK_API)) {
                    return NEW_INK_API + internalName.substring(OLD_INK_API.length());
                }
                return internalName;
            }
        }));

        node.version = remapped.version;
        node.access = remapped.access;
        node.name = remapped.name;
        node.signature = remapped.signature;
        node.superName = remapped.superName;
        node.interfaces = remapped.interfaces;
        node.sourceFile = remapped.sourceFile;
        node.sourceDebug = remapped.sourceDebug;
        node.module = remapped.module;
        node.outerClass = remapped.outerClass;
        node.outerMethod = remapped.outerMethod;
        node.outerMethodDesc = remapped.outerMethodDesc;
        node.visibleAnnotations = remapped.visibleAnnotations;
        node.invisibleAnnotations = remapped.invisibleAnnotations;
        node.visibleTypeAnnotations = remapped.visibleTypeAnnotations;
        node.invisibleTypeAnnotations = remapped.invisibleTypeAnnotations;
        node.attrs = remapped.attrs;
        node.innerClasses = remapped.innerClasses;
        node.nestHostClass = remapped.nestHostClass;
        node.nestMembers = remapped.nestMembers;
        node.permittedSubclasses = remapped.permittedSubclasses;
        node.recordComponents = remapped.recordComponents;
        node.fields = remapped.fields;
        node.methods = remapped.methods;

        for (MethodNode method : node.methods) {
            if ("getEnergyStorage".equals(method.name) && method.desc.startsWith("()")) {
                method.name = "getInkStorage";
            }

            for (AbstractInsnNode instruction : method.instructions) {
                if (!(instruction instanceof MethodInsnNode call)) {
                    continue;
                }

                if (PRINTER_BLOCK_ENTITY.equals(call.owner)
                        && "getEnergyStorage".equals(call.name)
                        && call.desc.startsWith("()")) {
                    call.name = "getInkStorage";
                }

                if ((NEW_INK_API + "storage/InkStorage").equals(call.owner)) {
                    if ("transferInk".equals(call.name)) {
                        call.owner = "net/fodoth/skina/goldentweaks/compat/exspectriments/ExspectrimentsASM";
                        call.name = "transferInk";
                        call.setOpcode(Opcodes.INVOKESTATIC);
                        call.itf = false;
                    } else if (call.getOpcode() == Opcodes.INVOKEINTERFACE) {
                        call.setOpcode(Opcodes.INVOKEVIRTUAL);
                        call.itf = false;
                    }
                }
            }
        }

        GoldenTweaks.LOGGER.warn("[GT] Remapped Exspectriments ink API in {}", node.name);
    }

    public static long transferInk(InkStorage source, InkStorage target) {
        long transferred = 0;

        for (InkColor color : source.acceptedColors()) {
            if (!target.accepts(color)) {
                continue;
            }

            long drained = source.drainEnergy(color, Math.min(
                    source.getEnergy(color),
                    target.getRoom(color)
            ));
            long overflow = target.addEnergy(color, drained);
            if (overflow > 0) {
                source.addEnergy(color, overflow);
            }
            transferred += drained - overflow;
        }

        return transferred;
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
