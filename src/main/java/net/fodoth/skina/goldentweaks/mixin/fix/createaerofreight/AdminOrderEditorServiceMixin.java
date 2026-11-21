package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.admin.AdminOrderEditorService;
import cn.blockforge.generated.createareofreight.infrastructure.config.RewardPoolFileConfig;
import net.fodoth.skina.goldentweaks.compat.createaerofreight.GTRewardItemGuard;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = AdminOrderEditorService.class, remap = false)
public abstract class AdminOrderEditorServiceMixin {

    @ModifyConstant(method = "readList", constant = @Constant(intValue = 512))
    private static int gt$removeEntryLimit(int original) {
        return Integer.MAX_VALUE;
    }

    @Redirect(method = "snapshot", at = @At(value = "INVOKE",
            target = "Lcn/blockforge/generated/createareofreight/infrastructure/config/RewardPoolFileConfig$Snapshot;generalRaw()Ljava/util/List;"))
    private static List<String> gt$validGeneralRewards(RewardPoolFileConfig.Snapshot snapshot) {
        return GTRewardItemGuard.filter(snapshot.generalRaw(), false);
    }

    @Redirect(method = "snapshot", at = @At(value = "INVOKE",
            target = "Lcn/blockforge/generated/createareofreight/infrastructure/config/RewardPoolFileConfig;flattenedSpecialRaw()Ljava/util/List;"))
    private static List<String> gt$validSpecialRewards() {
        return GTRewardItemGuard.filter(RewardPoolFileConfig.flattenedSpecialRaw(), true);
    }

    @Inject(method = "readList", at = @At("RETURN"), cancellable = true)
    private static void gt$filterSubmittedRewards(CompoundTag payload, String key, CallbackInfoReturnable<List<String>> cir) {
        if ("generalRewards".equals(key) || "specialRewards".equals(key)) {
            cir.setReturnValue(GTRewardItemGuard.filter(cir.getReturnValue(), "specialRewards".equals(key)));
        }
    }
}
