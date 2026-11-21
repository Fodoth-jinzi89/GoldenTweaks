package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.client.RewardDisplay;
import cn.blockforge.generated.createareofreight.client.ui.RewardPreviewLayout;
import java.util.Iterator;
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
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RewardDisplay.class, remap = false)
public abstract class RewardDisplayMixin {
    @Unique private static Screen gt$screen;
    @Unique private static long gt$openedAt;

    @Redirect(method = "drawRow(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IILjava/util/List;IIZ)Lcn/blockforge/generated/createareofreight/client/RewardDisplay$Row;",
            at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private static Iterator<ItemStack> gt$rotate(List<ItemStack> entries, GuiGraphics graphics, Font font,
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
        return GTRewardSelection.page(entries, capacity, now - gt$openedAt).iterator();
    }
}
