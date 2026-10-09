package net.fodoth.skina.goldentweaks.mixin.fix.modernui;


import icyllis.modernui.mc.text.TextRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.SequencedMap;

/**
 * ModernUI 首次创建 SDF 填充/描边渲染类型时，会把自己的固定缓冲区塞进
 * {@code Minecraft.getInstance().renderBuffers().bufferSource().getFixedBuffers()}。
 * 某些整合包里该映射是不可变视图（fastutil），{@code put} 会抛
 * {@link UnsupportedOperationException}，ModernUI 只能打一条 WARN。这里把写入吞掉，
 * 让首次创建静默通过。
 */
@Mixin(TextRenderType.class)
public class ModernUITextRenderTypeMixin {

    @Redirect(
            method = {"makeSDFFillType", "makeSDFStrokeType"},
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
