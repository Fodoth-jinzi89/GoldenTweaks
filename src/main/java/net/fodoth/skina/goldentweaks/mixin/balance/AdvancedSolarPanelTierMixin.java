package net.fodoth.skina.goldentweaks.mixin.balance;

import fr.iglee42.emgenerators.tiers.AdvancedSolarPanelTier;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancedSolarPanelTier.class)
public class AdvancedSolarPanelTierMixin {

    @Inject(method = "getMultiplier", at = @At("HEAD"), cancellable = true)
    private void goldenTweaks$adjustMultiplier(CallbackInfoReturnable<Integer> cir) {

        AdvancedSolarPanelTier tier = (AdvancedSolarPanelTier) (Object) this;

        int base;

        switch (tier) {
            case ADVANCED -> base = 2;
            case ELITE -> base = 3;
            case ULTIMATE -> base = 4;
            case OVERCLOCKED -> base = 5;
            case QUANTUM -> base = 6;
            case DENSE -> base = 7;
            case MULTIVERSAL -> base = 8;
            case CREATIVE -> base = 16;
            default -> {
                return;
            }
        }

        int boost = GoldenTweaksCommonConfig.EVOLVED_MEKANISM_SOLAR_MULTIPLIER.get();

        cir.setReturnValue(base * boost);
    }
}