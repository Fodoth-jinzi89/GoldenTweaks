package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipe;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMultiblock;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTEnergyFormat;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStacks;
import net.fodoth.skina.goldentweaks.util.GTDurationFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The chemical kettle's page in JEI.
 *
 * <p>Same geometry as the EMI renderer, so the two viewers show the same layout. JEI brings its own
 * paged scroll grid, which is why this side needs no custom widget: the slots are handed to
 * {@code addScrollGridWidget} in order and JEI lays them out three per row and pages them.</p>
 *
 * <p>Covers every stack kind the recipe can carry: items and fluids directly, Mekanism chemicals
 * through its own JEI ingredient type, and AE keys as AE2's own wrapped display item — the items are
 * what AE2 itself uses to show a generic key inside a normal item slot, so the icon and tooltip are
 * correct without reimplementing them.</p>
 */
public class GTChemicalReactionJeiCategory implements IRecipeCategory<GTChemicalReactionRecipe> {

    private static final String MEKANISM = "mekanism";
    private static final String AE2 = "ae2";

    public static final RecipeType<GTChemicalReactionRecipe> TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, GTChemicalReactionRecipe.ID),
            GTChemicalReactionRecipe.class);

    /** One arrow cycle per craft, but never so short that it reads as static. */
    private static final int MIN_ARROW_TICKS = 10;

    private final IDrawable icon;

    public GTChemicalReactionJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get()));
    }

    @Override
    public RecipeType<GTChemicalReactionRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("emi.category.goldentweaks.chemical_reaction");
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return GTChemicalReactionLayout.WIDTH;
    }

    @Override
    public int getHeight() {
        return GTChemicalReactionLayout.HEIGHT;
    }

    /**
     * Every input and output gets a slot, but they are unpositioned here.
     *
     * <p>{@code createRecipeExtras} is what arranges them: JEI's scroll grid takes the slot list and
     * decides where each page's slots go, so placing them here as well would fight it.</p>
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GTChemicalReactionRecipe recipe, IFocusGroup focuses) {
        for (GTReactionStack stack : recipe.inputs()) {
            addStack(builder.addSlot(RecipeIngredientRole.INPUT), stack);
        }
        for (GTReactionStack stack : recipe.outputs()) {
            addStack(builder.addSlot(RecipeIngredientRole.OUTPUT), stack);
        }
    }

    private static void addStack(IRecipeSlotBuilder slot, GTReactionStack stack) {
        // No per-slot frame: each grid is drawn as one large box, matching the EMI page. With an
        // arbitrary number of stacks, individual frames read as a wall of boxes.
        switch (stack.kind()) {
            case ITEM -> {
                ItemStack item = stack.toItemStack();
                if (!item.isEmpty()) {
                    slot.addItemStack(item);
                }
            }
            case FLUID -> slot.addFluidStack(
                    stack.fluid() == null ? net.minecraft.world.level.material.Fluids.EMPTY : stack.fluid(),
                    stack.amount());
            case CHEMICAL -> {
                if (!ModList.get().isLoaded(MEKANISM)) {
                    return;
                }
                var chemical = GTReactionStacks.toChemicalStack(stack);
                IIngredientType<mekanism.api.chemical.ChemicalStack> type = chemicalType();
                if (chemical != null && !chemical.isEmpty() && type != null) {
                    slot.addIngredient(type, chemical);
                }
            }
            case AE -> {
                if (!ModList.get().isLoaded(AE2)) {
                    return;
                }
                var generic = GTReactionStacks.toGenericStack(stack, registries());
                if (generic != null) {
                    // AE2's own wrapped display item is how it shows a generic key in an item slot.
                    slot.addItemStack(appeng.api.stacks.GenericStack.wrapInItemStack(generic));
                }
            }
        }
    }

    /**
     * Mekanism's JEI chemical type, fetched reflectively.
     *
     * <p>The constant lives on a client-only Mekanism class; naming it directly would make this
     * category fail to load on a client without Mekanism even though the guard above already
     * skipped the call.</p>
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private static IIngredientType<mekanism.api.chemical.ChemicalStack> chemicalType() {
        try {
            return (IIngredientType<mekanism.api.chemical.ChemicalStack>) Class
                    .forName("mekanism.client.recipe_viewer.jei.MekanismJEI")
                    .getField("TYPE_CHEMICAL")
                    .get(null);
        } catch (ReflectiveOperationException | LinkageError exception) {
            GoldenTweaks.LOGGER.warn("[GT] Mekanism JEI chemical type unavailable", exception);
            return null;
        }
    }

    @Nullable
    private static net.minecraft.core.HolderLookup.Provider registries() {
        var level = net.minecraft.client.Minecraft.getInstance().level;
        return level == null ? null : level.registryAccess();
    }

    /**
     * Lays both sides out as paged grids and draws the arrow and figures.
     *
     * <p>The slot lists are already ordered input-first then output, so they are split back apart by
     * role rather than by count: a recipe may have any number of either.</p>
     */
    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, GTChemicalReactionRecipe recipe,
                                   IFocusGroup focuses) {
        List<mezz.jei.api.gui.ingredient.IRecipeSlotDrawable> inputs =
                builder.getRecipeSlots().getSlots(RecipeIngredientRole.INPUT);
        List<mezz.jei.api.gui.ingredient.IRecipeSlotDrawable> outputs =
                builder.getRecipeSlots().getSlots(RecipeIngredientRole.OUTPUT);

        addGrid(builder, inputs, GTChemicalReactionLayout.INPUT_X);
        addGrid(builder, outputs, GTChemicalReactionLayout.OUTPUT_X);

        int arrowTicks = Math.max(MIN_ARROW_TICKS, recipe.getProcessingTime());
        // Positioned through the returned placeable rather than the deprecated (x, y, ticks) form.
        builder.addAnimatedRecipeArrow(arrowTicks)
                .setPosition(GTChemicalReactionLayout.ARROW_X, GTChemicalReactionLayout.ARROW_Y);

        addFigures(builder, recipe);
    }

    private static void addGrid(IRecipeExtrasBuilder builder,
                                List<mezz.jei.api.gui.ingredient.IRecipeSlotDrawable> slots, int x) {
        if (slots.isEmpty()) {
            return;
        }

        // One large box behind the whole grid; the slots themselves carry no frames.
        builder.addDrawable(gridFrame(), x, GTChemicalReactionLayout.GRID_Y);

        var grid = builder.addScrollGridWidget(slots, GTChemicalReactionLayout.COLUMNS,
                GTChemicalReactionLayout.ROWS);
        // Only pages when there is more than one; JEI hides the scrollbar by itself in that case.
        grid.setPosition(x, GTChemicalReactionLayout.GRID_Y);
    }

    /**
     * The single box drawn behind a whole grid.
     *
     * <p>A plain two-tone rectangle rather than a texture, matching the EMI page: the grid holds an
     * arbitrary number of stacks, so individual slot frames would read as a wall of boxes.</p>
     */
    private static mezz.jei.api.gui.drawable.IDrawable gridFrame() {
        return new mezz.jei.api.gui.drawable.IDrawable() {

            @Override
            public int getWidth() {
                return GTChemicalReactionLayout.GRID_WIDTH;
            }

            @Override
            public int getHeight() {
                return GTChemicalReactionLayout.GRID_HEIGHT;
            }

            @Override
            public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
                int right = xOffset + GTChemicalReactionLayout.GRID_WIDTH;
                int bottom = yOffset + GTChemicalReactionLayout.GRID_HEIGHT;

                graphics.fill(xOffset, yOffset, right, bottom, 0xFF8B8B8B);
                graphics.fill(xOffset, yOffset, right, yOffset + 1, 0xFF373737);
                graphics.fill(xOffset, bottom - 1, right, bottom, 0xFF373737);
                graphics.fill(xOffset, yOffset, xOffset + 1, bottom, 0xFF373737);
                graphics.fill(right - 1, yOffset, right, bottom, 0xFF373737);
            }
        };
    }

    private static void addFigures(IRecipeExtrasBuilder builder, GTChemicalReactionRecipe recipe) {
        List<net.minecraft.network.chat.FormattedText> lines = new ArrayList<>(4);
        lines.add(Component.translatable("goldentweaks.chemical_reaction.total_energy",
                GTEnergyFormat.formatExpandedWithUnit(recipe.totalEnergy(), "FE")));
        lines.add(Component.translatable("goldentweaks.chemical_reaction.energy_per_tick",
                GTEnergyFormat.formatExpandedWithUnit(recipe.getEnergyPerTick(), "FE/t")));
        lines.add(Component.translatable("goldentweaks.chemical_reaction.duration",
                GTDurationFormat.format(recipe.getProcessingTime())));
        // Raw integer, same reasoning as the EMI page: this is the value a port is set to.
        lines.add(Component.translatable("goldentweaks.chemical_reaction.mode",
                String.valueOf(recipe.mode())));

        builder.addText(lines, GTChemicalReactionLayout.TEXT_X, GTChemicalReactionLayout.TEXT_Y);
    }

    @Override
    public void draw(GTChemicalReactionRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        // Everything is drawn by the extras above; nothing to paint over the slots.
    }
}
