package net.fodoth.skina.goldentweaks.mixin.fix.carryon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.compat.thaumichorizons.client.GTCreaturePreview;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
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
 * both passes draw the same entity. So instead of replacing the call, the pose is scaled around it:</p>
 *
 * <ol>
 *   <li>scale about the pose origin, which Carry On has already moved onto the creature (it passes
 *       {@code 0, 0, 0} to the dispatcher and brings its own translation, in first person as well as in
 *       third person),</li>
 *   <li>call the dispatcher with Carry On's own arguments, so its {@code position + renderOffset} and
 *       therefore the creature's place stay exactly as Carry On put them - only the size changes, and
 *       EMF/ETF still see a dispatcher call.</li>
 * </ol>
 *
 * <p>Do <b>not</b> reintroduce a camera correction here: the dispatcher's {@code double x, y, z} are already
 * camera relative, it does not subtract the camera itself. Mixing in {@code Camera#getPosition()} leaves a
 * {@code camera * (scale - 1)} offset, which is invisible only while the scale happens to be exactly
 * {@code 1.0} (anything shorter than the carry box) and throws everything taller - a carried villager, for
 * instance - far off screen.</p>
 *
 * <p>Live entities are prepared with {@link GTCreaturePreview#prepareStored} because Carry On's entity is
 * out of the level (nothing ticks it) but still a real entity: only render pose and animation clock may be
 * touched. That clock is per entity per game tick, so it only works while the stand-in entity is stable -
 * see {@link net.fodoth.skina.goldentweaks.compat.carryon.client.CarryOnCarriedEntityCache}, which the
 * {@code CarryRenderHelperMixin} hooks in. A full {@code Mob#tick()} is never used - the player rides this
 * entity, so ticking it would let the player be dragged around by it.</p>
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

        pose.pushPose();
        // Carry On has already moved the pose onto the creature and passes 0, 0, 0 to the dispatcher, so
        // scaling here is scaling about the creature's own origin; the dispatcher's own maths is left alone.
        pose.scale(scale, scale, scale);
        dispatcher.render(entity, x, y, z, rotationYaw, partialTick, pose, buffer, packedLight);
        pose.popPose();
    }
}
