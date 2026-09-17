package net.fodoth.skina.goldentweaks.compat.thaumichorizons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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

    /** Last game tick each creature's animation clock was advanced for. */
    private static final Map<Mob, Long> LAST_ANIMATION_TICK = new WeakHashMap<>();

    /** Creatures that already had their leftover world state cleared once. */
    private static final Set<Mob> PREPARED = Collections.newSetFromMap(new WeakHashMap<>());

    private GTCreaturePreview() {
    }

    /** Creature box while it is being carried (Carry On). */
    public static final float CARRY_TARGET_HEIGHT = 1.0F;

    /** Puts the creature into the same pose every frame and advances its idle animation. */
    public static void resetPose(Mob mob) {
        if (PREPARED.add(mob)) {
            clearWorldState(mob);
        }

        pinPose(mob);
        advanceAnimation(mob);
    }

    /**
     * Pins the rotations and the interpolation fields to the current position. Safe for entities the game
     * keeps ticking: it touches nothing but the render pose (no AI, no gravity, no motion).
     */
    private static void pinPose(Mob mob) {
        mob.setYRot(0.0F);
        mob.setXRot(0.0F);
        mob.yRotO = 0.0F;
        mob.xRotO = 0.0F;
        mob.setYHeadRot(0.0F);
        mob.yHeadRotO = 0.0F;
        mob.yBodyRot = 0.0F;
        mob.yBodyRotO = 0.0F;

        // A creature can be stored while it is still moving; the interpolation fields would then
        // make the renderer lerp between the pose it had in the world and the pose it has in the
        // display, which shows up as twitching. Pin them to the current position.
        mob.xOld = mob.getX();
        mob.yOld = mob.getY();
        mob.zOld = mob.getZ();
    }

    /**
     * Renders a creature that the game already ticks normally, the same way {@link #render} does
     * (fixed pose + size fitted to {@code targetHeight}), but without clearing its AI/gravity and without
     * advancing its animation clock a second time - a live entity ticks on its own, so bumping the counter
     * here again would make its animations run twice as fast.
     *
     * <p>Used for the entity Carry On holds: that one is a real entity in the world, so only the render
     * pose may be touched.</p>
     */
    public static void renderLive(
            Mob mob,
            PoseStack pose,
            MultiBufferSource buffer,
            float partialTick,
            int packedLight,
            float targetHeight
    ) {
        pinPose(mob);

        float scale = fitScale(mob, targetHeight);
        EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(mob);

        pose.pushPose();
        pose.scale(scale, scale, scale);
        renderRaw(renderer, mob, 0.0F, partialTick, pose, buffer, packedLight);
        pose.popPose();
    }

    /**
     * A creature that was captured straight out of the world still carries its motion, walk cycle and
     * fall distance. Its block entity keeps ticking it, so those leftovers fight the display pose and
     * the creature twitches (NeoGuanNiao's birds flap erratically). Creatures that went through a
     * curing vat are rebuilt from clean data and do not have this problem.
     */
    private static void clearWorldState(Mob mob) {
        mob.setNoAi(true);
        mob.setNoGravity(true);
        mob.setDeltaMovement(Vec3.ZERO);
        mob.walkAnimation.setSpeed(0.0F);
        mob.fallDistance = 0.0F;
        mob.hurtTime = 0;
    }

    /**
     * Advances the creature's animation clock once per game tick instead of once per rendered frame.
     * Renderers run much faster than 20 times per second, so bumping the counter in the render call
     * makes animations (NeoGuanNiao's birds in particular) play several times too fast.
     */
    private static void advanceAnimation(Mob mob) {
        Level level = mob.level();
        long now = level.getGameTime();
        Long last = LAST_ANIMATION_TICK.get(mob);

        if (last != null && last == now) {
            return;
        }

        LAST_ANIMATION_TICK.put(mob, now);
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
