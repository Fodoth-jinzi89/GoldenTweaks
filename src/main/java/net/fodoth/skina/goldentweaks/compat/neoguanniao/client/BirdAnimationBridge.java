package net.fodoth.skina.goldentweaks.compat.neoguanniao.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.minecraft.world.entity.Mob;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Soft bridge to NeoGuanNiao's own preview animation driver.
 *
 * <p>A bird does not simply animate off {@code Entity#tickCount}: GeoLib's animation time, the client
 * tickers ({@code BirdTickTimer}: idle / trust / curious / ...) and the behaviour state machine
 * ({@code IDLE} / {@code CURIOUS} / {@code SLEEPING}, chosen in
 * {@code AbstractBirdEntity#movementController} and normally driven by goals) all have to advance. Only
 * the mod itself can do that correctly, so it exposes
 * {@code AbstractBirdEntity#tickAnimationPreview(long)} - the very method its own bird cage preview uses.</p>
 *
 * <p>Carry On takes the held creature out of the level ({@code PickupHandler} uses a
 * {@code RemovalReason}), so nothing ticks it and its animation freezes. This bridge lets the render call
 * drive it instead.</p>
 *
 * <p>{@code Entity#tick()} must <b>not</b> be used for this: the player holding a creature rides it, so a
 * full tick drives the ride/vehicle logic too (the player then shoots off in whatever direction is
 * pressed).</p>
 *
 * <p>This class must only be touched when {@code neoguanniao} is loaded (see
 * {@code GTCreaturePreview#tickStoredMob}); loading it pulls in the mod's classes.</p>
 */
public final class BirdAnimationBridge {

    /** One line per game session so a log tells whether this bridge is actually the one running. */
    private static final AtomicBoolean LOGGED = new AtomicBoolean();

    private BirdAnimationBridge() {
    }

    /**
     * @param gameTime {@code mob.level().getGameTime()} of the calling client tick
     * @return true if the entity is a NeoGuanNiao bird and its preview animation was advanced
     */
    public static boolean tickPreview(Mob mob, long gameTime) {
        if (!(mob instanceof AbstractBirdEntity<?> bird)) {
            return false;
        }

        // De-duplicates per game tick on the mod's side as well (previewTickedAt), so calling this from
        // several render passes is fine.
        bird.tickAnimationPreview(gameTime);

        if (LOGGED.compareAndSet(false, true)) {
            GoldenTweaks.LOGGER.info(
                    "[CarryOn 兼容] 已接管观鸟的抱持预览动画（tickAnimationPreview，本条只打印一次）"
            );
        }

        return true;
    }
}
