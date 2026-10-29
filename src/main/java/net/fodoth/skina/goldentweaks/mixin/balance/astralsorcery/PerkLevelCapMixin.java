package net.fodoth.skina.goldentweaks.mixin.balance.astralsorcery;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 星辉魔法的星能力（perk）点数上限写死在它自己的服务端配置 {@code perks.perkLevelCap} 里
 * （默认 30、范围 1–100），而星能力图谱有 364 个节点，默认上限点不满整张图谱。
 *
 * <p>{@code PerkLevelManager#initializeServerLevels} 是服务端等级表的唯一构建入口，
 * 客户端等级由 {@code PktSyncPerkLevels} 跟随服务端下发，所以只改这里即可双端一致。</p>
 *
 * <p>这里只抬不降：取星辉魔法自身配置值与 GT 配置值的较大者，GT 配置默认 364（图谱全部节点数）。</p>
 */
@Mixin(targets = "hellfirepvp.astralsorcery.common.perk.PerkLevelManager", remap = false)
public abstract class PerkLevelCapMixin {

    @ModifyExpressionValue(
            method = "initializeServerLevels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/ModConfigSpec$IntValue;getAsInt()I"
            ),
            remap = false
    )
    private int gt$enhancePerkLevelCap(int original) {
        return Math.max(original, GoldenTweaksCommonConfig.getAstralSorceryPerkPointCap());
    }
}
