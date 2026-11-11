package net.fodoth.skina.goldentweaks.compat.thermal;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 专业化升级的共用判定：一台机器只让升级槽位里最先出现的那一个专业化组件生效。
 */
public final class ThermalSpecializations {

    private ThermalSpecializations() {
    }

    /** 给定的专业化组件是否是该机器上第一个（因而唯一生效的）专业化升级。 */
    public static boolean isActive(List<ItemStack> augments, Item specialization) {
        for (ItemStack stack : augments) {
            if (stack.getItem() instanceof GTSpecializationAugment) {
                return stack.is(specialization);
            }
        }
        return false;
    }
}
