package net.fodoth.skina.goldentweaks.mixin.gpubooster.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.mojang.blaze3d.pipeline.MainTarget$Dimension")
public interface MainTargetDimensionAccessor {

    @Accessor("width")
    int goldentweaks$getWidth();

    @Accessor("height")
    int goldentweaks$getHeight();
}