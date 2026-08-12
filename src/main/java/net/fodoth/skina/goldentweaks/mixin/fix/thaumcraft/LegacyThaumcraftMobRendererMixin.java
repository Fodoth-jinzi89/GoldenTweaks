package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.SafeSheepModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.client.renderers.entity.LegacyThaumcraftMobRenderer;
import thaumcraft.common.entities.LegacyThaumcraftMobEntity;
import thaumcraft.common.entities.LegacyThaumcraftMobKind;

/**
 * 让 TAINT_SHEEP 使用 SafeSheepModel（与原版 SheepModel 骨骼层级一致，
 * 因此 OptiFine CEM / ETF / FreshAnimations 可正确替换），
 * 同时避免 SheepModel 内部的 {@code (Sheep)} 强转导致 ClassCastException。
 */
@Mixin(value = LegacyThaumcraftMobRenderer.class, remap = false)
public abstract class LegacyThaumcraftMobRendererMixin {

    @Unique
    private EntityModel<LegacyThaumcraftMobEntity> gt$sheepModel;

    /**
     * 在构造末尾烘焙 ModelLayers.SHEEP，生成 SafeSheepModel。
     */
    @Inject(method = "<init>*", at = @At("RETURN"))
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void gt$onConstructed(EntityRendererProvider.Context context,
                                  EntityModel<LegacyThaumcraftMobEntity> fallback,
                                  CallbackInfo ci) {
        this.gt$sheepModel = (EntityModel) new SafeSheepModel(context.bakeLayer(ModelLayers.SHEEP));
    }

    /**
     * TAINT_SHEEP 返回 SafeSheepModel，使其可被 OptiFine CEM 拦截替换。
     */
    @Inject(method = "modelFor", at = @At("HEAD"), cancellable = true)
    private void gt$onModelFor(LegacyThaumcraftMobEntity entity,
                               CallbackInfoReturnable<EntityModel<LegacyThaumcraftMobEntity>> cir) {
        if (entity.getLegacyKind() == LegacyThaumcraftMobKind.TAINT_SHEEP) {
            cir.setReturnValue(this.gt$sheepModel);
        }
    }
}
