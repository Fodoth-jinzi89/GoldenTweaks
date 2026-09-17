package net.fodoth.skina.goldentweaks.compat.neoguanniao.client;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.minecraft.world.entity.Mob;

/**
 * Soft bridge to NeoGuanNiao's own animation clock.
 *
 * <p>That mod's birds do not animate off {@code Entity#tickCount}: they drive their model through
 * {@code BirdTickController} (which owns {@code BirdTickTimer} and, among others, a
 * {@code BirdIdleAnimationTicker}), and pick the GeoLib animation with
 * {@code AnimationState.setAndContinue(BirdAnimationController.pickIdleAnimation())}.</p>
 *
 * <p>The clock has two halves and both are public, so a bird that another mod is holding outside the level
 * (Carry On) can keep animating without the entity being fully ticked - a full {@code Mob#tick()} would also
 * drive the ride/vehicle logic of the player holding it:</p>
 * <ul>
 *   <li>{@code BirdTickController#tickClient()} runs the timers
 *       ({@code BirdTickTimer}: idle / fly / eat / ... tickers);</li>
 *   <li>{@code BirdAnimationController#tick()} is the one that actually picks and pushes the GeoLib
 *       animation ({@code pickIdleAnimation()} / {@code shouldPlayFlyAnimation()} →
 *       {@code AnimationState.setAndContinue}).</li>
 * </ul>
 *
 * <p>This class must only be touched when {@code neoguanniao} is loaded (see
 * {@code GTCreaturePreview#tickStoredMob}); loading it pulls in the mod's classes.</p>
 */
public final class BirdAnimationBridge {

    private BirdAnimationBridge() {
    }

    /**
     * @return true if the entity is a NeoGuanNiao bird and its client animation clock was advanced
     */
    public static boolean tickClient(Mob mob) {
        if (!(mob instanceof AbstractBirdEntity<?> bird)) {
            return false;
        }

        bird.getTickController().tickClient();
        bird.getBirdControllers().getBirdAnimationController().tick();
        return true;
    }
}
