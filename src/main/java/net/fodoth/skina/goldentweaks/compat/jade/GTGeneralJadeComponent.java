package net.fodoth.skina.goldentweaks.compat.jade;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTEnergyFormat;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectNames;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortBlockEntity;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortGeneralStorage;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortKind;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * The general assembly's Jade panel: what one part currently holds.
 *
 * <p>Read from the block entity on the client rather than shipped over the network. Jade's own data
 * channel would mean a second copy of the same figures for every hover — the assembly already
 * pushes its contents to nearby clients through the block-entity update, which is what its window
 * reads. A provider that asked the server again would be the third copy, and could disagree with
 * the other two.</p>
 *
 * <p>Each kind is capped at {@link #MAX_PER_KIND}, with the remainder counted on one grey line. An
 * assembly holds every kind at once and the panel would otherwise run off the screen: the question
 * the tooltip answers is "is there room for this craft", and four of each settles it.</p>
 */
public final class GTGeneralJadeComponent implements IBlockComponentProvider {

    /** How many entries of one kind are drawn before the rest are summarised. */
    private static final int MAX_PER_KIND = 4;

    /** The machine panel's bar colours, so the two read as the same widget. */
    private static final int BAR_TRACK = 0xFF555555;
    private static final int ENERGY_FILL = 0xFFB03030;
    /**
     * Solid white.
     *
     * <p>Not {@code 0xFFFFFFFF}: Jade stores {@code -1} as its "unset" sentinel and swaps it for the
     * theme's mid grey, so pure white came out grey.</p>
     */
    private static final int BAR_TEXT = 0xFFFFFFFE;

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "general_assembly");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof GTPortBlockEntity port)
                || port.kind() != GTPortKind.GENERAL) {
            return;
        }
        GTPortGeneralStorage assembly = port.generalStorage();
        if (assembly == null) {
            return;
        }

        // First: the charge is a separate store and the one figure that moves on its own, so it
        // goes above the contents rather than among them.
        IElement energy = energyBar(assembly);
        if (energy != null) {
            tooltip.add(energy);
        }

        // Then the two ceilings the window also quotes, so the panel answers "how full is this
        // part" before it answers "with what".
        tooltip.add(Component.translatable("gui.goldentweaks.jade.general.capacity",
                        GTEnergyFormat.format(assembly.bytesUsed()),
                        GTEnergyFormat.format(assembly.byteBudget()),
                        assembly.typesUsed(),
                        assembly.typeBudget())
                .withStyle(ChatFormatting.GREEN));

        appendKind(tooltip, "gui.goldentweaks.port.kind.item", itemRows(assembly));
        appendKind(tooltip, "gui.goldentweaks.port.kind.fluid", fluidRows(assembly));

        if (ModList.get().isLoaded("mekanism")) {
            appendKind(tooltip, "gui.goldentweaks.port.kind.chemical", chemicalRows(assembly));
        }
        if (ModList.get().isLoaded("thaumcraft")) {
            appendKind(tooltip, "gui.goldentweaks.port.kind.essentia", essentiaRows(assembly));
        }
    }

    /**
     * The charge, drawn the way the machine's panel draws it.
     *
     * <p>Its own store rather than a share of the assembly's bytes: an energy hatch is sized from
     * the energy table, and a charge sitting in one is not something a craft competes with the
     * items for. Drawn only when the part holds any, since a bar reading zero is the same news as
     * no bar at all.</p>
     */
    private static @Nullable IElement energyBar(GTPortGeneralStorage assembly) {
        int stored = assembly.energy().getEnergyStored();
        int capacity = assembly.energy().getMaxEnergyStored();
        if (stored <= 0 || capacity <= 0) {
            return null;
        }
        float fraction = Math.max(0.0F, Math.min(1.0F, stored / (float) capacity));
        Component label = Component.translatable("gui.goldentweaks.jade.energy",
                GTEnergyFormat.formatWithUnit(stored, "FE"),
                GTEnergyFormat.formatWithUnit(capacity, "FE"));
        ProgressStyle style = IElementHelper.get().progressStyle()
                .color(BAR_TRACK, ENERGY_FILL)
                .textColor(BAR_TEXT)
                .fitContentX(false);
        return IElementHelper.get().progress(fraction, label, style,
                BoxStyle.getNestedBox(), false);
    }

    /**
     * One kind's entries, capped, under a heading.
     *
     * <p>An empty kind is skipped entirely rather than shown as a heading with nothing under it:
     * most assemblies only ever hold two of the four, and four headings with two gaps reads as a
     * broken panel.</p>
     */
    private static void appendKind(ITooltip tooltip, String headingKey, List<List<IElement>> rows) {
        if (rows.isEmpty()) {
            return;
        }
        tooltip.add(Component.translatable(headingKey).withStyle(ChatFormatting.GRAY));
        int shown = Math.min(rows.size(), MAX_PER_KIND);
        for (int index = 0; index < shown; index++) {
            tooltip.add(rows.get(index));
        }
        if (rows.size() > shown) {
            tooltip.add(Component.translatable("gui.goldentweaks.jade.more", rows.size() - shown)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static List<List<IElement>> itemRows(GTPortGeneralStorage assembly) {
        List<List<IElement>> rows = new ArrayList<>();
        var handler = assembly.items();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            rows.add(line(
                    IElementHelper.get().item(stack.copyWithCount(1)),
                    stack.getHoverName(), stack.getCount(), ""));
        }
        return rows;
    }

    private static List<List<IElement>> fluidRows(GTPortGeneralStorage assembly) {
        List<List<IElement>> rows = new ArrayList<>();
        var handler = assembly.fluids();
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            var stack = handler.getFluidInTank(tank);
            if (stack.isEmpty()) {
                continue;
            }
            rows.add(line(
                    IElementHelper.get().fluid(JadeFluidObject.of(stack.getFluid(), 1L)),
                    stack.getHoverName(), stack.getAmount(), " mB"));
        }
        return rows;
    }

    /**
     * Chemicals, drawn through EMI.
     *
     * <p>Jade knows items and fluids; a Mekanism chemical has no element of its own here. The
     * assembly's window already draws them through EMI, so the panel reuses that rather than
     * approximating a sprite.</p>
     */
    private static List<List<IElement>> chemicalRows(GTPortGeneralStorage assembly) {
        List<List<IElement>> rows = new ArrayList<>();
        for (var stack : assembly.chemicalContents()) {
            rows.add(line(GTEmiJadeElement.of(stack, 1.0F),
                    chemicalName(stack.id()), stack.amount(), ""));
        }
        return rows;
    }

    /** Essentia, drawn the same way. */
    private static List<List<IElement>> essentiaRows(GTPortGeneralStorage assembly) {
        List<List<IElement>> rows = new ArrayList<>();
        for (var stack : assembly.essentiaContents()) {
            rows.add(line(GTEmiJadeElement.of(stack, 1.0F),
                    aspectName(stack.id()), stack.amount(), ""));
        }
        return rows;
    }

    /**
     * A chemical's own name.
     *
     * <p>The holder carries the translation key, so asking it is what makes the line read
     * "氢氧化钠" instead of "sodium_hydroxide". Guarded: Mekanism is not always on the classpath,
     * and an id this pack does not ship resolves to nothing at all — the raw path is then the
     * honest fallback, still unambiguous.</p>
     */
    private static Component chemicalName(ResourceLocation id) {
        try {
            var holder = mekanism.api.MekanismAPI.CHEMICAL_REGISTRY.getHolder(id).orElse(null);
            if (holder != null) {
                return holder.value().getTextComponent();
            }
        } catch (NoClassDefFoundError | RuntimeException absent) {
            // Mekanism absent, or the id names a chemical this pack does not ship.
        }
        return Component.literal(id.getPath());
    }

    /**
     * An aspect's own name.
     *
     * <p>Thaumcraft spells these, through {@link GTAspectNames}; the raw tag is the fallback, still
     * unambiguous, just untranslated.</p>
     */
    private static Component aspectName(ResourceLocation id) {
        try {
            var aspect = GTAspectNames.of(id.getPath());
            return aspect == null ? Component.literal(id.getPath()) : aspect;
        } catch (NoClassDefFoundError absent) {
            return Component.literal(id.getPath());
        }
    }

    /**
     * One row: an icon if there is one, then the name and its amount.
     *
     * <p>The amount is written out rather than drawn as a stack badge — Jade's badge is an int, and
     * a full cell holds far more than that.</p>
     */
    private static List<IElement> line(IElement icon, Component name, long amount, String unit) {
        List<IElement> row = new ArrayList<>(3);
        if (icon != null) {
            row.add(icon);
            row.add(IElementHelper.get().spacer(2, 0));
        }
        row.add(IElementHelper.get().text(Component.literal("")
                .append(name.copy().withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" × " + GTEnergyFormat.formatExpanded(amount) + unit)
                        .withStyle(ChatFormatting.AQUA))));
        return row;
    }
}
