package net.fodoth.skina.goldentweaks.mixin.fix.carryon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.compat.thaumichorizons.client.GTCreaturePreview;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tschipp.carryon.client.render.CarriedObjectRender;

/**
 * Draws the entity Carry On is holding the same way the containment jar / curing vat do it
 * (see {@link GTCreaturePreview}): a fixed pose with no leftover rotation, and a size fitted to the carry
 * box instead of Carry On's single hard-coded 0.8 scale for every mob.
 *
 * <p>Two call sites are redirected: {@code drawFirstPersonEntity} (what you see while carrying) and
 * {@code drawThirdPerson} (other players' carried entities). Everything else Carry On renders (blocks, hand
 * offsets, script transformations, shadow toggles) is left untouched - only the
 * {@code EntityRenderDispatcher#render} call is swapped.</p>
 *
 * <p>The dispatcher normally applies the "world position -> camera space" translation itself, so the
 * replacement does the same before handing the pose to {@link GTCreaturePreview#renderLive}; that keeps the
 * entity exactly where Carry On put it, only its pose/size change. Live entities use {@code renderLive}
 * rather than {@code render} because this entity really exists in the world (clearing its AI/gravity, or
 * ticking its animation twice, would leak into the actual entity).</p>
 */
@Mixin(value = CarriedObjectRender.class, remap = false)
public class CarriedObjectRenderMixin {

    @Redirect(
            method = {
                    "drawFirstPersonEntity(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IF)V",
                    "drawThirdPerson(FLorg/joml/Matrix4f;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            ),
            remap = false
    )
    private static void gt$renderCarriedEntity(
            EntityRenderDispatcher dispatcher,
            Entity entity,
            double x,
            double y,
            double z,
            float rotationYaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight
    ) {
        if (!(entity instanceof Mob mob)) {
            dispatcher.render(entity, x, y, z, rotationYaw, partialTick, pose, buffer, packedLight);
            return;
        }

        Vec3 camera = dispatcher.camera.getPosition();

        pose.pushPose();
        pose.translate(x - camera.x, y - camera.y, z - camera.z);
        GTCreaturePreview.renderLive(mob, pose, buffer, partialTick, packedLight,
                GTCreaturePreview.CARRY_TARGET_HEIGHT);
        pose.popPose();
    }
}
