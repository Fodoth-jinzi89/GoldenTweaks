package net.fodoth.skina.goldentweaks.compat.thermal;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

/**
 * 「组件：精密炼化分馏」：流体精炼机的专业化升级，只处理油类炼化配方，耗能提升至 200%、产出提升至 400%。
 *
 * <p>配方筛选与数值改写见 {@link RefineryRecipeWrapper} 与 {@code MachineRefineryDistillationMixin}。</p>
 */
public class OilDistillationAugmentItem extends GTSpecializationAugmentItem {

    public OilDistillationAugmentItem(Item.Properties properties, CompoundTag augmentData) {
        super(properties, augmentData, "info.thermal.augment.oil_distillation.", ChatFormatting.GOLD);
    }
}
