package net.fodoth.skina.goldentweaks.mixin.fix.carryon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.compat.thaumichorizons.client.GTCreaturePreview;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
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
 * <p><b>The replacement has to reproduce exactly what the dispatcher did</b>, which is
 * {@code renderer.getRenderOffset(entity, partialTick)} added to the coordinates it was given, then a raw
 * {@code EntityRenderer#render}. The coordinates Carry On passes are already camera relative, so there is
 * <b>no</b> camera translation to redo here (doing that anyway pushes the entity out of view).</p>
 *
 * <p>Live entities use {@link GTCreaturePreview#renderLive} rather than {@code render} because Carry On's
 * entity really exists in the world: clearing its AI/gravity, or ticking its animation twice, would leak
 * into the actual entity.</p>
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
    @SuppressWarnings("unchecked")
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

        // Same maths as EntityRenderDispatcher#render: coordinates + the renderer's own offset.
        // The dispatcher always hands out the renderer registered for this exact entity; the game itself
        // uses raw generic calls here, so this cast is the same deal (see GTCreaturePreview#renderRaw).
        EntityRenderer<Entity> renderer = (EntityRenderer<Entity>) dispatcher.getRenderer(entity);
        Vec3 renderOffset = renderer.getRenderOffset(entity, partialTick);

        pose.pushPose();
        pose.translate(
                x + renderOffset.x,
                y + renderOffset.y,
                z + renderOffset.z
        );
        GTCreaturePreview.renderLive(mob, pose, buffer, partialTick, packedLight,
                GTCreaturePreview.CARRY_TARGET_HEIGHT);
        pose.popPose();
    }
}
