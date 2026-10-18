package net.fodoth.skina.goldentweaks.mixin.fix.mek;

import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 聚变反应堆的水/蒸汽储罐容量 {@code maxWater}/{@code maxSteam} 等于
 * {@code injectionRate × 配置的 perInjection}，但这两个字段只在 {@code setInjectionRate} 里赋值，
 * 而该方法在「速率没有变化」时会直接 return：构造函数把 injectionRate 写死成 2，
 * FusionReactorCache 的默认速率（-1）同样解析成 2。
 * <p>
 * 结果就是刚建好 / 重新加载后的反应堆 maxWater、maxSteam 一直是 0，流体储罐容量为 0，
 * 水插不进去（上游 issue mekanism/Mekanism#8281，临时办法是手动改一次燃烧速率）。
 * 这里在多方块成型时、以及每次 setInjectionRate 返回后强制重算一次容量。
 */
@Mixin(value = FusionReactorMultiblockData.class, remap = false)
public abstract class FusionReactorWaterCapacityMixin {

    @Shadow(remap = false)
    private int injectionRate;

    @Shadow(remap = false)
    private int maxWater;

    @Shadow(remap = false)
    private long maxSteam;

    @Inject(method = "onCreated", at = @At("TAIL"), remap = false)
    private void gt$recomputeTankCapacity(Level level, CallbackInfo ci) {
        gt$applyTankCapacity();
    }

    @Inject(method = "setInjectionRate", at = @At("RETURN"), remap = false)
    private void gt$recomputeTankCapacityAfterRate(int rate, CallbackInfo ci) {
        gt$applyTankCapacity();
    }

    @Unique
    private void gt$applyTankCapacity() {
        this.maxWater = this.injectionRate * MekanismGeneratorsConfig.generators.fusionWaterPerInjection.get();
        this.maxSteam = this.injectionRate * MekanismGeneratorsConfig.generators.fusionSteamPerInjection.get();
    }
}
