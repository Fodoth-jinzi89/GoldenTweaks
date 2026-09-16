package net.fodoth.skina.goldentweaks.mixin.fix.thaumichorizons.client;

import com.kentington.thaumichorizons.client.jar.SoulJarRenderer;
import com.kentington.thaumichorizons.common.jar.SoulJarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.compat.thaumichorizons.client.GTCreaturePreview;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rewrites how the creature inside a containment jar is drawn, following NeoGuanNiao's small bird
 * cage: fixed pose, living idle animation and a size that actually fits the jar. Thaumic Horizons
 * renders every stored mob at a flat 0.25 scale without touching its rotation, so large mobs poke out
 * of the jar and small ones disappear.
 * <p>
 * Thaumic Horizons' own soul swirl is kept by calling its public helper.
 */
@Mixin(value = SoulJarRenderer.class, remap = false)
public class SoulJarRendererMixin {

    @Inject(
            method = "render(Lcom/kentington/thaumichorizons/common/jar/SoulJarBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gt$renderCreaturePreview(
            SoulJarBlockEntity jar,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight,
            int overlay,
            CallbackInfo ci
    ) {
        pose.pushPose();
        SoulJarRenderer.soul(pose, buffer, jar.isSoul());
        pose.popPose();

        Mob mob = jar.visualEntity();
        if (mob != null) {
            pose.pushPose();
            pose.translate(0.5D, 0.05D, 0.5D);
            GTCreaturePreview.render(mob, pose, buffer, partialTick, packedLight, GTCreaturePreview.JAR_TARGET_HEIGHT);
            pose.popPose();
        }

        ci.cancel();
    }
}
