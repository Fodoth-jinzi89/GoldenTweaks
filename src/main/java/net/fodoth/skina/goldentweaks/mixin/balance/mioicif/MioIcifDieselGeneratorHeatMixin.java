package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.miophas.singularity_iteration.common.blockentity.generator.mio_icif_diesel_generator;
import com.miophas.singularity_iteration.common.energy.GeneratorOutput;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * 柴油热值 ×10，输出上限对齐 EV（1A EV = 2048 EU/t）。
 *
 * <ul>
 *   <li>每 16 mB 柴油的发热量：120 → 1200 EU（75 EU/mB）。</li>
 *   <li>发电机输出上限：120 → 2048 EU/t；同时把额定输出、最大抽取也放宽到 2048，
 *       否则 SI 电网/兼容输出仍会按旧的 120/512 截断。</li>
 *   <li>燃料信用上限从 120 提到 1200，避免高热值余量读档时被截断。</li>
 * </ul>
 */
@Mixin(value = mio_icif_diesel_generator.class, remap = false)
public abstract class MioIcifDieselGeneratorHeatMixin {

    private static final long GT_HEAT_PER_UNIT = 1200L;
    private static final long GT_EV_OUTPUT = 2048L;

    @ModifyArgs(
            method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntityType;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/miophas/singularity_iteration/core/prefab/blockentity/GenericGeneratorBlockEntity;<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntityType;Lcom/miophas/singularity_iteration/core/prefab/inventory/SlotLayout;JJJJLcom/miophas/singularity_iteration/core/api/energy/ICableTier;)V"
            )
    )
    private static void gt$raiseDieselCaps(Args args) {
        args.set(4, GT_EV_OUTPUT);
        args.set(7, GT_EV_OUTPUT);
    }

    @WrapOperation(
            method = "scexTickFuel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/miophas/singularity_iteration/common/energy/GeneratorOutput;rate(Lnet/minecraft/world/level/block/entity/BlockEntity;J)J",
                    ordinal = 0
            )
    )
    private static long gt$fuelEnergyPerUnit(BlockEntity blockEntity, long rate, Operation<Long> original) {
        return original.call(blockEntity, GT_HEAT_PER_UNIT);
    }

    @WrapOperation(
            method = "scexTickFuel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/miophas/singularity_iteration/common/energy/GeneratorOutput;rate(Lnet/minecraft/world/level/block/entity/BlockEntity;J)J",
                    ordinal = 1
            )
    )
    private static long gt$outputRate(BlockEntity blockEntity, long rate, Operation<Long> original) {
        return original.call(blockEntity, GT_EV_OUTPUT);
    }

    @ModifyConstant(method = "scexReadFuelCredit", constant = @Constant(longValue = 120L))
    private static long gt$fuelCreditCap(long original) {
        return GT_HEAT_PER_UNIT;
    }
}
