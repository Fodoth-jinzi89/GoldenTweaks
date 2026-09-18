package net.fodoth.skina.goldentweaks.mixin.fix.neoguanniao.client;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * Keeps birds that have no animation for the current state from freezing in their rest pose.
 *
 * <p>{@code AbstractBirdEntity#movementController} picks its animation with expressions like
 * {@code BIRD_DATA.animation().animationMap().get("walk")}. When a bird has no such animation in its data
 * (the myna / "八哥" has no flight animation, for instance) the map lookup returns {@code null} and
 * {@code setAndContinue(null)} leaves the model with no animation at all - which is what happens while such
 * a bird is carried by Carry On, because the carried state is airborne and picks the flight branch.</p>
 *
 * <p>Only the {@code null} case is replaced: an existing animation passes straight through, so nothing about
 * normal birds changes. The idle animation is taken through the public controller chain
 * ({@code getBirdControllers().getBirdAnimationController().pickIdleAnimation()}) rather than shadowing the
 * entity's own accessor, so this mixin cannot break if that accessor changes.</p>
 */
@Mixin(value = AbstractBirdEntity.class, remap = false)
public abstract class BirdMovementControllerMixin {

    @Redirect(
            method = "movementController",
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/animation/AnimationState;setAndContinue(Lsoftware/bernie/geckolib/animation/RawAnimation;)Lsoftware/bernie/geckolib/animation/PlayState;"
            ),
            remap = false
    )
    private PlayState gt$fallbackToIdleAnimation(AnimationState<?> animationState, RawAnimation animation) {
        if (animation != null) {
            return animationState.setAndContinue(animation);
        }

        RawAnimation idle = ((AbstractBirdEntity<?>) (Object) this)
                .getBirdControllers()
                .getBirdAnimationController()
                .pickIdleAnimation();

        return idle != null ? animationState.setAndContinue(idle) : animationState.setAndContinue(animation);
    }
}
