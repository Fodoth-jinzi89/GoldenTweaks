package net.fodoth.skina.goldentweaks.mixin.fix.mekanicalcreate;

import net.fodoth.skina.goldentweaks.compat.mekanicalcreate.BoundedCandidateMap;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Bounds mekanicalcreate's per-{@code RecipeManager} candidate catalog.
 *
 * <p>{@code SimulationRecipeResolver.CANDIDATE_CACHE} is a {@code WeakHashMap<RecipeManager, Map<..>>}, and the
 * inner map is created by a tiny synthetic lambda that just returns a new, <b>unbounded</b> {@code HashMap}
 * ({@code lambda$candidateCatalog$28}). This replaces that (empty) HashMap with a bounded LRU map.</p>
 *
 * <p>The target class and method are addressed by name only, so the mod jar is not needed at compile time;
 * {@code require = 0} plus the plugin gate on the target class means a future mod update degrades to
 * "no optimisation" rather than a hard failure.</p>
 */
@Mixin(targets = "io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver", remap = false)
public class MekanicalCreateCandidateCacheMixin {

    @Inject(method = "lambda$candidateCatalog$28", at = @At("RETURN"), cancellable = true, require = 0)
    private static void gt$boundCandidateCache(RecipeManager recipeManager,
                                               CallbackInfoReturnable<Map<?, ?>> cir) {
        Map<?, ?> created = cir.getReturnValue();
        if (created instanceof HashMap && !(created instanceof BoundedCandidateMap) && created.isEmpty()) {
            cir.setReturnValue(new BoundedCandidateMap<>());
        }
    }
}
