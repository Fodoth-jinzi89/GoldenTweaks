package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.client.renderers.entity.mob.MobRenderStyles;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存 {@code MobRenderStyles.create} 里烘培的 {@code ModelPart}，恢复旧版的显存优化。
 *
 * <p>0.3.0 的 {@code LegacyThaumcraftMobRenderer} 会为 29 个实体类型各注册一个渲染器，每个实例都在
 * 构造时调用 {@code MobRenderStyles.create} 并逐个 {@code context.bakeLayer(...)} 烤一整套模型
 * （僵尸/村民/螃蟹/孢子……），同一套几何体会被重复烘培 29 次。这里按 {@code ModelLayerLocation}
 * 缓存 {@code ModelPart}，让后续渲染器复用第一份；模型外壳仍各自创建，渲染时 {@code setupAnim}
 * 会重新写入姿态，因此共享底层部件是安全的。</p>
 */
@Mixin(value = MobRenderStyles.class, remap = false)
public abstract class MobRenderStylesModelCacheMixin {

    @Unique
    private static final Map<ModelLayerLocation, ModelPart> gt$sharedParts = new HashMap<>();

    @Redirect(
            method = "create",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;bakeLayer(Lnet/minecraft/client/model/geom/ModelLayerLocation;)Lnet/minecraft/client/model/geom/ModelPart;"
            )
    )
    private static ModelPart gt$sharedBakeLayer(EntityRendererProvider.Context context, ModelLayerLocation layer) {
        return gt$sharedParts.computeIfAbsent(layer, context::bakeLayer);
    }
}
