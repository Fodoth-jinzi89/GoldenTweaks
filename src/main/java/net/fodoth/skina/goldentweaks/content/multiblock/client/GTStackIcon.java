package net.fodoth.skina.goldentweaks.content.multiblock.client;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import mekanism.api.MekanismAPI;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTReactionStack;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTReactionStacks;
import net.fodoth.skina.goldentweaks.content.multiblock.recipe.client.GTAspectEmiStacks;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftAdditionalItems;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortAmounts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.gui.AspectGuiRenderer;

/**
 * One content entry in a slot: its icon, and optionally the amount on the corner.
 *
 * <p>Every kind of content goes through here — an item, a fluid, a Mekanism chemical, an aspect —
 * so a port's grid and the machine's product list draw their cells the same way instead of each
 * keeping its own switch over the stack's kind.</p>
 *
 * <p>With EMI present the icon is drawn through the viewer's own ingredient, which is also what
 * makes hovering the cell able to look a recipe up; a fluid or a chemical then renders exactly as
 * it does in a recipe page. Essentia is the exception: it is not an EMI ingredient unless scex is
 * installed, and Thaumcraft has its own renderer for it, so it is drawn directly. Without EMI the
 * sprite is resolved from whatever the owning mod ships.</p>
 */
public final class GTStackIcon {

    private GTStackIcon() {
    }

    /**
     * Draws one entry.
     *
     * @param showAmount whether to put the amount on the corner. A list that prints the amount as
     *                   text beside the icon passes {@code false}, so the two do not say the same
     *                   thing twice.
     */
    public static void draw(GuiGraphics graphics, Font font, GTReactionStack stack,
                            int x, int y, boolean showAmount) {
        EmiDrawContext context = EmiDrawContext.wrap(graphics);

        if (stack.kind() == GTReactionStack.Kind.ESSENTIA) {
            drawEssentiaEntry(graphics, context, font, stack, x, y, showAmount);
            return;
        }

        if (!ModList.get().isLoaded("emi")) {
            drawWithoutEmi(graphics, font, stack, x, y, showAmount);
            return;
        }

        EmiIngredient ingredient = GTReactionStacks.toEmiIngredient(stack, registries());
        if (ingredient == null) {
            return;
        }
        // Icon only: a stack's own render prints a count for the item flavour alone, so the amount
        // is added below through the very helper EMI uses for that.
        ingredient.render(graphics, x, y, 0.0F, EmiIngredient.RENDER_ICON);
        if (showAmount) {
            EmiRenderHelper.renderAmount(context, x, y, amountText(stack));
        }
    }

    /**
     * Essentia, which has no EMI stack of its own.
     *
     * <p>scex registers the aspect as a viewer ingredient; drawing that entry instead of the bare
     * icon is what lets the player look a recipe up by hovering the slot. Without scex Thaumcraft's
     * renderer draws it, so the cell is never blank on a pack that only has Thaumcraft.</p>
     */
    private static void drawEssentiaEntry(GuiGraphics graphics, EmiDrawContext context, Font font,
                                          GTReactionStack stack, int x, int y, boolean showAmount) {
        EmiStack registered = GTAspectEmiStacks.of(stack.essentiaTag());
        if (registered != null) {
            registered.setAmount(stack.amount())
                    .render(graphics, x, y, 0.0F, EmiIngredient.RENDER_ICON);
        } else {
            drawEssentia(graphics, stack, x, y);
        }
        if (!showAmount) {
            return;
        }
        // Essentia counts in whole units like an item, so it gets the item-shaped badge instead of
        // the fluid one that would append "mB".
        if (ModList.get().isLoaded("emi")) {
            EmiRenderHelper.renderAmount(context, x, y, amountText(stack));
        } else {
            drawAmount(graphics, font, stack.amount(), true, x, y);
        }
    }

    /** Without EMI the sprite is resolved here from whatever the owning mod ships. */
    private static void drawWithoutEmi(GuiGraphics graphics, Font font, GTReactionStack stack,
                                       int x, int y, boolean showAmount) {
        switch (stack.kind()) {
            case ITEM -> {
                var item = stack.toItemStack();
                if (!item.isEmpty()) {
                    graphics.renderItem(item, x, y);
                    graphics.renderItemDecorations(font, item, x, y);
                }
            }
            case FLUID -> {
                ResourceLocation sprite = fluidSprite(stack);
                if (sprite != null) {
                    graphics.blit(sprite, x, y, 0, 0, 16, 16, 16, 16);
                }
            }
            case CHEMICAL -> {
                ResourceLocation sprite = chemicalSprite(stack);
                if (sprite != null) {
                    graphics.blit(sprite, x, y, 0, 0, 16, 16, 16, 16);
                }
            }
            default -> {
            }
        }
        if (showAmount) {
            drawAmount(graphics, font, stack.amount(),
                    stack.kind() == GTReactionStack.Kind.ITEM, x, y);
        }
    }

