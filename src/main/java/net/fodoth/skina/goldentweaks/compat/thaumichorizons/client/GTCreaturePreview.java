package net.fodoth.skina.goldentweaks.compat.thaumichorizons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/**
 * Renders a creature that is stored inside a block the way NeoGuanNiao's small bird cage does it:
 * the creature is shown in a fixed pose (no leftover body/head rotation from when it was captured),
 * it keeps ticking so its idle animation stays alive, and it is scaled to fit the display it sits in
 * instead of using one hard-coded scale for every mob.
 */
public final class GTCreaturePreview {

    /** Creature box inside the containment jar. */
    public static final float JAR_TARGET_HEIGHT = 0.5F;

    /** Creature box inside the curing vat. */
    public static final float VAT_TARGET_HEIGHT = 1.6F;

    private static final float MIN_SCALE = 0.2F;
    private static final float MAX_SCALE = 1.0F;

    private GTCreaturePreview() {
    }

    /** Puts the creature into the same pose every frame and advances its idle animation. */
    public static void resetPose(Mob mob) {
        mob.setYRot(0.0F);
        mob.setXRot(0.0F);
        mob.yRotO = 0.0F;
        mob.xRotO = 0.0F;
        mob.setYHeadRot(0.0F);
        mob.yHeadRotO = 0.0F;
        mob.setDeltaMovement(0.0D, 0.0D, 0.0D);
        mob.setNoGravity(true);
        mob.tickCount++;
    }

    /** Scale that makes the creature fit the given height. */
    public static float fitScale(Mob mob, float targetHeight) {
        float height = Math.max(0.05F, mob.getBbHeight());
        return Math.clamp(targetHeight / height, MIN_SCALE, MAX_SCALE);
    }

    /**
     * Renders the creature at the current pose origin, scaled to fit {@code targetHeight}. The caller
     * is responsible for translating the pose to the spot the creature should stand on.
     */
    public static void render(
            Mob mob,
            PoseStack pose,
            MultiBufferSource buffer,
            float partialTick,
            int packedLight,
            float targetHeight
    ) {
        resetPose(mob);

        float scale = fitScale(mob, targetHeight);
        EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(mob);

        pose.pushPose();
        pose.scale(scale, scale, scale);
        renderRaw(renderer, mob, 0.0F, partialTick, pose, buffer, packedLight);
        pose.popPose();
    }

    /** Renders an entity that is not a mob (or a mob we should not touch) exactly as the game would. */
    public static void renderPlain(
            EntityRenderer<?> renderer,
            Entity entity,
            float rotationYaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight
    ) {
        renderRaw(renderer, entity, rotationYaw, partialTick, pose, buffer, packedLight);
    }

    @SuppressWarnings("unchecked")
    private static void renderRaw(
            EntityRenderer<?> renderer,
            Entity entity,
            float rotationYaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight
    ) {
        // The renderer always matches the entity it was looked up for; the game itself uses raw
        // calls here, so this cast is the same deal.
        ((EntityRenderer<Entity>) renderer).render(entity, rotationYaw, partialTick, pose, buffer, packedLight);
    }
}
