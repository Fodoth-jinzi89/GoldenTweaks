package net.fodoth.skina.goldentweaks.compat.jade;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortTier;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMenu;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTEnergyFormat;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStacks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactorStatus;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectNames;
import net.fodoth.skina.goldentweaks.util.GTDurationFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.MutableComponent;
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
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.Tag;

/**
 * Draws the kettle's tooltip panel.
 *
 * <p>Every line is conditional on the machine actually having something to say: the channel and the
 * progress bar exist only while a craft runs, and the energy bar only when the machine has energy
 * ports at all. That keeps an idle, unpowered machine down to a single line instead of a wall of
 * zeroes.</p>
 *
 * <p>The bar is Jade's own progress element with a grey track and a yellow fill, so it inherits the
 * mod's tooltip styling rather than being painted from scratch.</p>
 */
public final class GTReactorJadeComponent implements IBlockComponentProvider {

    /** Grey track, white text; the fill colour is chosen per bar. */
    private static final int BAR_TRACK = 0xFF555555;
    private static final int PROGRESS_FILL = 0xFFD8B33A;
    private static final int ENERGY_FILL = 0xFFB03030;
    /**
     * Solid white.
     *
     * <p>Not {@code 0xFFFFFFFF}: Jade stores {@code -1} as its "unset" sentinel and swaps it for the
     * theme's normal colour, which is a mid grey — that is why pure white came out grey. One step
     * below {@code -1} is indistinguishable white and is taken at face value.</p>
     */
    private static final int BAR_TEXT = 0xFFFFFFFE;

