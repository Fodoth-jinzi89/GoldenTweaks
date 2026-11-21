package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipe;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMultiblock;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTEnergyFormat;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStacks;
import net.fodoth.skina.goldentweaks.util.GTDurationFormat;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The chemical kettle's page in EMI.
 *
 * <p>Layout follows the reference template: an input grid with its scrollbar, an animated arrow,
 * an output grid with its scrollbar, and the energy and duration figures underneath. Both grids page
 * internally, so a reaction with more than nine inputs still fits the same panel.</p>
 *
 * <p>The page is registered natively rather than through the JEI compatibility layer because the
 * recipe carries stacks AMI's JEI category cannot express (chemicals and AE keys). The JEI plugin
 * stands down when EMI is present, so there is exactly one page per recipe.</p>
 */
public class GTChemicalReactionEmiRecipe implements EmiRecipe {

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, GTChemicalReactionRecipe.ID),
            EmiStack.of(new net.minecraft.world.item.ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get())));

    /** Arrow animation: one cycle per recipe duration, but never shorter than readable. */
    private static final int MIN_ARROW_MILLIS = 500;
    private static final int TICK_MILLIS = 50;

    private final ResourceLocation id;
    private final GTChemicalReactionRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> outputs;

    public GTChemicalReactionEmiRecipe(
            ResourceLocation id, GTChemicalReactionRecipe recipe, @Nullable HolderLookup.Provider registries) {
        this.id = id;
        this.recipe = recipe;
        this.inputs = convert(recipe.inputs(), registries);
        this.outputs = convert(recipe.outputs(), registries);
    }

    private static List<EmiIngredient> convert(
            List<GTReactionStack> stacks, @Nullable HolderLookup.Provider registries) {
        List<EmiIngredient> converted = new ArrayList<>(stacks.size());
        for (GTReactionStack stack : stacks) {
            EmiIngredient ingredient = GTReactionStacks.toEmiIngredient(stack, registries);
            // An unrepresentable entry (mod missing) still occupies its cell, otherwise the grid
            // would silently renumber and the page would disagree with the data pack.
            converted.add(ingredient == null ? EmiStack.EMPTY : ingredient);
        }
        return List.copyOf(converted);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return this.inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> outputs = new ArrayList<>();
        for (EmiIngredient ingredient : this.outputs) {
            outputs.addAll(ingredient.getEmiStacks());
        }
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return GTChemicalReactionLayout.WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return GTChemicalReactionLayout.HEIGHT;
    }

    /**
     * Off, matching what the aspect-source page needed.
     *
     * <p>A page whose inputs are a long list of alternatives would otherwise have EMI build a whole
     * material tree and a per-batch cost tooltip on every hover, which is far more work than the
     * page is worth.</p>
     */
    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Each grid registers its own slots with the holder, so EMI owns them and hover / R/U work.
        widgets.add(new GTChemicalReactionPagedGrid(
                this.inputs, GTChemicalReactionLayout.INPUT_X, GTChemicalReactionLayout.GRID_Y, widgets));
        widgets.add(new GTChemicalReactionPagedGrid(
                this.outputs, GTChemicalReactionLayout.OUTPUT_X, GTChemicalReactionLayout.GRID_Y, widgets));

        widgets.addFillingArrow(
                GTChemicalReactionLayout.ARROW_X, GTChemicalReactionLayout.ARROW_Y, arrowMillis());

        addFigures(widgets);
    }

    /** One arrow cycle per craft, clamped so a very short reaction is still visible as moving. */
    private int arrowMillis() {
        int perCraft = this.recipe.getProcessingTime() * TICK_MILLIS;
        return Math.max(MIN_ARROW_MILLIS, perCraft);
    }

    private void addFigures(WidgetHolder widgets) {
        int y = GTChemicalReactionLayout.TEXT_Y;
        int step = GTChemicalReactionLayout.LINE_HEIGHT;

        widgets.addText(Component.translatable("goldentweaks.chemical_reaction.total_energy",
                        GTEnergyFormat.formatExpandedWithUnit(this.recipe.totalEnergy(), "FE")),
                GTChemicalReactionLayout.TEXT_X, y, 0x404040, false);

        widgets.addText(Component.translatable("goldentweaks.chemical_reaction.energy_per_tick",
                        GTEnergyFormat.formatExpandedWithUnit(this.recipe.getEnergyPerTick(), "FE/t")),
                GTChemicalReactionLayout.TEXT_X, y + step, 0x404040, false);

        widgets.addText(Component.translatable("goldentweaks.chemical_reaction.duration",
                        GTDurationFormat.format(this.recipe.getProcessingTime())),
                GTChemicalReactionLayout.TEXT_X, y + step * 2, 0x404040, false);

        // The mode is printed as the raw integer the data pack carries, wildcard included: the
        // number is what a player types into a port, so translating 0 to a word would hide the value
        // they actually need to match.
        widgets.addText(Component.translatable("goldentweaks.chemical_reaction.mode",
                        String.valueOf(this.recipe.mode())),
                GTChemicalReactionLayout.TEXT_X, y + step * 3, 0x404040, false);
    }
}
