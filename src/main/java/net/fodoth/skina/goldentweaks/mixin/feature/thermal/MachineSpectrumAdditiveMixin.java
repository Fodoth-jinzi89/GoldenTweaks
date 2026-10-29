package net.fodoth.skina.goldentweaks.mixin.feature.thermal;

import cofh.thermal.lib.common.block.entity.MachineBlockEntity;
import cofh.thermal.lib.util.recipes.IMachineInventory;
import cofh.thermal.lib.util.recipes.internal.IMachineRecipe;
import net.fodoth.skina.goldentweaks.compat.thermal.SpectrumCrystallizerRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 结晶器跑光谱配方时的添加剂：照原版「每秒一次」的节奏，既决定生长加速、也按消耗概率吃掉添加剂。
 *
 * <p>原版 {@code CrystallarieumBlockEntity.tickRecipe} 由一个每秒触发的 looper 调用，每次给当前生长阶段推进
 * {@code 20 × growth_acceleration_mod} 个 ticks，并以 {@code consume_chance_per_second} 的概率消耗 1 个添加剂。
 * 这里把同样的行为搬到 Thermal 机器的每 tick 里：<b>加速</b>用放大 {@code processTick}（每 tick 进度 / 能耗）实现，
 * 于是总能耗不变、速度按倍率提升，且添加剂中途耗尽时倍率会立刻回落；<b>消耗</b>用一个每秒计数器做同样的概率判定。</p>
 *
 * <p>只对 {@link SpectrumCrystallizerRecipe} 生效，其它机器与配方完全不受影响。</p>
 */
@Mixin(value = MachineBlockEntity.class, remap = false)
public abstract class MachineSpectrumAdditiveMixin {

    @Shadow protected IMachineRecipe curRecipe;
    @Shadow protected int process;
    @Shadow protected int processMax;
    @Shadow protected int processTick;
    @Shadow protected int baseProcessTick;

    @Unique private int gt$spectrumAdditiveSecond;

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void gt$spectrumAdditive(CallbackInfo ci) {

        if (this.process <= 0 || !(this.curRecipe instanceof SpectrumCrystallizerRecipe recipe)) {
            this.gt$spectrumAdditiveSecond = 0;
            return;
        }

        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level == null) {
            return;
        }

        IMachineInventory inventory = (IMachineInventory) (Object) this;

        int accelerated = (int) (this.baseProcessTick * recipe.growthAcceleration(inventory));
        this.processTick = Math.max(1, Math.min(accelerated, this.processMax));

        if (++this.gt$spectrumAdditiveSecond >= 20) {
            this.gt$spectrumAdditiveSecond = 0;
            recipe.consumeAdditive(inventory, level.getRandom());
        }
    }
}
