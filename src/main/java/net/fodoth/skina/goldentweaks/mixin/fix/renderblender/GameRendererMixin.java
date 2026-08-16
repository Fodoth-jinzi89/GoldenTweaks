package net.fodoth.skina.goldentweaks.mixin.fix.renderblender;

import net.fodoth.skina.goldentweaks.compat.fix.renderblender.RenderBlenderCosmicQueueFlushHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderItemInHand", at = @At("RETURN"))
    private void gt$flushCosmicHandQueue(Camera camera, float partialTick, Matrix4f modelViewMatrix,
                                         CallbackInfo ci) {
        RenderBlenderCosmicQueueFlushHandler.flushRenderBlenderQueue();
    }
}
