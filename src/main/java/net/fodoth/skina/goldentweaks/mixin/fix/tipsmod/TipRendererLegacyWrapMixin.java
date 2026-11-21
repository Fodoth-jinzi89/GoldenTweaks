package net.fodoth.skina.goldentweaks.mixin.fix.tipsmod;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
        return font.split(gt$toStyledText(text), width);
    }

    @Unique
    private static FormattedText gt$toStyledText(FormattedText text) {
        String raw = text.getString();
        if (raw.indexOf('\u00a7') < 0) {
            return text;
        }

        MutableComponent result = Component.empty();
        StringBuilder buffer = new StringBuilder();
        Style style = Style.EMPTY;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\u00a7' && i + 1 < raw.length()) {
                ChatFormatting formatting = ChatFormatting.getByCode(raw.charAt(i + 1));
                if (formatting != null) {
                    if (!buffer.isEmpty()) {
                        result.append(Component.literal(buffer.toString()).withStyle(style));
                        buffer.setLength(0);
                    }
                    style = formatting == ChatFormatting.RESET
                            ? Style.EMPTY
                            : style.applyLegacyFormat(formatting);
                    i++;
                    continue;
                }
            }
            buffer.append(c);
        }
        if (!buffer.isEmpty()) {
            result.append(Component.literal(buffer.toString()).withStyle(style));
        }
        return result;
    }
}
