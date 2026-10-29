package net.fodoth.skina.goldentweaks.mixin.fix.forbiddenmagic;

import com.spitefulfox.forbiddenmagic.common.ForbiddenEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * 苦痛项圈一次受伤会产生 {@code 3 * 伤害} 点 centivis，随机分摊到六大原质上（1 vis = 100 centivis）。
 * 不做限制时，一次超大伤害（例如 {@code /kill}、异常伤害源）会直接把整个项圈灌满。
 *
 * <p>这里把单次受伤每个原质的获取量钳制在 10 点 vis 以内。</p>
 */
@Mixin(value = ForbiddenEvents.class, remap = false)
public abstract class ForbiddenEventsPainCollarMixin {

    @ModifyArg(
            method = "chargePainCollar(Lnet/minecraft/world/item/ItemStack;FZLnet/minecraft/util/RandomSource;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/vis/TCVisHelper;addRealVis(Lnet/minecraft/world/item/ItemStack;Lthaumcraft/api/aspects/Aspect;IIZ)I"
            ),
            index = 2
    )
    private static int goldentweaks$capPerHit(int amount) {
        // 10 vis = 1000 centivis
        return Math.min(amount, 1000);
    }
}
