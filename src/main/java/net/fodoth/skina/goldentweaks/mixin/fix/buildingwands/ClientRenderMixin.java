package net.fodoth.skina.goldentweaks.mixin.fix.buildingwands;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.nicguzzo.wands.client.render.ClientRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientRender.class)
public class ClientRenderMixin {

    @Inject(method = "render_shape", at = @At("HEAD"), cancellable = true)
    private static void gt$disableBlockPreview(PoseStack poseStack, VertexConsumer consumer, BlockState state,
                                               double x, double y, double z, CallbackInfo ci) {
        if (GoldenTweaksClientConfig.DISABLE_BUILDING_WANDS_BLOCK_PREVIEW.get()) {
            ci.cancel();
        }
    }
}
