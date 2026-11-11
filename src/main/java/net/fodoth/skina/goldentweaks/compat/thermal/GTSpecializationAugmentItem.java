package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.lib.util.helpers.StringHelper;
import cofh.thermal.lib.common.item.AugmentItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * 专业化升级组件的通用物品：名称是「组件：&lt;短代号&gt;」，提示首行是「&lt;机器&gt;：专业化」（金色），
 * 再用 绿(a)/黄(b)/红(c) 分色要点列出说明。
 *
 * <p>1.12.2 Thermal Expansion 是把若干条 {@code info.thermalexpansion.augment.<id>.<i>} 的短句直接挂上去；
 * 这里沿用同样的分色约定，只是把现代 Thermal 那套「类型 + 属性加成」的机制式提示在客户端侧跳过
 * （见 {@code ThermalAugmentTooltipMixin}）。</p>
 */
public abstract class GTSpecializationAugmentItem extends AugmentItem implements GTSpecializationAugment {

    private final String prefix;
    private final ChatFormatting titleColor;

    protected GTSpecializationAugmentItem(Item.Properties properties, CompoundTag augmentData,
                                          String prefix, ChatFormatting titleColor) {
        super(properties, augmentData);
        this.prefix = prefix;
        this.titleColor = titleColor;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(prefix + "0").withStyle(titleColor));
        addBullets(tooltip, "a", ChatFormatting.GREEN);
        addBullets(tooltip, "b", ChatFormatting.YELLOW);
        addBullets(tooltip, "c", ChatFormatting.RED);
    }

    /** 1.12.2 的 {@code .a}/{@code .b}/{@code .c} 是连续编号；键缺了就停。 */
    private void addBullets(List<Component> tooltip, String group, ChatFormatting color) {
        for (int i = 0; ; i++) {
            String key = prefix + group + "." + i;
            if (!StringHelper.canLocalize(key)) {
                return;
            }
            tooltip.add(StringHelper.getTextComponent(key).withStyle(color));
        }
    }
}
