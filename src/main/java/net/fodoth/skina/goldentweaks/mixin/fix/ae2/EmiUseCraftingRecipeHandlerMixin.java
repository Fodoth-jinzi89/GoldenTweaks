package net.fodoth.skina.goldentweaks.mixin.fix.ae2;

import appeng.integration.modules.emi.EmiUseCraftingRecipeHandler;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Prevents AE2's JEI bridge from crashing EMI-only instances. */
@Mixin(value = EmiUseCraftingRecipeHandler.class, remap = false)
public abstract class EmiUseCraftingRecipeHandlerMixin {
    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private void gt$skipWithoutJei(EmiRecipe recipe, EmiCraftContext<?> context,
                                   CallbackInfoReturnable<Boolean> cir) {
        try {
            Class.forName("mezz.jei.gui.overlay.bookmarks.BookmarkOverlay", false,
                    EmiUseCraftingRecipeHandler.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            cir.setReturnValue(false);
        }
    }
}
