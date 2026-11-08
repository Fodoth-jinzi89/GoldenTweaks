package net.fodoth.skina.goldentweaks.mixin.fix.tipsmod;

import net.fodoth.skina.goldentweaks.util.LegacyTextStyles;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Tips are stored as plain translation strings with {@code §} colour codes. Vanilla line
 * splitting keeps the style at the wrap point as the base style of the remaining text, so a wrap
 * inside a highlighted word makes every {@code §r} on the next line restore that highlight and
 * the entire line stays coloured. Feed the splitter explicit styles instead of legacy codes.
 */
@Mixin(targets = "net.darkhax.tipsmod.common.impl.client.TipRenderer")
public abstract class TipRendererLegacyWrapMixin {

    @Redirect(
            method = "renderLinesReversed(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIILnet/minecraft/network/chat/FormattedText;I)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;"
            )
    )
    private static List<FormattedCharSequence> gt$splitWithExplicitStyles(Font font, FormattedText text, int width) {
        return font.split(LegacyTextStyles.toStyledText(text), width);
    }
}
