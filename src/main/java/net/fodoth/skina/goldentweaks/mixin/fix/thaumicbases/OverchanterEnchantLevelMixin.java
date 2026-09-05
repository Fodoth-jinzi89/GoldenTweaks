package net.fodoth.skina.goldentweaks.mixin.fix.thaumicbases;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import thaumcraft.api.aspects.Aspect;
import tb.common.blockentity.OverchanterBlockEntity;

@Mixin(value = OverchanterBlockEntity.class, remap = false)
public abstract class OverchanterEnchantLevelMixin {
    @ModifyArg(
            method = "serverTick",
            at = @At(value = "INVOKE", target = "Lthaumcraft/common/essentia/EssentiaHandler;drainEssentia(Lnet/minecraft/world/level/block/entity/BlockEntity;Lthaumcraft/api/aspects/Aspect;Lnet/minecraft/core/Direction;IZ)Z"),
            index = 1)
    private static Aspect gt$useIncantatio(Aspect original) {
        Aspect incantatio = Aspect.get("incantatio");
        return incantatio != null ? incantatio : original;
    }

    @ModifyArg(
            method = "finishOverchant",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments$Mutable;set(Lnet/minecraft/core/Holder;I)V"),
            index = 1)
    private int gt$doubleEnchantLevel(int original) {
        // finishOverchant supplies the original level plus one; apply a pure x2 multiplier.
        return (original - 1) * 2;
    }
}
