package net.fodoth.skina.goldentweaks.mixin.fix.thaumichorizons;

import com.kentington.thaumichorizons.common.soul.SoulReceiver;
import com.kentington.thaumichorizons.common.soul.SoulSieveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import thaumcraft.common.blockentities.BrainJarBlockEntity;

/**
 * Lets the soul sieve run when nothing that can collect souls sits above it.
 * <p>
 * {@code SoulSieveBlockEntity.tick} computes a per-tick progress budget and forces it to zero unless
 * the block above is a {@link SoulReceiver} or a brain jar with room left:
 *
 * <pre>
 * int budget = Math.min(11, ticksLeft);
 * if (above instanceof SoulReceiver receiver)      budget = receiver.capacity(budget);
 * else if (above instanceof BrainJarBlockEntity)   budget = jar.xp() &gt;= 2000 ? 0 : budget;
 * else                                             budget = 0;      // nothing above
 * if (budget &lt;= 0) { ticking = false; return; }                    // job never advances
 * </pre>
 *
 * A sieve with plain air above it therefore consumes a soul sand, arms its 1200 tick job, and then
 * returns every tick without ever advancing: the extraction bar stays full and no sand or soul is
 * ever produced - which is exactly the "it eats soul sand but does nothing" report. Thaumic Horizons'
 * own description only says that souls fly upwards when no brain jar collects them, so a missing
 * collector should not stop the sand extraction at all.
 */
@Mixin(value = SoulSieveBlockEntity.class, remap = false)
public class SoulSieveReceiverMixin {

    @ModifyVariable(
            method = "tick",
            at = @At(value = "STORE", ordinal = 2),
            index = 8,
            require = 1
    )
    private static int gt$progressWithoutReceiver(
            int budget,
            Level level,
            BlockPos pos,
            BlockState state,
            SoulSieveBlockEntity sieve
    ) {
        if (budget > 0) {
            return budget;
        }

        BlockEntity above = level.getBlockEntity(pos.above());
        if (above instanceof SoulReceiver || above instanceof BrainJarBlockEntity) {
            // A collector is there and genuinely has no room: keep the vanilla behaviour.
            return budget;
        }

        return Math.max(1, Math.min(11, sieve.progress()));
    }
}
