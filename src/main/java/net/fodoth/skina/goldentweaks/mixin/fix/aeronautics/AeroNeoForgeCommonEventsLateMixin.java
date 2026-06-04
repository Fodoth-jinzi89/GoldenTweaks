package net.fodoth.skina.goldentweaks.mixin.fix.aeronautics;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.mixin.fix.aeronautics.accessor.AeroNeoForgeCommonEventsAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
        targets = "dev.eriksonn.aeronautics.neoforge.events.AeroNeoForgeCommonEvents$ModBusEvents",
        remap = false
)
public abstract class AeroNeoForgeCommonEventsLateMixin {

    @WrapOperation(
            method = "commonSetup",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/eriksonn/aeronautics/neoforge/index/AeroFluidsNeoForge;registerFluidInteractions()V"
            )
    )
    private static void goldentweaks$lateJeiCompat(
            Operation<Void> original
    ) {
        original.call();

        try {
            GoldenTweaks.LOGGER.warn(
                    "[GoldenTweaks] Applying delayed Aeronautics JEI compatibility registration"
            );

            AeroNeoForgeCommonEventsAccessor.goldentweaks$invokeJeiCompat();

            GoldenTweaks.LOGGER.warn(
                    "[GoldenTweaks] Successfully registered delayed Aeronautics JEI compatibility"
            );
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.warn(
                    "[GoldenTweaks] Failed to register delayed Aeronautics JEI compatibility",
                    t
            );
        }
    }
}