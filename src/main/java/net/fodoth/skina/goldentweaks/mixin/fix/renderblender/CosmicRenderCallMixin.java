package net.fodoth.skina.goldentweaks.mixin.fix.renderblender;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.weibai.renderblender.api.client.render.CosmicRenderCall;
import net.weibai.renderblender.api.iface.transform.CosmicRenderable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

/** Keeps deferred first-person cosmic layers aligned with Complementary's hand sway. */
@Mixin(value = CosmicRenderCall.class, remap = false)
public class CosmicRenderCallMixin {

    @Shadow
    @Final
    public Matrix4f projection;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gt$applyComplementaryHandSway(CosmicRenderable model, ItemStack stack,
                                                ItemDisplayContext context, PoseStack pose,
                                                int light, int overlay, Matrix4f projection,
                                                Matrix4f modelView, CallbackInfo ci) {
        if (context != ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                && context != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            return;
        }
        String packName = Iris.getCurrentPackName();
        if (packName == null || !packName.toLowerCase(Locale.ROOT).contains("complementary")) {
            return;
        }

        String option = Iris.getCurrentPack()
                .map(pack -> pack.getShaderPackOptions().getOptionValues().getStringValueOrDefault("HAND_SWAYING"))
                .orElse("0");
        float multiplier = switch (option) {
            case "1" -> 0.5F;
            case "2" -> 1.0F;
            case "3" -> 2.0F;
            default -> 0.0F;
        };
        if (multiplier == 0.0F) {
            return;
        }

        float time = SystemTimeUniforms.TIMER.getFrameTimeCounter();
        this.projection.m30(this.projection.m30() + multiplier * (float) Math.sin(time * 0.86F) / 256.0F);
        this.projection.m31(this.projection.m31() + multiplier * (float) Math.cos(time * 1.5F) / 64.0F);
    }
}
