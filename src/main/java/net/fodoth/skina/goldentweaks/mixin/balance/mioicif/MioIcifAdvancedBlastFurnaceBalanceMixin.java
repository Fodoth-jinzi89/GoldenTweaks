package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.producer.mio_icif_blast_furnace_advanced;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 进阶高炉与基础高炉保持一致：热量缓冲 50,000 → 5,000 HU（保留 100 HU 进热余量，实际容量 5,100），
 * 配方时长直接使用数据包里缩短后的值（同时保留它自身的 4× 进度速度）。
 * 界面通过 {@code getHeatStored()}/{@code getHeatCapacity()} 取值，也一并改成 5,000。
 */
@Mixin(value = mio_icif_blast_furnace_advanced.class, remap = false)
public abstract class MioIcifAdvancedBlastFurnaceBalanceMixin {

    @Unique
    private static final long GT_HEAT_THRESHOLD = 5000L;
    @Unique
    private static final int GT_HEAT_CAPACITY = 5100;

    @ModifyConstant(
            method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntityType;)V",
            constant = @Constant(intValue = 50100))
    private static int gt$heatStorageCapacity(int original) {
        return GT_HEAT_CAPACITY;
    }

    @ModifyConstant(method = "heatup", constant = @Constant(longValue = 50000L))
    private long gt$heatupThreshold(long original) {
        return GT_HEAT_THRESHOLD;
    }

    @ModifyConstant(method = "isHot", constant = @Constant(longValue = 50000L))
    private long gt$hotThreshold(long original) {
        return GT_HEAT_THRESHOLD;
    }

    @ModifyConstant(method = "getHeatStored", constant = @Constant(longValue = 50000L))
    private long gt$displayedHeat(long original) {
        return GT_HEAT_THRESHOLD;
    }

    @ModifyConstant(method = "getHeatCapacity", constant = @Constant(intValue = 50000))
    private int gt$displayedCapacity(int original) {
        return (int) GT_HEAT_THRESHOLD;
    }
}