    private static Component amountText(GTReactionStack stack) {
        return Component.literal(GTPortAmounts.number(stack.amount(),
                stack.kind() == GTReactionStack.Kind.ITEM));
    }

    /**
     * The amount, bottom-right of the cell, in the shape a player expects from an inventory slot.
     *
     * <p>Only used when EMI is absent: with EMI installed its own helper draws the same label and
     * matching its spacing keeps every window looking identical.</p>
     */
    private static void drawAmount(GuiGraphics graphics, Font font, long amount, boolean item,
                                   int x, int y) {
        Component text = Component.literal(GTPortAmounts.number(amount, item));
        int width = font.width(text);
        graphics.drawString(font, text, x + 17 - width, y + 9, 0xFFFFFFFF, true);
    }

    /**
     * Draws one aspect, deferring to Thaumcraft's own renderer.
     *
     * <p>Not a plain blit: the sheet comes from {@code Aspect.image()}, the tint from
     * {@code Aspect.color()}, and the cosmic aspects are not sheet-based at all — they render as a
     * dedicated item model. All three of those live in {@code AspectGuiRenderer}, and the mod
     * already wraps the cosmic case in {@code GTAspectEntry.isCosmic}.</p>
     *
     * <p>Guarded because every class named here belongs to Thaumcraft: this is loaded on packs that
     * do not have it.</p>
     */
    private static void drawEssentia(GuiGraphics graphics, GTReactionStack stack, int x, int y) {
        String tag = stack.essentiaTag();
        if (tag == null) {
            return;
        }
        try {
            Aspect aspect = Aspect.getAspect(tag);
            if (aspect == null) {
                return;
            }
            if (GTAspectEntry.isCosmic(tag)) {
                graphics.renderItem(GTThaumcraftAdditionalItems.cosmicIconStack(), x, y);
            } else {
                AspectGuiRenderer.draw(graphics, aspect, x, y, 16, 1.0F);
            }
        } catch (NoClassDefFoundError absent) {
            // Nothing sensible to draw without the mod's renderer.
        }
    }

    /**
     * The still frame of a fluid, at the path vanilla lays them out on.
     *
     * <p>Fluids are not items, so there is no sprite to ask for; the texture name is derived from
     * the fluid's id the same way the vanilla block models do it.</p>
     */
    @Nullable
    private static ResourceLocation fluidSprite(GTReactionStack stack) {
        Fluid fluid = stack.fluid();
        if (fluid == null) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        // Vanilla's flowing and still frames live under the namespace's own block textures.
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                "textures/block/" + id.getPath() + "_still.png");
    }

    /**
     * The icon of one chemical, or {@code null} without Mekanism.
     *
     * <p>Read through the chemical rather than derived from the id: Mekanism lets a chemical name
     * its own icon, so the registry entry is the only thing that knows the right path.</p>
     */
    @Nullable
    private static ResourceLocation chemicalSprite(GTReactionStack stack) {
        try {
            var holder = MekanismAPI.CHEMICAL_REGISTRY.getHolder(stack.id()).orElse(null);
            if (holder == null) {
                return null;
            }
            ResourceLocation icon = holder.value().getIcon();
            // getIcon gives the texture path without an extension or the textures/ prefix.
            return ResourceLocation.fromNamespaceAndPath(icon.getNamespace(),
                    "textures/" + icon.getPath() + ".png");
        } catch (NoClassDefFoundError absent) {
            return null;
        }
    }

    /**
     * The EMI form of one entry, or {@code null} when EMI is absent or cannot build it.
     *
     * <p>Exposed because hover and click handling need to know what the cell stands for without
     * drawing it — a recipe lookup starts from this, not from the pixels.</p>
     */
    @Nullable
    public static EmiIngredient ingredientOf(GTReactionStack stack) {
        if (!ModList.get().isLoaded("emi")) {
            return null;
        }
        return GTReactionStacks.toEmiIngredient(stack, registries());
    }

    @Nullable
    private static HolderLookup.Provider registries() {
        var level = Minecraft.getInstance().level;
        return level == null ? null : level.registryAccess();
    }
}
