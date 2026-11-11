package net.fodoth.skina.goldentweaks.compat.thermal;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

/**
 * 「组件：骤冷析晶程序」：急速冷冻机的专业化升级，只处理浓缩金属冷却结晶，耗能 25%、产出 200%。
 *
 * <p>配方筛选与数值改写见 {@link RefineryRecipeWrapper} 与 {@code MachineChillerRareMetalMixin}。</p>
 */
public class RareMetalCrystallizationAugmentItem extends GTSpecializationAugmentItem {

    public RareMetalCrystallizationAugmentItem(Item.Properties properties, CompoundTag augmentData) {
        super(properties, augmentData, "info.thermal.augment.rare_metal_crystallization.", ChatFormatting.GOLD);
    }
}
