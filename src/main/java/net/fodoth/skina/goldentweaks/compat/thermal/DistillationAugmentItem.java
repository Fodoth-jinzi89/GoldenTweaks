package net.fodoth.skina.goldentweaks.compat.thermal;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

/**
 * 「组件：塔板分馏提取」：流体精炼机的专业化升级，只处理浓缩金属分馏，耗能降至 25%、产出提升至 200%。
 *
 * <p>配方筛选与数值改写见 {@link ConcentratedRefineryRecipe} 与 {@code MachineRefineryDistillationMixin}。</p>
 */
public class DistillationAugmentItem extends GTSpecializationAugmentItem {

    public DistillationAugmentItem(Item.Properties properties, CompoundTag augmentData) {
        super(properties, augmentData, "info.thermal.augment.distillation.", ChatFormatting.GOLD);
    }
}
