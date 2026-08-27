package net.fodoth.skina.goldentweaks.mixin.feature.vanilla;

import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import com.ibm.icu.text.BreakIterator;

@Mixin(Font.class)
public abstract class SemanticFontWrapMixin {
    @Shadow
    public abstract int width(String text);

    @Inject(method = "split", at = @At("HEAD"), cancellable = true)
    private void goldenTweaks$semanticWrap(FormattedText text, int maxWidth, CallbackInfoReturnable<List<FormattedCharSequence>> cir) {
        String value = text.getString();
        if (value.isEmpty() || maxWidth <= 0) {
            return;
        }

        List<FormattedText> lines = new ArrayList<>();
        BreakIterator breaker = BreakIterator.getLineInstance();
        for (String paragraph : value.split("\\n", -1)) {
            if (paragraph.isEmpty()) {
                lines.add(FormattedText.EMPTY);
                continue;
            }
            breaker.setText(paragraph);
            int lineStart = 0;
            int lastBreak = breaker.first();
            int boundary;
            while ((boundary = breaker.next()) != BreakIterator.DONE) {
                if (width(paragraph.substring(lineStart, boundary)) > maxWidth && lastBreak > lineStart) {
                    lines.add(FormattedText.of(paragraph.substring(lineStart, lastBreak)));
                    lineStart = lastBreak;
                }
                lastBreak = boundary;
            }
            if (lineStart < paragraph.length()) {
                lines.add(FormattedText.of(paragraph.substring(lineStart)));
            }
        }
        cir.setReturnValue(Language.getInstance().getVisualOrder(lines));
    }
}
