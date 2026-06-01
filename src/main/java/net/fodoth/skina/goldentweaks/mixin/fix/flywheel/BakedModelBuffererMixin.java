package net.fodoth.skina.goldentweaks.mixin.fix.flywheel;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.model.baked.BlockMaterialFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.engine_room.flywheel.lib.model.baked.BakedModelBufferer")
public class BakedModelBuffererMixin {

    @Inject(
            method = "bufferModel",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$skipInvalidModels(
            BakedModel model,
            BlockPos pos,
            BlockAndTintGetter level,
            BlockState state,
            PoseStack poseStack,
            BlockMaterialFunction blockMaterialFunction,
            CallbackInfoReturnable<?> cir
    ) {

        if (model == null) {
            cir.cancel();
            return;
        }

        try {

            ModelData modelData =
                    model.getModelData(level, pos, state, level.getModelData(pos));

            ChunkRenderTypeSet renderTypes =
                    model.getRenderTypes(
                            state,
                            RandomSource.create(),
                            modelData
                    );

            if (renderTypes.isEmpty()) {
                cir.cancel();
            }

        } catch (Throwable ignored) {
            cir.cancel();
        }
    }
}