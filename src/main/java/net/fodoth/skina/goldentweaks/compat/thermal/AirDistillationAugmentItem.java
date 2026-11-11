package net.fodoth.skina.goldentweaks.compat.thermal;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

/**
 * 「组件：冷冻空气分馏」：流体精炼机的专业化升级，只处理空气组分分馏配方。
 *
 * <p>配方筛选见 {@link RefineryRecipeWrapper} 与 {@code MachineRefineryDistillationMixin}。</p>
 */
public class AirDistillationAugmentItem extends GTSpecializationAugmentItem {

    public AirDistillationAugmentItem(Item.Properties properties, CompoundTag augmentData) {
        super(properties, augmentData, "info.thermal.augment.air_distillation.", ChatFormatting.GOLD);
    }
}
