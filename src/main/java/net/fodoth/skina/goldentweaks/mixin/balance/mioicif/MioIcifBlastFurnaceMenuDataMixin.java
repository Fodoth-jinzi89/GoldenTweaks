package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.producer.mio_icif_blast_furnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 基础高炉界面的热量上限来自方块实体里匿名 {@code ContainerData} 的 {@code get(1)}，
 * 那里硬编码返回 50,000（而不是调用 {@code getHeatCapacity()}）。缓冲改成 5,000 后
 * 界面仍显示 xxx/50000 HU，这里同步改成 5,000，并把显示值夹在 5,000 以内
 * （实际存储保留 100 HU 的进热余量，避免刚好掉到阈值以下停火）。
 */
@Mixin(targets = "com.miophas.singularity_iteration.common.blockentity.producer.mio_icif_blast_furnace$5", remap = false)
public abstract class MioIcifBlastFurnaceMenuDataMixin {

    @Unique
    private static final long GT_HEAT_DISPLAY_MAX = 5000L;

    @ModifyConstant(method = "get", constant = @Constant(intValue = 50000))
    private static int gt$menuHeatCapacity(int original) {
        return (int) GT_HEAT_DISPLAY_MAX;
    }

    @Redirect(
            method = "get",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/miophas/singularity_iteration/common/blockentity/producer/mio_icif_blast_furnace;getHeatStored()J"))
    private static long gt$clampDisplayedHeat(mio_icif_blast_furnace furnace) {
        return Math.min(furnace.getHeatStored(), GT_HEAT_DISPLAY_MAX);
    }
}
