package net.fodoth.skina.goldentweaks.mixin.fix.mioicif;

import com.miophas.singularity_iteration.common.armory.ArmoryRules;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 武装库（{@code mio_icif:producer/block_armory}）与全息投影展示台（{@code block_armor_showcase}）
 * 原先把"可收纳装备"限制为 {@code minecraft} / {@code mio_icif} 命名空间（其它模组的装备必须先用
 * 连接器套件打上 {@code ArmoryLink} 组件），导致绝大多数模组的护甲/武器放不进去。
 *
 * <p>{@link ArmoryRules#isNative} 是 {@code isSummonable} → {@code fits} → 槽位校验的源头。这里放宽为
 * 接受任意非空物品：护甲槽仍由 {@code fits()} 按 {@code EquipmentSlot} 匹配（只有真正穿得上的护甲能进），
 * 主/副手槽则允许任意手持物品。连接器套件随之变成"原生支持"，不再需要。</p>
 */
@Mixin(value = ArmoryRules.class, remap = false)
public abstract class MioIcifArmoryRulesMixin {

    @Inject(method = "isNative", at = @At("HEAD"), cancellable = true)
    private static void gt$acceptAnyEquipment(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty()) {
            cir.setReturnValue(true);
        }
    }
}
