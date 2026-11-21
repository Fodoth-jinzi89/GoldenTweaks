package net.fodoth.skina.goldentweaks.mixin.fix.mekanicalcreate;

import net.fodoth.skina.goldentweaks.compat.mekanicalcreate.GTCandidateIndexes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.Map;

@Mixin(targets = "io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver$CandidateCatalog", remap = false)
public abstract class MekanicalCreateCandidateIndexMixin {

    @ModifyArg(method = "create", at = @At(value = "INVOKE",
            target = "Ljava/util/Collections;unmodifiableMap(Ljava/util/Map;)Ljava/util/Map;"), index = 0)
    private static Map<Object, List<Object>> gt$shareSingletonIndexes(Map<Object, List<Object>> index) {
        return GTCandidateIndexes.compactSingletons(index);
    }
}
