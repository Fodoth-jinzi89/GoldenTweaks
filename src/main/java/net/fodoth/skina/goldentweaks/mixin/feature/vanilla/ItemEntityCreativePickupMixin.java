package net.fodoth.skina.goldentweaks.mixin.feature.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 创造模式玩家背包满时仍可拾取掉落物（视为有空间）。
 * <p>
 * 快速拾取等场景会把物品拉向玩家，但 vanilla 的 {@code ItemEntity#playerTouch}
 * 在背包满时不会拾取，物品会留在/落到地面。这里把 {@code Inventory.add} 的失败
 * 结果改成成功，并在原逻辑销毁实体前清空栈，让掉落物被正常消耗。
 */
@Mixin(ItemEntity.class)
public class ItemEntityCreativePickupMixin {

    @WrapOperation(
            method = "playerTouch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean gt$creativeFullInventory(
            Inventory inventory, ItemStack stack, Operation<Boolean> original, Player player
    ) {
        boolean added = original.call(inventory, stack);
        if (!added && player.getAbilities().instabuild) {
            // 创造模式背包满也视为拾取成功：清空栈，让原逻辑销毁实体并结算统计
            stack.setCount(0);
            return true;
        }
        return added;
    }
}
