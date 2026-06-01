package net.fodoth.skina.goldentweaks.mixin.fix.modernui;


import icyllis.modernui.mc.text.TextRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.SequencedMap;

@Mixin(TextRenderType.class)
public class ModernUITextRenderTypeMixin {

    @Redirect(
            method = "makeSDFStrokeType",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/SequencedMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )


    private static Object goldentweaks$ignoreImmutableFixedBuffers(
            SequencedMap<Object, Object> map, Object key, Object value
    ) {

        try {
            return map.put(key, value);
        } catch (UnsupportedOperationException e) {
            return map.get(key);
        }
    }
}
