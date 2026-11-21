package net.fodoth.skina.goldentweaks.mixin.balance.flavorimmerseddailyargo;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.flavor_immersed_daily_argo.herding.AnimalSpawning", remap = false)
public abstract class AnimalSpawningMixin {

    @Inject(method = "trySpawnWater", at = @At("HEAD"), cancellable = true)
    private static void gt$scaleAquaticSpawning(@NotNull ServerLevel level, @NotNull ServerPlayer player,
                                               boolean saltwater, @NotNull CallbackInfo ci) {
        double chance = GoldenTweaksCommonConfig.getArgoAquaticSpawnChance();
        if (chance <= 0.0D || (chance < 1.0D && level.getRandom().nextDouble() >= chance)) {
            ci.cancel();
        }
    }
}
