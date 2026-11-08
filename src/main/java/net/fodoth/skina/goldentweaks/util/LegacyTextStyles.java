package net.fodoth.skina.goldentweaks.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;

/**
 * Converts legacy {@code §} formatting codes embedded in a {@link FormattedText} into per-run
 * {@link Style}s.
 *
 * <p>Vanilla {@code StringSplitter} keeps the rest of a split run under the style at the break
 * point, and a {@code §r} inside that remainder resets to that same style rather than to the
 * original base style. When a wrap happens in the middle of a highlighted word, every {@code §r}
 * on the following line therefore restores the highlight colour instead of clearing it, and the
 * whole line ends up coloured. Converting the codes to explicit styles beforehand removes the
 * legacy codes entirely, so wrapping only ever cuts between styled runs.</p>
 */
public final class LegacyTextStyles {

    private static final char FORMATTING_CODE = '\u00a7';

    private LegacyTextStyles() {
    }

    public static FormattedText toStyledText(FormattedText text) {
        if (text.getString().indexOf(FORMATTING_CODE) < 0) {
            return text;
        }

        MutableComponent result = Component.empty();
        StringBuilder buffer = new StringBuilder();
        Style[] currentStyle = {Style.EMPTY};
        StringDecomposer.iterateFormatted(text, Style.EMPTY, (index, style, codePoint) -> {
            if (!style.equals(currentStyle[0])) {
                flush(result, buffer, currentStyle[0]);
                currentStyle[0] = style;
            }
            buffer.appendCodePoint(codePoint);
            return true;
        });
        flush(result, buffer, currentStyle[0]);
        return result;
    }

    private static void flush(MutableComponent result, StringBuilder buffer, Style style) {
        if (buffer.isEmpty()) {
            return;
        }
        result.append(Component.literal(buffer.toString()).withStyle(style));
        buffer.setLength(0);
    }
}
