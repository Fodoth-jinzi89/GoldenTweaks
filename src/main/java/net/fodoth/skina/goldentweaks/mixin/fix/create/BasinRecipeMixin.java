package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.foundation.recipe.DummyCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(BasinRecipe.class)
public class BasinRecipeMixin {

    @Redirect(
            method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/recipe/DummyCraftingContainer;asCraftInput()Lnet/minecraft/world/item/crafting/CraftingInput;"
            )
    )
    private static CraftingInput goldentweaks$realCraftInput(DummyCraftingContainer container) {
        int size = container.getContainerSize();
        List<ItemStack> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(container.getItem(i));
        }
        return CraftingInput.of(1, size, items);
    }
}
