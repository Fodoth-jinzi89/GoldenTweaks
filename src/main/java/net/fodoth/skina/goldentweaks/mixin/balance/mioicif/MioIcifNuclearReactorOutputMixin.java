package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.miophas.singularity_iteration.common.blockentity.generator.mio_icif_nuclear_reactor_generator;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 核反应堆产能倍率（默认 ×10，开关默认开）。
 *
 * <ul>
 *   <li>产能：{@code operate()} 里 {@code euSum * GeneratorOutput.multiplier(this)} 的内部倍率再乘本模组倍率。</li>
 *   <li>储能/输出上限：构造器传给父类的容量与最大抽取、以及恢复存档时重设的 8192 输出，一并乘倍率。</li>
 *   <li>界面与对外口径：{@code potentialEnergy} / {@code prepareEnergy} / {@code getOfferedEnergy} /
 *       {@code extractPowerForConsumer} / {@code getPowerOutput} 里的 1,000,000 硬编码同样乘倍率，
 *       否则产出会立刻撞上旧上限而被截断。</li>
 * </ul>
 *
 * <p>倍率关闭时 {@code getEffectiveMioIcifReactorMultiplier()} 返回 1，所有调用点自然退回原版。</p>
 */
@Mixin(value = mio_icif_nuclear_reactor_generator.class, remap = false)
public abstract class MioIcifNuclearReactorOutputMixin {

    @WrapOperation(
            method = "operate",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/miophas/singularity_iteration/common/energy/GeneratorOutput;multiplier(Lnet/minecraft/world/level/block/entity/BlockEntity;)D"))
    private static double gt$scaleReactorOutput(BlockEntity blockEntity, Operation<Double> original) {
        return original.call(blockEntity) * GoldenTweaksCommonConfig.getEffectiveMioIcifReactorMultiplier();
    }

    @ModifyConstant(
            method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntityType;)V",
            constant = @Constant(longValue = 1000000L))
    private static long gt$scaleReactorCapacity(long original) {
        return original * GoldenTweaksCommonConfig.getEffectiveMioIcifReactorMultiplier();
    }

    @ModifyConstant(
            method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntityType;)V",
            constant = @Constant(longValue = 8192L))
    private static long gt$scaleReactorPacket(long original) {
        return original * GoldenTweaksCommonConfig.getEffectiveMioIcifReactorMultiplier();
    }

    @ModifyConstant(method = "loadAdditional", constant = @Constant(longValue = 8192L))
    private long gt$scaleReloadedPacket(long original) {
        return original * GoldenTweaksCommonConfig.getEffectiveMioIcifReactorMultiplier();
    }

    @ModifyConstant(method = "potentialEnergy", constant = @Constant(longValue = 1000000L))
    private long gt$scalePotentialEnergy(long original) {
        return original * GoldenTweaksCommonConfig.getEffectiveMioIcifReactorMultiplier();
    }

    @ModifyConstant(method = "prepareEnergy", constant = @Constant(longValue = 1000000L))
    private long gt$scalePreparedEnergy(long original) {
        return original * GoldenTweaksCommonConfig.getEffectiveMioIcifReactorMultiplier();
    }

    @ModifyConstant(method = "getOfferedEnergy", constant = @Constant(doubleValue = 1000000.0))
    private double gt$scaleOfferedEnergy(double original) {
        return original * GoldenTweaksCommonConfig.getEffectiveMioIcifReactorMultiplier();
    }

    @ModifyConstant(method = "extractPowerForConsumer", constant = @Constant(longValue = 1000000L))
    private long gt$scaleExtractedPower(long original) {
        return original * GoldenTweaksCommonConfig.getEffectiveMioIcifReactorMultiplier();
    }

    @ModifyConstant(method = "getPowerOutput", constant = @Constant(longValue = 1000000L))
    private long gt$scalePowerOutput(long original) {
        return original * GoldenTweaksCommonConfig.getEffectiveMioIcifReactorMultiplier();
    }
}
