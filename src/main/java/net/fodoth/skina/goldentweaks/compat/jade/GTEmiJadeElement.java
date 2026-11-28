package net.fodoth.skina.goldentweaks.compat.jade;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.stack.EmiIngredient;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTReactionStack;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTReactionStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ui.Element;

/**
 * Draws an EMI ingredient inside a Jade tooltip.
 *
 * <p>Chemicals and essentia have no icon Jade can build on its own — it knows items and fluids — but
 * both already have a rendering in EMI: a chemical draws its own sprite with its amount badge, and an
 * aspect draws through scex when that mod is present. Reusing that stack is what makes the tooltip
 * show the same picture as the recipe page instead of an approximated one.</p>
 *
 * <p>Hover, search and recipe lookups do not work from a tooltip, so only the drawing is reused; the
 * line of text beside it carries the name and amount.</p>
 */
public final class GTEmiJadeElement extends Element {

    /** The slot is the same 16x16 the rest of the tooltip uses for stacks. */
    private static final float SIZE = 16.0F;

    private final EmiIngredient ingredient;
    private final float scale;

    public GTEmiJadeElement(EmiIngredient ingredient, float scale) {
        this.ingredient = ingredient;
        this.scale = scale;
    }

    /**
     * The element for one of this mod's own reaction stacks, or {@code null} when it cannot be
     * drawn.
     *
     * <p>{@code null} covers three cases that all look the same from the caller: EMI is not
     * installed, the id no longer resolves, and the stack is of a kind EMI has no rendering for.
     * The caller writes the name as text either way, so a missing icon is a smaller loss than a
     * row that silently disappears.</p>
     */
    public static @Nullable GTEmiJadeElement of(GTReactionStack stack, float scale) {
        if (!ModList.get().isLoaded("emi")) {
            return null;
        }
        var level = Minecraft.getInstance().level;
        var ingredient = GTReactionStacks.toEmiIngredient(
                stack, level == null ? null : level.registryAccess());
        return ingredient == null ? null : new GTEmiJadeElement(ingredient, scale);
    }

    @Override
    public Vec2 getSize() {
        return new Vec2(SIZE * this.scale, SIZE * this.scale);
    }

    @Override
    public void render(GuiGraphics graphics, float x, float y, float mouseX, float mouseY) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(this.scale, this.scale, 1.0F);
        // Drawn at the origin of the translated pose, so the scale is applied to the slot itself and
        // not to the coordinates it occupies.
        this.ingredient.render(graphics, 0, 0, 0.0F);
        pose.popPose();
    }
}
