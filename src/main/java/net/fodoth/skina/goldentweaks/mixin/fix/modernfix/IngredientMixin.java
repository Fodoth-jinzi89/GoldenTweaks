package net.fodoth.skina.goldentweaks.mixin.fix.modernfix;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Ingredient.class, priority = 900)
public abstract class IngredientMixin {
    @Shadow @Final private Ingredient.Value[] values;
    @Shadow private ItemStack[] itemStacks;
    @Shadow private ICustomIngredient customIngredient;

    @Inject(method = "getItems", at = @At("HEAD"), cancellable = true)
    private void gt$cacheDirectItem(CallbackInfoReturnable<ItemStack[]> cir) {
        // The value already strongly owns this stack. A soft reference can only release its array.
        if (customIngredient == null && values.length == 1 && values[0] instanceof Ingredient.ItemValue value) {
            if (itemStacks == null) itemStacks = new ItemStack[]{value.item()};
            cir.setReturnValue(itemStacks);
        }
    }
}
