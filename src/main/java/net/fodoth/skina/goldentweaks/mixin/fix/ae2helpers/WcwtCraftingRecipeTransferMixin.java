package net.fodoth.skina.goldentweaks.mixin.fix.ae2helpers;

import com.lhy.wcwt.compat.jei.WcwtCraftingRecipeTransferHandler;
import com.lhy.wcwt.menu.WirelessComprehensiveWorkTerminalMenu;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import rearth.ae2helpers.client.AutoCraftingWatcher;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = WcwtCraftingRecipeTransferHandler.class, remap = false)
public abstract class WcwtCraftingRecipeTransferMixin {
    @Shadow
    @Final
    private IRecipeTransferHandlerHelper transferHelper;

    @Inject(method = "transferRecipe(Lcom/lhy/wcwt/menu/WirelessComprehensiveWorkTerminalMenu;Lnet/minecraft/world/item/crafting/RecipeHolder;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/world/entity/player/Player;ZZ)Lmezz/jei/api/recipe/transfer/IRecipeTransferError;", at = @At("HEAD"))
    private void gt$setPending(WirelessComprehensiveWorkTerminalMenu menu, RecipeHolder<CraftingRecipe> holder,
                               IRecipeSlotsView slots, net.minecraft.world.entity.player.Player player,
                               boolean maxTransfer, boolean doTransfer, CallbackInfoReturnable<?> cir) {
        if (!doTransfer || !AutoCraftingWatcher.INSTANCE.isAutoInsertEnabled()) return;
        Map<Integer, Ingredient> map = transferHelper.getGuiSlotIndexToIngredientMap(holder);
        HashMap<Integer, Ingredient> pending = new HashMap<>();
        map.forEach((slot, ingredient) -> {
            if (!ingredient.isEmpty()) pending.put(slot, ingredient);
        });
        AutoCraftingWatcher.INSTANCE.setPending(pending, menu.findMissingIngredients(pending).craftableSlots());
    }
}
