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
            case ADVANCED -> base = 8;
            case ELITE -> base = 24;
            case ULTIMATE -> base = 72;
            case OVERCLOCKED -> base = 216;
            case QUANTUM -> base = 648;
            case DENSE -> base = 1944;
            case MULTIVERSAL -> base = 5832;
            case CREATIVE -> base = 17496;
            default -> {
                return;
            }
        }

        int boost = GoldenTweaksCommonConfig.getEmSolarMultiplier();

        cir.setReturnValue(base * boost);
    }
}