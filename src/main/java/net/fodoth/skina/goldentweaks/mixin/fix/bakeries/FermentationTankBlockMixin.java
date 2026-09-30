package net.fodoth.skina.goldentweaks.mixin.fix.bakeries;

import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code FermentationTankBlock#useItemOn} hard-codes Bakery's own whole wheat flour as the only item that can be
 * added (the first {@code ItemStack#is} call in the method). The item-unification rule maps
 * {@code bakeries:whole_wheat_flour} to {@code create:wheat_flour}, so players holding the unified flour could no
 * longer put flour into the tank. Accept whatever item OEI replaced the checked one with as well.
 *
 * <p>{@code targets} is addressed by name so the bakeries jar is not needed at compile time, and {@code require = 0}
 * plus the plugin gate on the target class means a future bakeries update degrades to "tank only accepts its own
 * flour again" instead of a startup crash.</p>
 */
@Mixin(targets = "com.renyigesai.bakeries.common.blocks.FermentationTankBlock", remap = false)
public abstract class FermentationTankBlockMixin {

    @Redirect(
            method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z",
                    ordinal = 0
            ),
            require = 0
    )
    private static boolean goldentweaks$acceptUnifiedWholeWheatFlour(ItemStack stack, Item item) {
        if (stack.is(item)) {
            return true;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (itemId == null) {
            return false;
        }
        String replacementId = ItemReplacementCache.matchItem(itemId.toString());
        if (replacementId == null) {
            return false;
        }
        ResourceLocation replaced = ResourceLocation.tryParse(replacementId);
        if (replaced == null || !BuiltInRegistries.ITEM.containsKey(replaced)) {
            return false;
        }
        return stack.is(BuiltInRegistries.ITEM.get(replaced));
    }
}
