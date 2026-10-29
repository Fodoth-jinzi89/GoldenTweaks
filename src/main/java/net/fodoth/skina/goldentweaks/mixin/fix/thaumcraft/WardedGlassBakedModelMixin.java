package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft.accessor.BakedModelWrapperAccessor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.client.model.WardedGlassBakedModel;

import java.util.List;

/**
 * 守卫者玻璃渲染回退到旧版（port.152）行为。
 *
 * <p>port.278 把守卫者玻璃的连接纹理面烘进了区块网格，并且对无朝向的那趟调用
 * （{@code side == null}：物品栏 / 手持 / EMI / 破坏粒子）直接返回空列表，导致物品模型不再显示。
 * 旧版的做法是方块模型留空，玻璃面由 {@code WardedGlassConnectedRenderer} 逐帧自绘
 * （GoldenTweaks 里是 {@code GTWardedGlassRenderer}），物品走普通的方块贴图模型。</p>
 *
 * <p>这里：方块面返回空（交给 {@code GTWardedGlassRenderer} 自绘），无朝向那趟交还内层原模型
 * （恢复物品 / 粒子的普通外观）。副作用与旧版一致：放进移动结构（如 Create 装置，走
 * {@code WardedProxyBlockEntityRenderer} 的 {@code renderSingleBlock}）时玻璃本体不再显示，
 * 只剩聚焦覆盖层。</p>
 */
@Mixin(value = WardedGlassBakedModel.class, remap = false)
public abstract class WardedGlassBakedModelMixin {

    @Inject(
            method = "getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gt$restoreLegacyWardedGlass(
            @Nullable BlockState state,
            @Nullable Direction side,
            RandomSource random,
            ModelData data,
            @Nullable RenderType renderType,
            CallbackInfoReturnable<List<BakedQuad>> cir
    ) {
        BakedModel original = ((BakedModelWrapperAccessor) (Object) this).gt$getOriginalModel();
        cir.setReturnValue(side == null
                ? original.getQuads(state, side, random, data, renderType)
                : List.of());
    }
}
