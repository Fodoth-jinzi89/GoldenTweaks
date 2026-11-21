package net.fodoth.skina.goldentweaks.mixin.balance.mioicif.oei;

import net.fodoth.skina.goldentweaks.compat.oei.GTOeiItemEquivalence;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让原版的物品比较也识别 OEI 的统一映射：
 * <ul>
 *   <li>{@link ItemStack#isSameItem}：两边都收敛到统一目标后相同即可（与该方法忽略组件的语义一致）。</li>
 *   <li>{@link ItemStack#isSameItemSameComponents}：统一目标相同且显式组件 patch 相同才视为相同。</li>
 *   <li>{@code hashItemAndComponents}：同步收敛到统一目标，维持哈希/等价契约。</li>
 * </ul>
 *
 * <p>这样 SI 机器把「配方结果是来源物品、输出槽是统一后物品」判定为同一产物，不会在输出槽
 * 尚未真正塞满时断加工/熄灯。仅在同时安装 SI 与 OEI 时启用。</p>
 *
 * <p>创造模式标签构建期间整体跳过：OEI 此时刻意不做统一替换，标签会按原样收录同一矿物的多个
 * 变体；若仍按统一目标判等/哈希，NeoForge 事件与标签去重集合会把这些变体当成重复项抛异常。</p>
 */
@Mixin(ItemStack.class)
public abstract class MioIcifOeiItemEquivalenceMixin {

    @Inject(method = "isSameItem", at = @At("RETURN"), cancellable = true)
    private static void gt$isSameItemAfterReplacement(ItemStack first, ItemStack second,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (GTOeiItemEquivalence.isCreativeTabBuilding()) {
            return;
        }
        if (!Boolean.TRUE.equals(cir.getReturnValue()) && GTOeiItemEquivalence.isSameItem(first, second)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isSameItemSameComponents", at = @At("RETURN"), cancellable = true)
    private static void gt$isSameItemSameComponentsAfterReplacement(ItemStack first, ItemStack second,
                                                                    CallbackInfoReturnable<Boolean> cir) {
        if (GTOeiItemEquivalence.isCreativeTabBuilding()) {
            return;
        }
        if (!Boolean.TRUE.equals(cir.getReturnValue()) && GTOeiItemEquivalence.isSameItemSameComponents(first, second)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hashItemAndComponents", at = @At("HEAD"), cancellable = true)
    private static void gt$hashItemAndComponentsAfterReplacement(ItemStack stack,
                                                                 CallbackInfoReturnable<Integer> cir) {
        if (GTOeiItemEquivalence.isCreativeTabBuilding()) {
            return;
        }
        cir.setReturnValue(GTOeiItemEquivalence.hashItemAndComponents(stack));
    }
}
