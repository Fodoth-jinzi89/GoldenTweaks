package net.fodoth.skina.goldentweaks.mixin.fix.thaumicbases;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tb.common.item.TBWandComponents;
import thaumcraft.api.wands.WandRod;

@Mixin(value = TBWandComponents.class, remap = false)
public abstract class WandRodCapacityMixin {
    @Inject(method = "bootstrap", at = @At("RETURN"))
    private static void gt$setWandRodCapacities(CallbackInfo ci) {
        WandRod thaumium = WandRod.get("tbthaumium");
        if (thaumium != null) {
            thaumium.setCapacity(80);
        }
        WandRod voidRod = WandRod.get("tbvoid");
        if (voidRod != null) {
            voidRod.setCapacity(160);
        }
    }
}
