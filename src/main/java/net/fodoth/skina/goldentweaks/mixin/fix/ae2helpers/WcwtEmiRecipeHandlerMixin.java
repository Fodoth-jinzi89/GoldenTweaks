package net.fodoth.skina.goldentweaks.mixin.fix.ae2helpers;

import com.lhy.wcwt.compat.emi.WcwtEmiRecipeHandler;
import com.lhy.wcwt.menu.WirelessComprehensiveWorkTerminalMenu;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import net.fodoth.skina.goldentweaks.compat.ae2helpers.WcwtPendingHelper;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WcwtEmiRecipeHandler.class, remap = false)
public abstract class WcwtEmiRecipeHandlerMixin {

    @Inject(method = "craft(Ldev/emi/emi/api/recipe/EmiRecipe;Ldev/emi/emi/api/recipe/handler/EmiCraftContext;)Z", at = @At("HEAD"))
    private void gt$setPending(EmiRecipe recipe, EmiCraftContext<WirelessComprehensiveWorkTerminalMenu> context,
                               CallbackInfoReturnable<Boolean> cir) {
        if (context.getType() != EmiCraftContext.Type.FILL_BUTTON) {
            return;
        }
        RecipeHolder<?> holder = recipe.getBackingRecipe();
        if (holder != null && holder.value() instanceof CraftingRecipe crafting) {
            WcwtPendingHelper.stagePending(context.getScreenHandler(), crafting);
        }
    }
}
