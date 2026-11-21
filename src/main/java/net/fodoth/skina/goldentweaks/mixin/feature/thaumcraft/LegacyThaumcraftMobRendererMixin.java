package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.client.GTLegacyMobStyles;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.client.renderers.entity.LegacyThaumcraftMobRenderer;
import thaumcraft.client.renderers.entity.mob.MobRenderStyle;
import thaumcraft.common.entities.LegacyThaumcraftMobEntity;
import thaumcraft.common.entities.LegacyThaumcraftMobKind;

import java.util.Map;

@Mixin(value = LegacyThaumcraftMobRenderer.class, remap = false)
public abstract class LegacyThaumcraftMobRendererMixin {
    @Redirect(method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lnet/minecraft/client/model/EntityModel;)V", at = @At(value = "INVOKE",
            target = "Lthaumcraft/client/renderers/entity/mob/MobRenderStyles;create(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lnet/minecraft/client/renderer/entity/RenderLayerParent;Lnet/minecraft/client/model/EntityModel;)Ljava/util/Map;"))
    private Map<LegacyThaumcraftMobKind, MobRenderStyle> gt$lazyStyles(EntityRendererProvider.Context context,
            RenderLayerParent<LegacyThaumcraftMobEntity, EntityModel<LegacyThaumcraftMobEntity>> parent,
            EntityModel<LegacyThaumcraftMobEntity> model) {
        return new GTLegacyMobStyles(context, parent, model);
    }
}
