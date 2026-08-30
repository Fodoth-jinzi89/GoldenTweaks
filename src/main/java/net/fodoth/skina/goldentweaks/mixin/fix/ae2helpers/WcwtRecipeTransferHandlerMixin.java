package net.fodoth.skina.goldentweaks.mixin.fix.ae2helpers;

import com.lhy.wcwt.compat.jei.WcwtRecipeTransferHandler;
import com.lhy.wcwt.menu.WirelessComprehensiveWorkTerminalMenu;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.fodoth.skina.goldentweaks.compat.ae2helpers.WcwtPendingHelper;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WcwtRecipeTransferHandler.class, remap = false)
public abstract class WcwtRecipeTransferHandlerMixin {

    @Inject(method = "transferRecipe(Lcom/lhy/wcwt/menu/WirelessComprehensiveWorkTerminalMenu;Ljava/lang/Object;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/world/entity/player/Player;ZZ)Lmezz/jei/api/recipe/transfer/IRecipeTransferError;", at = @At("HEAD"))
    private void gt$setPending(WirelessComprehensiveWorkTerminalMenu menu, Object recipe,
                               IRecipeSlotsView slots, net.minecraft.world.entity.player.Player player,
                               boolean maxTransfer, boolean doTransfer, CallbackInfoReturnable<?> cir) {
        if (!doTransfer) {
            return;
        }
        if (recipe instanceof RecipeHolder<?> holder && holder.value() instanceof CraftingRecipe crafting) {
            WcwtPendingHelper.setPending(menu, crafting);
        } else if (recipe instanceof CraftingRecipe crafting) {
            WcwtPendingHelper.setPending(menu, crafting);
        }
    }
}
