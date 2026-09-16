package net.fodoth.skina.goldentweaks.mixin.fix.thaumichorizons;

import com.kentington.thaumichorizons.common.soul.SoulSieveBlockEntity;
import com.kentington.thaumichorizons.common.soul.SoulVis;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes the soul sieve survive a bad CentiVis response from the vis network.
 * <p>
 * {@code SoulSieveBlockEntity.tick} drains aer CentiVis to speed up its sand extraction and throws
 * an {@link IllegalStateException} when the drained amount is outside {@code [0, requested]}. That
 * exception escapes the tick while the block entity's re-entrancy flag is still set and can never be
 * cleared again, so a single bad response freezes the sieve for good - it keeps accepting soul sand
 * but never finishes a job, which looks exactly like "the sieve does not work, and a nearby
 * energized node does not help either".
 * <p>
 * Clamping the response (and logging it at debug level) keeps the sieve running: a bogus response is
 * simply treated as no boost for that tick, the documented base speed still applies.
 */
@Mixin(value = SoulSieveBlockEntity.class, remap = false)
public class SoulSieveBoostMixin {

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/kentington/thaumichorizons/common/soul/SoulVis;drain(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;I)I"
            )
    )
    private static int gt$tolerantVisDrain(Level level, BlockPos pos, int amount) {
        int drained = SoulVis.drain(level, pos, amount);
        if (drained < 0 || drained > amount) {
            GoldenTweaks.LOGGER.debug(
                    "Soul sieve centivis response {} is out of range (requested {}) at {}",
                    drained,
                    amount,
                    pos
            );
            return Math.max(0, Math.min(drained, amount));
        }

        return drained;
    }
}
