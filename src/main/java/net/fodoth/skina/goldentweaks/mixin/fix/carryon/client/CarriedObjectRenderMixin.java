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
 * offsets, script transformations, shadow toggles) is left untouched.</p>
 *
 * <p><b>The dispatcher call itself must be kept.</b> {@code EntityRenderDispatcher#render} is the only path
 * entity model/texture mods such as EMF (Entity Model Features) and ETF (Entity Texture Features) hook, so
 * rendering the entity through {@code EntityRenderer#render} directly makes EMF/ETF produce their default
 * variant - a carried baby chicken then shows the adult model and flickers between the two variants because
 * both passes draw the same entity. So instead of replacing the call, the pose is set up around it:</p>
 *
 * <ol>
 *   <li>translate by {@code position - camera} (exactly what the dispatcher would do),</li>
 *   <li>scale about that origin,</li>
 *   <li>call the dispatcher with the camera position, so its own
 *       {@code position + renderOffset - camera} reduces to the renderer's offset alone - the entity stays
 *       exactly where Carry On put it, only pose/size change, and EMF/ETF still see a dispatcher call.</li>
 * </ol>
 *
 * <p>Live entities are prepared with {@link GTCreaturePreview#prepareStored} because Carry On's entity is
 * out of the level (nothing ticks it) but still a real entity: only render pose and animation clock may be
 * touched. A full {@code Mob#tick()} is never used - the player rides this entity, so ticking it would let
 * the player be dragged around by it.</p>
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

        GTCreaturePreview.prepareStored(mob);

        float scale = GTCreaturePreview.fitScale(mob, GTCreaturePreview.CARRY_TARGET_HEIGHT);
        Vec3 camera = dispatcher.camera.getPosition();

        pose.pushPose();
        // The dispatcher's own maths is "translate(position + renderOffset - camera)". Pre-applying
        // "position - camera" and handing it the camera position leaves exactly the render offset for the
        // dispatcher to apply, so the entity does not move; the scale in between is what makes it fit.
        pose.translate(x - camera.x, y - camera.y, z - camera.z);
        pose.scale(scale, scale, scale);
        dispatcher.render(entity, camera.x, camera.y, camera.z, rotationYaw, partialTick, pose, buffer, packedLight);
        pose.popPose();
    }
}
