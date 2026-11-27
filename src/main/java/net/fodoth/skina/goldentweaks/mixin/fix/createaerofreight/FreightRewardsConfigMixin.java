package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.FreightRewardsConfig;
import cn.blockforge.generated.createareofreight.api.reward.RewardEntry;
import cn.blockforge.generated.createareofreight.infrastructure.config.RewardPoolFileConfig;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FreightRewardsConfig.class, remap = false)
public abstract class FreightRewardsConfigMixin {
    @Redirect(method = "poolFor", at = @At(value = "FIELD", target = "Lcn/blockforge/generated/createareofreight/FreightRewardsConfig;EMERGENCY_POOL:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    private static List<RewardEntry> gt$noEmergencyIron() {
        return List.of();
    }

    // Existing installations may already have the upstream default.toml iron fallback.
    @Inject(method = "poolFor", at = @At("RETURN"), cancellable = true)
    private static void gt$noDefaultIron(String poolId, CallbackInfoReturnable<List<RewardEntry>> cir) {
        List<RewardEntry> rewards = cir.getReturnValue();
        List<RewardEntry> configured = RewardPoolFileConfig.current().pools().get(poolId == null ? "default" : poolId);
        boolean usesDefault = poolId == null || poolId.isBlank() || "default".equals(poolId)
                || configured == null || configured.isEmpty();
        if (usesDefault && rewards.size() == 1 && "minecraft:iron_nugget".equals(rewards.getFirst().itemId())) {
            cir.setReturnValue(List.of());
        }
    }
}
