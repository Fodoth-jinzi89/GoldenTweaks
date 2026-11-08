package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.producer.mio_icif_blast_furnace_elc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.Unique;

/**
 * 电力纳米高炉平衡：所有配方统一 20 tick（原 40 tick），EU/t 与容量不变。
 * 同时把机器配置里的展示时长改成 20，避免 JEI/界面仍显示 40。
 */
@Mixin(value = mio_icif_blast_furnace_elc.class, remap = false)
public abstract class MioIcifBlastFurnaceElcBalanceMixin {

    @Unique
    private static final int GT_WORK_TIME = 20;

    @ModifyConstant(
            method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntityType;)V",
            constant = @Constant(intValue = 40))
    private static int gt$workTime(int original) {
        return GT_WORK_TIME;
    }

    @ModifyConstant(method = "buildMachineDefinition", constant = @Constant(intValue = 40))
    private static int gt$displayedWorkTime(int original) {
        return GT_WORK_TIME;
    }
}
