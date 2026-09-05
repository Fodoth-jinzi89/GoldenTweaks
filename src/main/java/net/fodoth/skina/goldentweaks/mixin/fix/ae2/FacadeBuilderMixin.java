package net.fodoth.skina.goldentweaks.mixin.fix.ae2;

import appeng.client.render.cablebus.FacadeBuilder;
import appeng.items.parts.FacadeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FacadeBuilder.class, remap = false)
public abstract class FacadeBuilderMixin {
    @Redirect(
            method = "buildFacadeItemQuads",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getModel(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Lnet/minecraft/client/resources/model/BakedModel;"))
    private BakedModel gt$getFacadeBlockModel(
            ItemRenderer instance, ItemStack p_174265_, Level p_174266_, LivingEntity p_174267_, int p_174268_) {
        if (!(p_174265_.getItem() instanceof FacadeItem facadeItem)) {
            return instance.getModel(p_174265_, p_174266_, p_174267_, p_174268_);
        }
        return Minecraft.getInstance().getBlockRenderer()
                .getBlockModel(facadeItem.getTextureBlockState(p_174265_));
    }
}
