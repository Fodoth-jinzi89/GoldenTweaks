package net.fodoth.skina.goldentweaks.compat.createoreexcavation;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 在钻井物品上说明 GoldenTweaks 的「矿脉分享」机制。
 *
 * <p>数值直接取当前配置，所以玩家改完配置就能在物品提示里看到生效值。
 * 颜色对应 {@code &6} 标题 / {@code &e} 说明行 / {@code &a} 增益数值 / {@code &b} 上限数值。</p>
 */
public final class GTCreateOreExcavationTooltip {

    private static final String KEY = "tooltip.goldentweaks.createoreexcavation.vein_sharing";

    /** 会受到矿脉分享影响的机器；用注册名匹配，避免类加载时链接到未安装的模组。 */
    private static final Set<String> SHARING_ITEMS = Set.of(
            "createoreexcavation:drilling_machine",
            "createoreexcavation:extractor"
    );

    private GTCreateOreExcavationTooltip() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!SHARING_ITEMS.contains(id.toString())) {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(KEY).withStyle(ChatFormatting.GOLD));

        if (!GoldenTweaksCommonConfig.isOreVeinSharingEnabled()) {
            tooltip.add(Component.translatable(KEY + ".disabled").withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable(KEY + ".prefix").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable(KEY + ".speed",
                percent(GoldenTweaksCommonConfig.getOreVeinSpeedBonus(), "-", ChatFormatting.GREEN),
                percent(GoldenTweaksCommonConfig.getOreVeinSpeedFloor(), "", ChatFormatting.AQUA)
        ));
        tooltip.add(Component.translatable(KEY + ".yield",
                percent(GoldenTweaksCommonConfig.getOreVeinYieldBonus(), "+", ChatFormatting.GREEN),
                percent(GoldenTweaksCommonConfig.getOreVeinDoubleOutputCap(), "", ChatFormatting.AQUA)
        ));
    }

    private static Component percent(double fraction, String sign, ChatFormatting color) {
        return Component.literal(sign + formatPercent(fraction)).withStyle(color);
    }

    private static String formatPercent(double fraction) {

        double value = fraction * 100.0D;
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return Math.round(value) + "%";
        }
        return String.format(Locale.ROOT, "%.1f%%", value);
    }
}
