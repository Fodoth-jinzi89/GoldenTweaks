package net.fodoth.skina.goldentweaks.mixin.fix.avaritia_more_items;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.neoforged.fml.ModList;
import net.weibai.avaritia_more_items.compat.ae2.ThaumicEnergisticsCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * avaritia_more_items 的 AE2 兼容在<b>模组构造阶段</b>就为「源质」这个 AEKeyType 注册了一份
 * {@code ContainerItemStrategy}，而 ThaumicEnergistics 自己在 {@code FMLCommonSetupEvent} 的延迟任务里
 * 又注册了同一份。AE2 的 {@code CowMap.putIfAbsent} 对「键已存在」直接抛异常，于是 TE 的延迟任务把
 * 整个启动打挂：
 *
 * <pre>
 * IllegalArgumentException: Map already contains a value for the following key: thaumicenergistics:essentia
 *   at appeng.api.behaviors.ContainerItemStrategy.register
 *   at thaumicenergistics.ThaumicEnergistics.lambda$commonSetup$0
 * </pre>
 *
 * <p>源质键类型归 ThaumicEnergistics 所有，它的实现本身就覆盖源质罐 / 源质安瓿等容器，因此 TE 在场时
 * 跳过 avaritia_more_items 这份重复注册，让 TE 自己的注册顺利完成；TE 不在时保持原样。</p>
 */
@Mixin(value = ThaumicEnergisticsCompat.class, remap = false)
public class AvaritiaMoreItemsEssentiaStrategyMixin {

    @Inject(
            method = "registerEssentiaContainerStrategy",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$skipWhenThaumicEnergisticsPresent(CallbackInfo ci) {
        if (ModList.get().isLoaded("thaumicenergistics")) {
            GoldenTweaks.LOGGER.warn(
                    "[GT] Skipped avaritia_more_items' duplicate essentia container strategy; "
                            + "ThaumicEnergistics registers its own");
            ci.cancel();
        }
    }
}
