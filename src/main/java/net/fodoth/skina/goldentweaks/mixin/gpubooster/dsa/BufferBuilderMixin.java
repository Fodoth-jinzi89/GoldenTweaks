package net.fodoth.skina.goldentweaks.mixin.gpubooster.dsa;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.gpubooster.api.GTGL;
import net.fodoth.skina.goldentweaks.util.DSAMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin {

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void goldentweaks$setupCtx(
            ByteBufferBuilder allocator,
            VertexFormat.Mode drawMode,
            VertexFormat format,
            CallbackInfo ci
    ) {
        if (GoldenTweaksClientConfig.hasDSA(DSAMode.VBO)) {
            GTGL.CURRENT_VERTEX_FORMAT.set(format);
        }
    }
}