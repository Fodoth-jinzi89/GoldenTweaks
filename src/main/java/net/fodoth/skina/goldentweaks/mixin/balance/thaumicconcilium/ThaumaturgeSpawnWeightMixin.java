package net.fodoth.skina.goldentweaks.mixin.balance.thaumicconcilium;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lets a pack set the ordinary thaumaturge spawn weight from GoldenTweaks' config.
 *
 * <p>The mod already exposes the weight in its own file, so the point is not to add a knob but to
 * put it where a pack tunes everything else. Redirecting the config read keeps the rest of the
 * biome modifier intact — the biome blacklist, the "weight of zero disables it" early exit and the
 * 1-to-3 group size all still run as written.</p>
 */
@Mixin(targets = "com.ilya3point999k.thaumicconcilium.common.entities.mobs.thaumaturge.ThaumaturgeModule$Spawns",
        remap = false)
public abstract class ThaumaturgeSpawnWeightMixin {

    @Redirect(
            method = "modify",
            at = @At(value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/ModConfigSpec$IntValue;get()Ljava/lang/Object;"),
            remap = false)
    private Object gt$overrideSpawnWeight(ModConfigSpec.IntValue self) {
        int override = GoldenTweaksCommonConfig.getThaumaturgeSpawnWeightOverride();
        // Negative leaves the mod's own value in place, so a pack that never touches the new option
        // gets exactly the behaviour it had before.
        return override < 0 ? self.get() : override;
    }
}
