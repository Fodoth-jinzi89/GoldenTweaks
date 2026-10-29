package net.fodoth.skina.goldentweaks.mixin.fix.spectrum;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.dafuqs.spectrum.registries.SpectrumEventListeners;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 光谱的 {@code onLivingIncomingDamage} 在处理「分裂伤害」（{@code SplitDamageItem}）时，
 * 直接对 {@code DamageSource.getWeaponItem()} 调 {@code getItem()} 来判类型，
 * 而该方法在没有武器来源时返回的是 {@code null}（例如学院技能、
 * {@code AcademyDamageHelper.apply} 这类直接构造的伤害源）。任何一次这样的命中都会让
 * 事件分发线程抛 {@code NullPointerException}，随后连带刷出整张
 * {@code LivingIncomingDamageEvent} 监听器 dump。
 *
 * <p>这里把两个 {@code getWeaponItem()} 取值点统一兜成 {@link ItemStack#EMPTY}：
 * 判空后走到「不是 SplitDamageItem」的正常分支，既不再 NPE，也不改变有武器时的行为。</p>
 */
@Mixin(SpectrumEventListeners.class)
public class SpectrumEventListenersMixin {

    @ModifyExpressionValue(
            method = "onLivingIncomingDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;getWeaponItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack gt$nullSafeWeaponItem(ItemStack original) {
        return original == null ? ItemStack.EMPTY : original;
    }
}
