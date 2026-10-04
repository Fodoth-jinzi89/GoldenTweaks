package net.fodoth.skina.goldentweaks.mixin.balance.mekanism;

import mekanism.common.inventory.slot.BasicInventorySlot;
import net.fodoth.skina.goldentweaks.compat.mekanism.IGTFactorySlotLimit;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让工厂的输入/输出槽位可以突破物品默认堆叠上限（64），上限由 {@link IGTFactorySlotLimit} 的倍率决定。
 * mekanism_extras 的工厂槽位在 getLimit 中做了 8 &lt;&lt; 等级 的放大，evolvedmekanism 没有，这里统一补上。
 */
@Mixin(value = BasicInventorySlot.class, remap = false)
public abstract class BasicInventorySlotLimitMixin implements IGTFactorySlotLimit {

    @Unique
    private int goldentweaks$stackLimitMultiplier = 1;

    @Override
    public int goldentweaks$getStackLimitMultiplier() {
        return goldentweaks$stackLimitMultiplier;
    }

    @Override
    public void goldentweaks$setStackLimitMultiplier(int multiplier) {
        this.goldentweaks$stackLimitMultiplier = multiplier;
    }

    @Inject(method = "getLimit", at = @At("RETURN"), cancellable = true, remap = false)
    private void goldentweaks$applyStackLimitMultiplier(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int multiplier = goldentweaks$stackLimitMultiplier;
        if (multiplier <= 1 || cir.getReturnValueI() <= 0) {
            return;
        }
        try {
            cir.setReturnValue(Math.multiplyExact(cir.getReturnValueI(), multiplier));
        } catch (ArithmeticException e) {
            cir.setReturnValue(Integer.MAX_VALUE);
        }
    }
}
