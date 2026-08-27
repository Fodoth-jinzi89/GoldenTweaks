package net.fodoth.skina.goldentweaks.mixin.fix.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.client.render.model.ShadeSeparatedBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.createmod.catnip.impl.client.render.model.BakedModelBuffererImpl", remap = false)
public class BakedModelBuffererImplMixin {

    @Inject(
            method = "bufferModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/createmod/catnip/client/render/model/ShadeSeparatedBufferSource;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void goldentweaks$skipMissingModel(
            BakedModel model,
            BlockPos pos,
            BlockAndTintGetter level,
            BlockState state,
            PoseStack poseStack,
            ShadeSeparatedBufferSource bufferSource,
            CallbackInfo ci
    ) {
        if (model == null) {
            ci.cancel();
        }
    }
}
