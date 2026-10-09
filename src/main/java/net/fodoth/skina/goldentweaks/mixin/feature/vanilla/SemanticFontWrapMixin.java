package net.fodoth.skina.goldentweaks.mixin.feature.vanilla;

import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.ibm.icu.text.BreakIterator;

@Mixin(Font.class)
public abstract class SemanticFontWrapMixin {
    @Shadow
    @Final
    private StringSplitter splitter;

    @Inject(method = "split", at = @At("HEAD"), cancellable = true)
    private void goldenTweaks$semanticWrap(FormattedText text, int maxWidth, CallbackInfoReturnable<List<FormattedCharSequence>> cir) {
        if (maxWidth <= 0) {
            return;
        }

        // 预先解析组件样式与 § 颜色码，得到纯文本 + 逐字符样式表，
        // 这样按语义换行时既不会丢失组件样式，也不会让跨行的颜色状态断开。
        StringBuilder plain = new StringBuilder();
        List<Style> styles = new ArrayList<>();
        StringDecomposer.iterateFormatted(text, Style.EMPTY, (index, style, codePoint) -> {
            plain.appendCodePoint(codePoint);
            for (int i = Character.charCount(codePoint); i > 0; i--) {
                styles.add(style);
            }
            return true;
        });

        String value = plain.toString();
        if (value.isEmpty()) {
            return;
        }

        List<FormattedText> lines = new ArrayList<>();
        BreakIterator breaker = BreakIterator.getLineInstance();

        int paragraphStart = 0;
        for (int i = 0; i <= value.length(); i++) {
            if (i == value.length() || value.charAt(i) == '\n') {
                if (i == paragraphStart) {
                    lines.add(FormattedText.EMPTY);
                } else {
                    this.goldenTweaks$wrapParagraph(value, styles, paragraphStart, i, maxWidth, breaker, lines);
                }
                paragraphStart = i + 1;
            }
        }

        cir.setReturnValue(Language.getInstance().getVisualOrder(lines));
    }

    private void goldenTweaks$wrapParagraph(String value, List<Style> styles, int start, int end, int maxWidth, BreakIterator breaker, List<FormattedText> lines) {
        String paragraph = value.substring(start, end);
        breaker.setText(paragraph);
        int lineStart = 0;
        int lastBreak = breaker.first();
        int boundary;
        while ((boundary = breaker.next()) != BreakIterator.DONE) {
            // 直接用 StringSplitter 量宽（与原版 split 同一条路径），不走 Font.width(String)：
            // 后者会被 AcceleratedRendering 缓存进非线程安全的 Object2IntOpenHashMap，
            // 而 TMRV 会在工作线程上构造 EMI 信息配方并调用 split，跨线程写入导致 rehash 越界崩溃。
            if (this.splitter.stringWidth(paragraph.substring(lineStart, boundary)) > maxWidth && lastBreak > lineStart) {
                lines.add(goldenTweaks$slice(value, styles, start + lineStart, start + lastBreak));
                lineStart = lastBreak;
            }
            lastBreak = boundary;
        }
        if (lineStart < paragraph.length()) {
            lines.add(goldenTweaks$slice(value, styles, start + lineStart, end));
        }
    }

    private static FormattedText goldenTweaks$slice(String value, List<Style> styles, int from, int to) {
        if (from >= to) {
            return FormattedText.EMPTY;
        }
        return new FormattedText() {
            @Override
            public <T> Optional<T> visit(FormattedText.ContentConsumer<T> acceptor) {
                return acceptor.accept(value.substring(from, to));
            }

            @Override
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> acceptor, Style style) {
                int index = from;
                while (index < to) {
                    Style runStyle = styles.get(index);
                    int runEnd = index + 1;
                    while (runEnd < to && styles.get(runEnd).equals(runStyle)) {
                        runEnd++;
                    }
                    Optional<T> result = acceptor.accept(runStyle.applyTo(style), value.substring(index, runEnd));
                    if (result.isPresent()) {
                        return result;
                    }
                    index = runEnd;
                }
                return Optional.empty();
            }
        };
    }
}
