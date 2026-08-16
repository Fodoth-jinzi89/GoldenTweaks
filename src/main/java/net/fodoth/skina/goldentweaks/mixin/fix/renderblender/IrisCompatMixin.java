package net.fodoth.skina.goldentweaks.mixin.fix.renderblender;

import net.minecraft.world.item.ItemDisplayContext;
import net.weibai.renderblender.client.compat.IrisCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 光影（Iris shaderpack）启用时，renderblender 的 {@code IrisCompat.shouldDefer}
 * 只对第一/第三人称（手持）上下文返回 true，把 cosmic 层延迟到 AFTER_LEVEL。
 * 物品展示框里的 cosmic 物品以 {@code ItemDisplayContext.FIXED} 渲染，不在其中，
 * 会在 Iris/sodium 的世界渲染管线内直接用自定义 core shader 绘制并被丢弃（星空消失）。
 *
 * <p>这里把 FIXED 也纳入延迟范围：光影启用时展示框的 cosmic 层同样入队，
 * 由 AFTER_LEVEL 的 {@code CosmicRenderQueue.renderAll()} 统一渲染。
 * 无光影时 {@code isShaderPackEnabled()} 为 false，保持原样立即渲染。
 */
@Mixin(value = IrisCompat.class, remap = false)
public abstract class IrisCompatMixin {

    @Inject(method = "shouldDefer", at = @At("HEAD"), cancellable = true)
    private static void gt$deferFixedContext(ItemDisplayContext context, CallbackInfoReturnable<Boolean> cir) {
        if (context == ItemDisplayContext.FIXED && IrisCompat.isShaderPackEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
