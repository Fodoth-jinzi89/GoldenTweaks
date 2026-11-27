package net.fodoth.skina.goldentweaks.compat.mioicif.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * One replicator page: UU matter in, one item out.
 *
 * <p>The machine has no recipe list of its own — it asks the quote book what an item is worth at run
 * time — so the page is generated from the reference price table instead, which is the same table
 * the server seeds its quotes from.</p>
 */
final class GTUuReplicationEmiRecipe implements EmiRecipe {

    static final int WIDTH = 118;
    static final int HEIGHT = 60;
    private static final int TANK_X = 4;
    private static final int TANK_Y = 8;
    private static final int TANK_W = 22;
    private static final int TANK_H = 40;
    private static final int OUTPUT_X = 84;
    private static final int OUTPUT_Y = 8;
    private static final int ARROW_X = 34;
    private static final int ARROW_Y = 10;
    private static final int ARROW_W = 30;
    private static final int ARROW_H = 17;
    private static final int NOTE_X = 4;
    private static final int NOTE_Y = 50;

    private final ResourceLocation id;
    private final EmiIngredient uu;
    private final EmiStack output;
    private final long millibuckets;
    private final int tankHeight;

    GTUuReplicationEmiRecipe(ResourceLocation id, Fluid uuFluid, ItemStack result,
                             long millibuckets, int tankCapacity) {
        this.id = id;
        this.millibuckets = millibuckets;
        this.output = EmiStack.of(result);
        this.uu = dev.emi.emi.api.neoforge.NeoForgeEmiStack.of(
                new FluidStack(uuFluid, (int) Math.min(Integer.MAX_VALUE, millibuckets)));
        // The gauge height is the tank's fill level, not the row's worth: a cheap item still shows a
        // thin sliver, which is what the machine itself would look like mid-craft.
        this.tankHeight = Math.max(2, (int) Math.round(
                TANK_H * Math.min(1.0D, (double) millibuckets / (double) Math.max(1, tankCapacity))));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTUuReplicationEmiPlugin.category();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(uu);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // The tank: a dark well whose fill shows how much of the buffer one item drains.
        widgets.addDrawable(TANK_X, TANK_Y, TANK_W, TANK_H, (graphics, mouseX, mouseY, delta) -> {
            graphics.fill(TANK_X, TANK_Y, TANK_X + TANK_W, TANK_Y + TANK_H, 0xFF101010);
            graphics.fill(TANK_X + 1, TANK_Y + TANK_H - this.tankHeight,
                    TANK_X + TANK_W - 1, TANK_Y + TANK_H - 1, 0xFF6A3FA0);
            graphics.fill(TANK_X, TANK_Y, TANK_X + TANK_W, TANK_Y + 1, 0xFF3A3A3A);
        });

        // A hoverable stack over the tank carries the amount as a slot tooltip rather than as a
        // badge: the figure can exceed a stack, and a badge is capped at 99.
        widgets.addSlot(uu, TANK_X + 3, TANK_Y + 7).drawBack(false);

        widgets.addFillingArrow(ARROW_X, ARROW_Y, 1000);
        widgets.addTooltipText(
                List.of(
                        Component.translatable("goldentweaks.uu_replication.tooltip",
                                GTUuMatterFormat.mb(this.millibuckets)),
                        Component.translatable("goldentweaks.uu_replication.tank",
                                GTUuMatterFormat.mb(GTUuMatter.tankCapacity()))
                                .withStyle(ChatFormatting.GRAY)),
                ARROW_X, ARROW_Y, ARROW_W, ARROW_H);

        widgets.addSlot(output, OUTPUT_X, OUTPUT_Y).recipeContext(this);

        widgets.addText(
                Component.translatable("goldentweaks.uu_replication.cost",
                        GTUuMatterFormat.mb(this.millibuckets)),
                NOTE_X, NOTE_Y, 0x404040, false);
    }
}
