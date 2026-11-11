package net.fodoth.skina.goldentweaks.compat.thermal;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

/**
 * 结晶器的专业化升级组件。名称与提示版式照 1.12.2 Thermal Expansion 的 {@code ItemAugment}：
 * 名字是「组件：&lt;短代号&gt;」，提示首行是「&lt;机器&gt;：专业化」，再用 绿(a)/黄(b)/红(c) 分色要点列出说明。
 */
public class CrystallarieumAugmentItem extends GTSpecializationAugmentItem {

    public CrystallarieumAugmentItem(Item.Properties properties, CompoundTag augmentData) {
        super(properties, augmentData, "info.thermal.augment.crystallarieum.", ChatFormatting.GOLD);
    }
}
