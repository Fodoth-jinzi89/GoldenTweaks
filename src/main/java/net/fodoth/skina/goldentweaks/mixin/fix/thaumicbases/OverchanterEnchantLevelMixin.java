package net.fodoth.skina.goldentweaks.mixin.fix.thaumicbases;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import tb.common.blockentity.OverchanterBlockEntity;

@Mixin(value = OverchanterBlockEntity.class, remap = false)
public abstract class OverchanterEnchantLevelMixin {
    @ModifyArg(
            method = "finishOverchant",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments$Mutable;set(Lnet/minecraft/core/Holder;I)V"),
            index = 1)
    private int gt$doubleEnchantLevel(int original) {
        // finishOverchant supplies the original level plus one; apply a pure x2 multiplier.
        return (original - 1) * 2;
    }
}
