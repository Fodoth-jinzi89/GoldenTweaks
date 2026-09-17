package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiTagKey;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 与 {@link ItemEmiStackTooltipMixin} 同根因：{@code EmiTags#getRawValues} 会经
 * {@code EmiTagKey#registry()} 调 {@code client.level.registryAccess()}，
 * 没有世界时（EMI 在菜单里/退出世界后烘焙）直接 NPE，
 * 日志表现为 {@code Exception deserializing stack "#item:..."}。
 *
 * <p>没有世界就没有注册表可查 ⇒ 返回空列表（调用方得到空 ingredient）；有世界时行为不变。</p>
 */
@Mixin(value = EmiTags.class, remap = false)
public abstract class EmiTagsRawValuesMixin {

    @Inject(method = "getRawValues", at = @At("HEAD"), cancellable = true)
    private static <T> void gt$skipTagLookupWithoutLevel(
            EmiTagKey<T> key, CallbackInfoReturnable<List<EmiStack>> cir
    ) {

        if (Minecraft.getInstance().level == null) {
            cir.setReturnValue(List.of());
        }
    }
}
