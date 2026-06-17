package net.fodoth.skina.goldentweaks.mixin.fix.spectrum;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.dafuqs.spectrum.inventories.CraftingTabletScreenHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingTabletScreenHandler.class)
public abstract class CraftingTabletScreenHandlerMixin {

    @WrapOperation(
            method = "removed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private ItemEntity goldentweaks$skipFakeDrop(
            Player player,
            ItemStack itemStack,
            boolean includeThrowerName,
            Operation<ItemEntity> original
    ) {
        if (itemStack.getCount() == 1) {
            return null;
        }

        return original.call(player, itemStack, includeThrowerName);
    }
}