package net.fodoth.skina.goldentweaks.mixin.fix.cofh_core;

import cofh.core.client.event.CoreClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * CoFH Core 在 {@code handleRenderTooltipEvent} 里对工具提示首行组件的 siblings 直接 {@code removeIf}，
 * 但有些界面传入的 {@code Component} 其 siblings 是不可变列表（例如 AE2 的 {@code PatternAccessTermScreen}
 * 经 {@code GuiGraphics.renderTooltip} 走到这里时），对不可变列表调用 {@code removeIf} 会抛
 * {@code UnsupportedOperationException}，导致渲染线程崩溃。
 * <p>
 * 这里在调用 {@code Optional#ifPresent} 处包一层 try/catch：无法就地过滤时直接跳过这次清理，
 * 仍然保留 CoFH 本来的「去掉不可见样式兄弟组件」行为（仅在可变列表上生效）。
 */
@Mixin(value = CoreClientEvents.class, remap = false)
public abstract class CoreClientEventsMixin {

    @Redirect(
            method = "handleRenderTooltipEvent",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V")
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void gt$guardImmutableSiblings(Optional optional, Consumer consumer) {
        try {
            optional.ifPresent(consumer);
        } catch (UnsupportedOperationException ignored) {
            // 不可变的 siblings 列表无法就地清理，跳过即可，不要因此崩溃
        }
    }
}
