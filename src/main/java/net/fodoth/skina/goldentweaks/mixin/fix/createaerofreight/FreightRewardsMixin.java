package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.FreightRewards;
import cn.blockforge.generated.createareofreight.FreightRewardsConfig;
import cn.blockforge.generated.createareofreight.api.reward.RewardEntry;
import java.util.List;
import net.fodoth.skina.goldentweaks.compat.createaerofreight.GTRewardSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FreightRewards.class, remap = false)
public abstract class FreightRewardsMixin {
    @Inject(method = "chooseRewards", at = @At("HEAD"), cancellable = true)
    private static void gt$choose(String cargoType, long seed, int count, CallbackInfoReturnable<List<RewardEntry>> cir) {
        cir.setReturnValue(GTRewardSelection.choose(FreightRewardsConfig.poolFor(cargoType), seed));
    }

    @ModifyVariable(method = "chooseChaosRewards", at = @At("HEAD"), argsOnly = true)
    private static int gt$chaosCount(int count, long seed, int originalCount) {
        return GTRewardSelection.specialtyCount(seed);
    }

    @Redirect(method = "rewardAmounts", at = @At(value = "INVOKE", target = "Lcn/blockforge/generated/createareofreight/api/reward/RewardEntry;quantityWeight()D", ordinal = 0))
    private static double gt$generalQuantity(RewardEntry entry) {
        return entry.quantityWeight() * 4.0;
    }

    @Redirect(method = "rewardAmounts", at = @At(value = "INVOKE", target = "Lcn/blockforge/generated/createareofreight/api/reward/RewardEntry;quantityWeight()D", ordinal = 1))
    private static double gt$fixedSpecialtyQuantity(RewardEntry entry) {
        return entry.quantityWeight() * 0.3;
    }

    @Redirect(method = "rewardAmounts", at = @At(value = "INVOKE", target = "Lcn/blockforge/generated/createareofreight/api/reward/RewardEntry;quantityWeight()D", ordinal = 2))
    private static double gt$specialtyQuantity(RewardEntry entry) {
        return entry.quantityWeight() * 0.3;
    }

    @Inject(method = "rewardAmounts", at = @At("RETURN"), cancellable = true)
    private static void gt$order(int scaledCount, double timeMultiplier, boolean late, List<RewardEntry> specialties,
            boolean fixedRewards, CallbackInfoReturnable<List<FreightRewards.RewardAmount>> cir) {
        cir.setReturnValue(GTRewardSelection.specialtiesFirst(cir.getReturnValue(), FreightRewardsConfig.generalRewards().size()));
    }
}
