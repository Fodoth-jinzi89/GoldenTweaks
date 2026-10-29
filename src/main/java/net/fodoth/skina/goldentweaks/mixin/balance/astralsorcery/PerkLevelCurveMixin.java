package net.fodoth.skina.goldentweaks.mixin.balance.astralsorcery;

import net.fodoth.skina.goldentweaks.compat.astralsorcery.GTPerkLevelCurve;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * 把星辉魔法的星能力升级经验表换成平滑曲线，详见 {@link GTPerkLevelCurve}。
 *
 * <p>{@code buildLevelRequirements} 在 {@code LevelData} 构造时调用：服务端由
 * {@code initializeServerLevels} 触发，客户端由同步包 {@code PktSyncPerkLevels} 带着服务端的
 * 等级上限触发，两端都会走到，所以这里不需要区分侧别。</p>
 */
@Mixin(targets = "hellfirepvp.astralsorcery.common.perk.PerkLevelManager$LevelData", remap = false)
public abstract class PerkLevelCurveMixin {

    @Shadow
    @Final
    private Map<Integer, Long> totalExpLevelRequired;

    @Shadow
    @Final
    private int maxLevel;

    @Inject(method = "buildLevelRequirements", at = @At("TAIL"), remap = false)
    private void gt$applySmoothPerkCurve(CallbackInfo ci) {

        if (!GoldenTweaksCommonConfig.isAstralSorcerySmoothPerkCurve()) {
            return;
        }

        GTPerkLevelCurve.fill(totalExpLevelRequired, maxLevel);
    }
}
