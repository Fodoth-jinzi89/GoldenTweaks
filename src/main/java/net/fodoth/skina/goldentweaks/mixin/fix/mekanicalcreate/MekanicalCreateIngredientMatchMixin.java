package net.fodoth.skina.goldentweaks.mixin.fix.mekanicalcreate;

import net.fodoth.skina.goldentweaks.compat.mekanicalcreate.ItemStackKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code SimulationRecipeResolver#sameIngredient} 用「双重循环 + matched[]」做多重集比较，
 * 两个同长度的大标签（如两份 {@code c:planks}）一次就是 N² 次 ItemStack 比较；
 * 而 {@code mergeDisplayInputs} 会对每条展示配方的每对原料都调它。
 *
 * <p>长度大于阈值时改用计数哈希表，O(n)。小数组仍走原实现，避免多一次分配。</p>
 */
@Mixin(targets = "io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver", remap = false)
public class MekanicalCreateIngredientMatchMixin {

    @Inject(method = "sameIngredient", at = @At("HEAD"), cancellable = true, require = 0)
    private static void gt$hashMatch(Ingredient first, Ingredient second,
                                     CallbackInfoReturnable<Boolean> cir) {

        if (first == second) {
            cir.setReturnValue(true);
            return;
        }

        ItemStack[] firstItems = first.getItems();
        ItemStack[] secondItems = second.getItems();

        if (firstItems.length != secondItems.length) {
            cir.setReturnValue(false);
            return;
        }

        if (firstItems.length <= 4) {
            return;
        }

        Map<ItemStackKey, Integer> counts = new HashMap<>(firstItems.length * 2);

        for (ItemStack stack : firstItems) {
            counts.merge(ItemStackKey.of(stack), 1, Integer::sum);
        }

        for (ItemStack stack : secondItems) {
            ItemStackKey key = ItemStackKey.of(stack);
            Integer count = counts.get(key);
            if (count == null || count == 0) {
                cir.setReturnValue(false);
                return;
            }
            counts.put(key, count - 1);
        }

        cir.setReturnValue(true);
    }
}
