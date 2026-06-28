package net.fodoth.skina.goldentweaks.mixin.fix.modernui;

import com.ibm.icu.text.BreakIterator;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import icyllis.modernui.graphics.text.EmojiFont;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EmojiFont.class)
public class EmojiFontMixin {

    @WrapOperation(
            method = "calcGlyphScore",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/ibm/icu/text/BreakIterator;following(I)I"
            )
    )
    private int goldenTweaks$safeFollowing(
            BreakIterator instance, int i, Operation<Integer> original
    ) {
        try {
            return original.call(instance, i);
        } catch (Exception e) {
            return BreakIterator.DONE;
        }
    }
}
