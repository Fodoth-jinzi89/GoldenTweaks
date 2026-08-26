package net.fodoth.skina.goldentweaks.mixin.shut;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "team.teampotato.ruok.util.particle.async.ParticleAsyncManager", remap = false)
public abstract class RuokParticleLoggerMixin {

    @Redirect(
            method = "lambda$tickAsync$2",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;[Ljava/lang/Object;)V"
            )
    )
    private static void gt$suppressAsyncParticleError(
            Logger logger, String message, Object[] arguments
    ) {
    }
}
