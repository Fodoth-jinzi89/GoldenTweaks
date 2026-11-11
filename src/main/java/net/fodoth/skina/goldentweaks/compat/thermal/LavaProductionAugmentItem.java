package net.fodoth.skina.goldentweaks.compat.thermal;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

/**
 * 「组件：地核能量熔融」：熔岩炉的专业化升级，只运行产出熔岩的配方，耗能 10%、产出 200%。
 *
 * <p>配方筛选与数值改写见 {@link RefineryRecipeWrapper} 与 {@code MachineCrucibleLavaMixin}。</p>
 */
public class LavaProductionAugmentItem extends GTSpecializationAugmentItem {

    public LavaProductionAugmentItem(Item.Properties properties, CompoundTag augmentData) {
        super(properties, augmentData, "info.thermal.augment.lava_production.", ChatFormatting.GOLD);
    }
}
