package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 核反应堆界面的储能上限来自方块实体里匿名 {@code ContainerData} 的 {@code get(1)}，
 * 那里硬编码返回 1,000,000（而不是读 {@code energyStorage.getCapacity()}）。
 * 倍率放大后同步放大，界面能源条才不会一进世界就显示满格。
 */
@Mixin(targets = "com.miophas.singularity_iteration.common.blockentity.generator.mio_icif_nuclear_reactor_generator$1", remap = false)
public abstract class MioIcifNuclearReactorMenuDataMixin {

    @ModifyConstant(method = "get", constant = @Constant(intValue = 1000000))
    private static int gt$menuEnergyCapacity(int original) {
        return original * GoldenTweaksCommonConfig.getEffectiveMioIcifReactorMultiplier();
    }
}
