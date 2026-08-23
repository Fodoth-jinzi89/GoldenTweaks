package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraftcelestial;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import thaumcraft.celestial.common.world.CelestialAffinityManager;

@Mixin(value = CelestialAffinityManager.class, remap = false)
public class CelestialAffinityManagerMixin {

    @ModifyArg(
            method = "observe",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/celestial/common/world/CelestialAffinityManager;addCalamity(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/nbt/CompoundTag;I)V"
            ),
            index = 2
    )
    private static int gt$reduceObservationCalamity(int amount) {
        return amount / 10;
    }
}
