package net.fodoth.skina.goldentweaks.mixin.fix.thaumichorizons.client;

import com.kentington.thaumichorizons.client.vat.VatCreatureRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.compat.thaumichorizons.client.GTCreaturePreview;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Rewrites how the creature floating inside a curing vat is drawn, following NeoGuanNiao's small
 * bird cage: fixed pose, living idle animation and a size fitted to the vat instead of the raw mob
 * size. Everything else the vat renders (water, glass, effigy, held item, clone fade) is left alone,
 * so only the creature render call is swapped.
 */
@Mixin(value = VatCreatureRenderer.class, remap = false)
public class VatCreatureRendererMixin {

    @Redirect(
            method = "render(Lcom/kentington/thaumichorizons/common/vat/VatBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            )
    )
    private void gt$renderVatCreature(
            EntityRenderer<?> renderer,
            Entity entity,
            float rotationYaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight
    ) {
        if (entity instanceof Mob mob) {
            GTCreaturePreview.render(mob, pose, buffer, partialTick, packedLight, GTCreaturePreview.VAT_TARGET_HEIGHT);
            return;
        }

        GTCreaturePreview.renderPlain(renderer, entity, rotationYaw, partialTick, pose, buffer, packedLight);
    }
}