    /** How many products are listed before the rest are summarised. */
    private static final int MAX_PRODUCTS = 6;


    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "chemical_reactor_body");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag root = accessor.getServerData().getCompound(GTReactorJadeProvider.ROOT);
        if (root.isEmpty()) {
            return;
        }

        GTReactorStatus status = statusOf(root);
        boolean running = status == GTReactorStatus.PROCESSING;

        // Line one: the machine's own state. A running craft has its progress drawn as a bar below,
        // so the counters are left off this line to avoid saying the same thing twice.
        tooltip.add(statusLine(root, status));

        // Line two: the switches that are on. Only lit ones are named, so the line reads as a
        // summary rather than a checklist — "隔离" alone, or "锁定 隔离" once lock is added.
        Component switches = switchSummary(root.getInt(GTReactorJadeProvider.FLAGS));
        if (switches != null) {
            tooltip.add(switches);
        }

        if (running && root.getBoolean(GTReactorJadeProvider.HAS_CHANNEL)) {
            tooltip.add(Component.translatable("gui.goldentweaks.jade.channel",
                    root.getInt(GTReactorJadeProvider.CHANNEL)).withStyle(ChatFormatting.GRAY));
        }

        // Between the channel and the power figure: which class of hatch the machine runs on, which
        // is what decides both the input rate below and how far it may overclock.
        int energyTier = root.getInt(GTReactorJadeProvider.ENERGY_TIER);
        if (energyTier > 0) {
            // The label stays green; only the tier is coloured, so a glance at the hue says which
            // class of hatch the machine is built from without reading the letters.
            GTPortTier tier = GTPortTier.byLevel(energyTier);
            tooltip.add(Component.translatable("gui.goldentweaks.jade.energy_tier.label")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(tier.code())
                            .withStyle(Style.EMPTY.withColor(tier.colour()))));
        }

        // Only while a craft runs: an idle machine draws the hatch rate at most, and stating a power
        // figure for a reaction that is not happening would be a guess. Read as a long: a parallel
        // craft's bill can exceed the int range, and narrowing it would print the wrong figure.
        long power = root.getLong(GTReactorJadeProvider.POWER);
        if (power > 0L) {
            tooltip.add(figureLine("gui.goldentweaks.jade.power", power));
        }

        int rate = root.getInt(GTReactorJadeProvider.RATE);
        if (rate > 0) {
            tooltip.add(figureLine("gui.goldentweaks.jade.max_input", rate));
        }

        // Both bars are built before either is added: when they appear together the wider text
        // sets the width for both, so the pair lines up as one column. On its own a bar falls back
        // to the fixed width instead.
        Component progressLabel = running ? progressLabel(root) : null;
        int capacity = root.getInt(GTReactorJadeProvider.CAPACITY);
        Component energyLabel = capacity > 0 ? energyLabel(root, capacity) : null;

        int width = sharedWidth(progressLabel, energyLabel);
        if (progressLabel != null) {
            tooltip.add(progressBar(root, pad(progressLabel, width)));
        }
        if (energyLabel != null) {
            tooltip.add(energyBar(root, capacity, pad(energyLabel, width)));
        }

        if (running) {
            appendProducts(tooltip, root);
        }
    }

    /**
     * A label and a figure, in two colours: the label green, the number and its unit aqua.
     *
     * <p>Split rather than formatted into one string so the number is the part that stands out —
     * these are the two figures a player opens the panel for. The unit goes inside the translatable
     * value, exactly as the energy bar's own label does it, so "XFE/t" stays in the translation
     * rather than being spelled out here.</p>
     */
    private static Component figureLine(String labelKey, long value) {
        return Component.translatable(labelKey).withStyle(ChatFormatting.GREEN)
                .append(Component.literal(GTEnergyFormat.formatWithUnit(value, "FE/t"))
                        .withStyle(ChatFormatting.AQUA));
    }

    private static GTReactorStatus statusOf(CompoundTag root) {
        try {
            return GTReactorStatus.valueOf(root.getString(GTReactorJadeProvider.STATUS));
        } catch (IllegalArgumentException unknown) {
            // A status this build does not know about reads as the neutral case rather than
            // throwing inside the tooltip render.
            return GTReactorStatus.OTHER;
        }
    }

    private static Component statusLine(CompoundTag root, GTReactorStatus status) {
        String key = switch (status) {
            case IDLE -> "gui.goldentweaks.chemical_reactor.status.idle";
            case PROCESSING -> "gui.goldentweaks.chemical_reactor.status.processing";
            case OFF -> "gui.goldentweaks.chemical_reactor.status.off";
            case NO_ENERGY -> "gui.goldentweaks.chemical_reactor.status.no_energy";
            case BROKEN_STRUCTURE -> "gui.goldentweaks.chemical_reactor.status.broken_structure";
            case STARTING, OTHER -> "gui.goldentweaks.chemical_reactor.status.other";
            case INSUFFICIENT_ENERGY_HATCH ->
                    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch";
        };
        // The processing wording expects two durations; the bar below carries them instead.
        if (status == GTReactorStatus.PROCESSING) {
            return Component.translatable("gui.goldentweaks.jade.status.processing")
                    .withStyle(ChatFormatting.GREEN);
        }
        if (status == GTReactorStatus.INSUFFICIENT_ENERGY_HATCH) {
            // The wording carries a figure, so it is filled in rather than passed through empty —
            // translating the bare key would print the raw "%s" placeholder.
            return Component.translatable(key,
                            GTEnergyFormat.formatExpanded(
                                    root.getInt(GTReactorJadeProvider.REFUSED_COST)))
                    .withStyle(ChatFormatting.RED);
        }
        return Component.translatable(key).withStyle(switch (status) {
            case IDLE -> ChatFormatting.YELLOW;
            case OFF, NO_ENERGY, BROKEN_STRUCTURE, OTHER, INSUFFICIENT_ENERGY_HATCH ->
                    ChatFormatting.RED;
            default -> ChatFormatting.WHITE;
        });
    }

    /**
     * The switches that are on, as one line, or {@code null} when none are.
     *
     * <p>Only the lit ones are named, in the order the window lays them out, so the line reads as a
     * summary — "隔离" alone, or "锁定 隔离" once lock is added. A machine with everything off gets
     * no line at all rather than an empty one.</p>
     */
    @Nullable
    private static Component switchSummary(int flags) {
        List<Component> lit = new ArrayList<>();
        if ((flags & GTChemicalReactorMenu.FLAG_LOCK_RECIPE) != 0) {
            lit.add(Component.translatable("gui.goldentweaks.chemical_reactor.lock.short"));
        }
        if ((flags & GTChemicalReactorMenu.FLAG_VOID_OUTPUT) != 0) {
            lit.add(Component.translatable("gui.goldentweaks.chemical_reactor.void.short"));
        }
        if ((flags & GTChemicalReactorMenu.FLAG_BATCH_MODE) != 0) {
            lit.add(Component.translatable("gui.goldentweaks.chemical_reactor.batch.short"));
        }
        if ((flags & GTChemicalReactorMenu.FLAG_ISOLATE_INPUT) != 0) {
            lit.add(Component.translatable("gui.goldentweaks.chemical_reactor.isolate.short"));
        }
        if (lit.isEmpty()) {
            return null;
        }

        MutableComponent joined = Component.empty();
        for (int index = 0; index < lit.size(); index++) {
            if (index > 0) {
                joined.append(" ");
            }
            joined.append(lit.get(index));
        }
        return joined.withStyle(ChatFormatting.YELLOW);
    }

    /**
     * The progress bar, with the elapsed and total time printed over it.
     *
     * <p>The text is the same pair the window shows after "运行中：", so the two read identically.</p>
     */
    private static Component progressLabel(CompoundTag root) {
        int progress = root.getInt(GTReactorJadeProvider.PROGRESS);
        int length = root.getInt(GTReactorJadeProvider.LENGTH);
        return Component.translatable("gui.goldentweaks.jade.progress",
                GTDurationFormat.format(progress), GTDurationFormat.format(length));
    }

    private static IElement progressBar(CompoundTag root, Component label) {
        int progress = root.getInt(GTReactorJadeProvider.PROGRESS);
        int length = root.getInt(GTReactorJadeProvider.LENGTH);
        float fraction = length <= 0 ? 0.0F
                : Math.max(0.0F, Math.min(1.0F, progress / (float) length));
        return bar(fraction, label, PROGRESS_FILL);
    }

    /** The energy bar, using the same wording as the window's own readout. */
    private static Component energyLabel(CompoundTag root, int capacity) {
        int stored = root.getInt(GTReactorJadeProvider.ENERGY);
        return Component.translatable("gui.goldentweaks.jade.energy",
                GTEnergyFormat.formatWithUnit(stored, "FE"),
                GTEnergyFormat.formatWithUnit(capacity, "FE"));
    }

    private static IElement energyBar(CompoundTag root, int capacity, Component label) {
        int stored = root.getInt(GTReactorJadeProvider.ENERGY);
        float fraction = Math.max(0.0F, Math.min(1.0F, stored / (float) capacity));
        return bar(fraction, label, ENERGY_FILL);
    }

    /** Builds one bar with the shared appearance. */
    private static IElement bar(float fraction, Component label, int fill) {
        ProgressStyle style = IElementHelper.get().progressStyle()
                .color(BAR_TRACK, fill)
                .textColor(BAR_TEXT)
                .fitContentX(false);
        return IElementHelper.get().progress(fraction, label, style,
                BoxStyle.getNestedBox(), false);
    }

    /** Width used when only one bar is shown and there is nothing to align it with. */
    private static final int SOLO_BAR_TEXT_WIDTH = 96;

    /**
     * The width both bars are padded to.
     *
     * <p>With both on screen the wider label sets it, so the pair reads as one column. With only one
     * there is nothing to align to, so a fixed width keeps the bar from resizing as the numbers
     * tick.</p>
     */
    private static int sharedWidth(@Nullable Component progress, @Nullable Component energy) {
        if (progress == null || energy == null) {
            return SOLO_BAR_TEXT_WIDTH;
        }
        return Math.max(font().width(progress), font().width(energy));
    }

    /**
     * Pads a label out to a given pixel width with spaces.
     *
     * <p>Jade sizes a bar from its text width, so two different labels would otherwise give two
     * different bar lengths. A space is four pixels wide, so the padding lands on a four-pixel
     * boundary; the leftover one to three pixels sit inside the bar's own text margin and are not
     * visible.</p>
     */
    private static Component pad(Component label, int width) {
        int current = font().width(label);
        if (current >= width) {
            return label;
        }
        int spaces = (width - current) / 4;
        if (spaces <= 0) {
            return label;
        }
        return Component.empty().append(label).append(" ".repeat(spaces));
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    /**
     * Lists the pending products, three at a time.
     *
     * <p>Each entry is drawn as an ordinary item row, which is what a player already knows how to
     * read; no production rate is shown because the panel is about this craft, not throughput.</p>
     */
    private static void appendProducts(ITooltip tooltip, CompoundTag root) {
        ListTag products = root.getList(GTReactorJadeProvider.PRODUCTS,
                Tag.TAG_COMPOUND);
        if (products.isEmpty()) {
            return;
        }

        tooltip.add(Component.translatable("gui.goldentweaks.jade.expected")
                .withStyle(ChatFormatting.GRAY));

        int shown = 0;
        for (int index = 0; index < products.size() && shown < MAX_PRODUCTS; index++) {
            CompoundTag entry = products.getCompound(index);
            IElement icon = iconOf(entry);
            if (icon == null) {
                // Nothing to draw: an id this client cannot resolve, or a kind that needs EMI when it
                // is not installed. The row is skipped rather than replaced with text, because the
                // panel is read as a set of slots and a lone name among them reads as an error.
                continue;
            }
            // Bare icon plus a written amount, one product per line. Jade's own badge is an int, and
            // a parallel craft can exceed that, so the number is spelled out beside the icon instead
            // of being drawn as a stack count — the same figure the machine's own window prints.
            List<IElement> row = new ArrayList<>(3);
            row.add(icon);
            row.add(IElementHelper.get().spacer(2, 0));
            row.add(IElementHelper.get().text(productLabel(entry)));
            tooltip.add(row);
            shown++;
        }

        int remaining = products.size() - shown;
        if (remaining > 0) {
            tooltip.add(Component.translatable("gui.goldentweaks.jade.more", remaining)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /**
     * The icon for one product entry, or {@code null} when it cannot be drawn.
     *
     * <p>Fluids go through Jade's own fluid element, which draws the same still texture EMI uses
     * for a fluid stack, so a product reads the same in both places.</p>
     */
    @Nullable
    private static IElement iconOf(CompoundTag entry) {
        String kind = entry.getString(GTReactorJadeProvider.PRODUCT_KIND);
        ResourceLocation id = ResourceLocation.tryParse(
                entry.getString(GTReactorJadeProvider.PRODUCT_ID));
        if (id == null) {
            return null;
        }
        if (GTReactorJadeProvider.KIND_FLUID.equals(kind)) {
            var fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(null);
            if (fluid == null) {
                return null;
            }
            // A single unit, so the icon is just a picture: the real amount is written beside it.
            // Jade's own badge is an int and would be wrong past that anyway.
            return IElementHelper.get().fluid(JadeFluidObject.of(fluid, 1L));
        }
        if (GTReactorJadeProvider.KIND_NAMED.equals(kind)) {
            return emiIconOf(entry, id, 1L);
        }
        var item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null) {
            return null;
        }
        // One item, not a stack: a stack count is capped at 99 and drawn over the icon, which would
        // both understate the craft and say the same thing the label already says correctly.
        return IElementHelper.get().item(new ItemStack(item, 1));
    }

    /**
     * One product as a written line, e.g. {@code Coal × 8} or {@code Water 1,000 mB}.
     *
     * <p>The same shapes the machine's own window uses: items count in whole items, fluids and
     * chemicals in millibuckets, and essentia in whole aspect units, so no unit is guessed.</p>
     *
     * <p>The real kind is only consulted for {@link GTReactorJadeProvider#KIND_NAMED} entries. An
     * item entry carries no real-kind field at all, so asking anyway would fall through to the
     * resolver's "assume chemical" default and label a stack of items in millibuckets.</p>
     */
    private static Component productLabel(CompoundTag entry) {
        String kind = entry.getString(GTReactorJadeProvider.PRODUCT_KIND);
        ResourceLocation id = ResourceLocation.tryParse(
                entry.getString(GTReactorJadeProvider.PRODUCT_ID));
        long amount = entry.getLong(GTReactorJadeProvider.PRODUCT_COUNT);
        // Grouped the way the player's language groups numbers, so a large parallel craft stays
        // readable instead of becoming an unbroken run of digits.
        String size = GTEnergyFormat.formatExpanded(amount);

        Component name = nameOf(entry, kind, id);
        if (name == null) {
            name = Component.literal(id == null ? "?" : id.getPath());
        }
        if (GTReactorJadeProvider.KIND_FLUID.equals(kind)) {
            return Component.literal("").append(name).append(" " + size + " mB");
        }
        if (GTReactorJadeProvider.KIND_NAMED.equals(kind)) {
            GTReactionStack.Kind real = realKindOf(entry);
            if (real == GTReactionStack.Kind.CHEMICAL) {
                return Component.literal("").append(name).append(" " + size + " mB");
            }
            // Essentia counts in whole aspect units and an AE key has no unit of its own, so both
            // take the bare figure — matching the machine window's own formatting.
            return Component.literal("").append(name).append(" " + size);
        }
        return Component.literal("").append(name).append(" × " + size);
    }

    /** The display name of one product entry, or {@code null} when it cannot be resolved. */
    @Nullable
    private static Component nameOf(CompoundTag entry, String kind, @Nullable ResourceLocation id) {
        if (id == null) {
            return null;
        }
        if (GTReactorJadeProvider.KIND_FLUID.equals(kind)) {
            var fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(null);
            return fluid == null ? null : fluid.getFluidType().getDescription();
        }
        if (!GTReactorJadeProvider.KIND_NAMED.equals(kind)) {
            var item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            return item == null ? null : new ItemStack(item).getHoverName();
        }
        GTReactionStack.Kind real = realKindOf(entry);
        if (real == GTReactionStack.Kind.CHEMICAL) {
            // Mekanism names a chemical through its holder, which carries the translation key; the
            // registry lookup is guarded because Mekanism is not always on the classpath.
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
        // Essentia: its name is a Thaumcraft string, so the aspect is read when the mod is there and
        // the raw tag is the fallback — still unambiguous, just untranslated.
        String tag = id.getPath();
        try {
            var aspect = GTAspectNames.of(tag);
            return aspect == null ? Component.literal(tag) : aspect;
        } catch (NoClassDefFoundError absent) {
            return Component.literal(tag);
        }
    }

    @Nullable
    private static IElement emiIconOf(CompoundTag entry, ResourceLocation id, long amount) {
        if (!ModList.get().isLoaded("emi")) {
            return null;
        }
        GTReactionStack.Kind kind = realKindOf(entry);
        GTReactionStack stack = new GTReactionStack(kind, id, amount, Optional.empty());
        var level = Minecraft.getInstance().level;
        var ingredient = GTReactionStacks.toEmiIngredient(
                stack, level == null ? null : level.registryAccess());
        return ingredient == null ? null : new GTEmiJadeElement(ingredient, 1.0F);
    }

    /** The kind the server recorded for an entry with no Jade-native element. */
    private static GTReactionStack.Kind realKindOf(CompoundTag entry) {
        String name = entry.getString(GTReactorJadeProvider.PRODUCT_REAL_KIND);
        for (GTReactionStack.Kind kind : GTReactionStack.Kind.values()) {
            if (kind.name().equals(name)) {
                return kind;
            }
        }
        // Written by an older build that sent only the display name; a chemical is the better guess
        // because it is what most recipes of this kind produce.
        return GTReactionStack.Kind.CHEMICAL;
    }
}
