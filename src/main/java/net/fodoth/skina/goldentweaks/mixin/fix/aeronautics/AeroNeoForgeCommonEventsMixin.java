package net.fodoth.skina.goldentweaks.mixin.fix.aeronautics;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
        targets = "dev.eriksonn.aeronautics.neoforge.events.AeroNeoForgeCommonEvents$ModBusEvents",
        remap = false
)
public abstract class AeroNeoForgeCommonEventsMixin {

    @WrapWithCondition(
            method = "registerEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/eriksonn/aeronautics/neoforge/events/AeroNeoForgeCommonEvents$ModBusEvents;jeiCompat()V"
            )
    )
    private static boolean goldentweaks$skipBrokenJeiCompat(RegisterEvent event) {
        return false;
    }
}