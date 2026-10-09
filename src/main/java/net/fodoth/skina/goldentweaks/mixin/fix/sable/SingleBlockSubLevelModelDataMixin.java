package net.fodoth.skina.goldentweaks.mixin.fix.sable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.neoforge.platform.SableSubLevelRenderPlatformImpl;
import dev.ryanhcode.sable.sublevel.render.vanilla.SingleBlockSubLevelWrapper;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 只由单个方块组成的 Sable 子关卡走 {@link SableSubLevelRenderPlatformImpl} 这条独立渲染路径，
 * 它取的是包装器/level 上的 ModelData（默认 EMPTY），从不调用方块模型的
 * {@link BakedModel#getModelData} 钩子。精致存储把每格显示物烘进方块模型（{@code DISPLAY_ITEMS}），
 * 因此在这类结构上只剩 BE 画出的数量、物品图标消失。这里补上该钩子，让单方块子关卡与
 * 普通世界/多方块子关卡（SectionCompiler 会调用钩子）行为一致。
 */
@Mixin(value = SableSubLevelRenderPlatformImpl.class, remap = false)
public class SingleBlockSubLevelModelDataMixin {

    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    private void gt$tesselateBlockWithModelData(
            SingleBlockSubLevelWrapper wrapper,
            BakedModel model,
            BlockState state,
            BlockPos pos,
            PoseStack poseStack,
            VertexConsumer consumer,
            RandomSource random,
            long seed,
            int packedOverlay,
            RenderType renderType,
            CallbackInfo ci
    ) {
        ModelData modelData = model.getModelData(wrapper, pos, state, wrapper.getLevel().getModelData(pos));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer()
                .tesselateWithoutAO(wrapper, model, state, pos, poseStack, consumer, true, random, seed, packedOverlay, modelData, renderType);
        ci.cancel();
    }

    @Inject(method = "getRenderLayers", at = @At("HEAD"), cancellable = true)
    private void gt$getRenderLayersWithModelData(
            SingleBlockSubLevelWrapper wrapper,
            BakedModel model,
            BlockState state,
            BlockPos pos,
            RandomSource random,
            CallbackInfoReturnable<List<RenderType>> cir
    ) {
        ModelData modelData = model.getModelData(wrapper, pos, state, wrapper.getModelData(pos));
        cir.setReturnValue(model.getRenderTypes(state, random, modelData).asList());
    }
}
