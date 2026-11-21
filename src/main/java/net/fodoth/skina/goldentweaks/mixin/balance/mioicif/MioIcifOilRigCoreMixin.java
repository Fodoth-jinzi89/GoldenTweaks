package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.oilrig.mio_icif_oil_rig_core_entity;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 石油钻机无限钻探（普通钻机）：命中判定不再读取并破坏方块，恒定返回命中，且永不因钻完 45 列而停机。
 * 单次产出同时从 100 mB 提升到 1000 mB。以上行为受配置项控制，默认开启。
 */
@Mixin(value = mio_icif_oil_rig_core_entity.class, remap = false)
public abstract class MioIcifOilRigCoreMixin {

    @Shadow
    private BlockPos drillCoord;

    @Shadow
    private boolean isRigFinish;

    @Inject(method = "tryDrill", at = @At("HEAD"), cancellable = true)
    private void gt$infiniteDrill(Level level, BlockPos corePos, CallbackInfoReturnable<Boolean> cir) {
        if (!GoldenTweaksCommonConfig.isMioIcifOilRigInfinite()) {
            return;
        }
        this.drillCoord = corePos.below(2);
        cir.setReturnValue(true);
    }

    @ModifyConstant(method = "tryOperate", constant = @Constant(intValue = 100))
    private int gt$oilPerDrill(int original) {
        return GoldenTweaksCommonConfig.isMioIcifOilRigInfinite() ? 1000 : original;
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void gt$clearLegacyFinish(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (GoldenTweaksCommonConfig.isMioIcifOilRigInfinite()) {
            this.isRigFinish = false;
        }
    }
}
