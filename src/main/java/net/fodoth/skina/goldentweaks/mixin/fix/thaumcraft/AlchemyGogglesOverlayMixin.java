package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftAdditionalItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.AlchemyGogglesOverlay;

/** Renders cosmic aspect icons shown in-world by the Goggles of Revealing. */
@Mixin(value = AlchemyGogglesOverlay.class, remap = false)
public class AlchemyGogglesOverlayMixin {

    @Inject(method = "renderAspectIcon", at = @At("HEAD"), cancellable = true)
    private static void gt$renderCosmicAspectIcon(Aspect aspect, float x, float y, float size, float alpha,
                                                   PoseStack pose, MultiBufferSource buffers, CallbackInfo ci) {
        if (!GTAspectEntry.isCosmic(aspect.getTag())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        pose.pushPose();
        pose.translate(x, y, 0.01F);
        pose.scale(size, size, size);
        minecraft.getItemRenderer().renderStatic(
                GTThaumcraftAdditionalItems.cosmicIconStack(),
                ItemDisplayContext.FIXED,
                0xF000F0,
                OverlayTexture.NO_OVERLAY,
                pose,
                buffers,
                minecraft.level,
                0
        );
        pose.popPose();
        ci.cancel();
    }
}
