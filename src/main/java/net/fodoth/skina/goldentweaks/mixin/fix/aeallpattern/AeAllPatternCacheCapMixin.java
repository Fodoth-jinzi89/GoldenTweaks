package net.fodoth.skina.goldentweaks.mixin.fix.aeallpattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Shrinks aeallpattern's aggregate-pattern expansion caches.
 *
 * <p>{@code AggregatePatternExpander} keeps two per-server {@code LinkedHashMap}s
 * ({@code EXPANSION_CACHE} / {@code RECIPE_EXPANSION_CACHE}), each capped at {@code 256} entries by an
 * anonymous {@code removeEldestEntry} override (inner classes {@code AggregatePatternExpander$1} / {@code $2}).
 * A single aggregate expansion stores a whole list of {@code AggregateAssemblerPatternDetails}; a heap dump
 * from the pack showed ~13.3k of those objects (~207 MB) held by this cache — the byte cost is bounded only by
 * the entry cap, and 256 entries is far too many for pattern memoisation.</p>
 *
 * <p>Lowering the cap keeps the memoisation working while bounding the retained heap. Injections are
 * {@code require = 0} and gated on the target class being present, so a future aeallpattern update degrades to
 * "no optimisation" instead of crashing.</p>
 */
@Mixin(targets = {
        "io.github.langqi99.aeallpattern.aggregate.AggregatePatternExpander$1",
        "io.github.langqi99.aeallpattern.aggregate.AggregatePatternExpander$2"
}, remap = false)
public class AeAllPatternCacheCapMixin {

    @Unique
    private static final int GT_AEALLPATTERN_CACHE_LIMIT = 64;

    @ModifyConstant(method = "removeEldestEntry", constant = @Constant(intValue = 256), require = 0)
    private int gt$capCacheEntries(int original) {
        return GT_AEALLPATTERN_CACHE_LIMIT;
    }
}
