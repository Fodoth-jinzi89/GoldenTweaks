package net.fodoth.skina.goldentweaks.mixin.shut;

import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Heightmap.class)
public abstract class HeightmapLoggerMixin {
    @Redirect(
            method = "setRawData",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;)V",
                    remap = false
            )
    )
    private void gt$suppressMismatchedHeightmapWarning(Logger logger, String message) {
    }
}
