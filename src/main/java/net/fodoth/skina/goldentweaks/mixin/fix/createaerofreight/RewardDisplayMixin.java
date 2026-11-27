package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.client.RewardDisplay;
import cn.blockforge.generated.createareofreight.client.ui.RewardPreviewLayout;
import java.util.List;
import net.fodoth.skina.goldentweaks.compat.createaerofreight.GTRewardSelection;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 奖励图标超过一行容量时按页轮播。
 *
 * 注入点是 public 的 {@code drawRow(...,boolean)}，而不是真正画内容的那个 366 字节重载——
 * 后者在 1.1.2 里多了一个包内可见的 {@code FreightGuiTheme} 参数，从外部引用不到，没法写进
 * {@code @Redirect} 的签名。好在所有入口（{@code draw}、两个 drawRow 重载）都汇入这个 public
 * 重载，在它的 HEAD 把 List 参数换成分页结果，效果一样且不碰那个类型。
 */
@Mixin(value = RewardDisplay.class, remap = false)
public abstract class RewardDisplayMixin {
    @Unique private static Screen gt$screen;
    @Unique private static long gt$openedAt;

    @ModifyVariable(
            method = "drawRow(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IILjava/util/List;IIZ)Lcn/blockforge/generated/createareofreight/client/RewardDisplay$Row;",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static List<ItemStack> gt$rotate(List<ItemStack> entries, GuiGraphics graphics, Font font,
            int x, int y, List<ItemStack> snapshot, int total, int maxWidth, boolean shadow) {
        int capacity = RewardPreviewLayout.inline(maxWidth,
                font.width(Component.translatable("screen.create_aerofreight.expected_reward")),
                font.width("+" + Math.max(0, total)) + 7, snapshot.size(), total).icons();
        Screen screen = Minecraft.getInstance().screen;
        long now = Util.getMillis();
        if (gt$screen != screen) {
            gt$screen = screen;
            gt$openedAt = now;
        }
        return GTRewardSelection.page(entries, capacity, now - gt$openedAt);
    }
}
