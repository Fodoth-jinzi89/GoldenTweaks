package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.api.stack.EmiIngredient;
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
 * 与 {@link ItemEmiStackTooltipMixin} 同根因：{@code EmiTags#getRawValues}、{@code EmiTags#getIngredient}
 * 会经 {@code EmiTagKey#registry()} 调 {@code client.level.registryAccess()}，
 * 没有世界时（EMI 在菜单里/退出世界后烘焙）直接 NPE，日志表现为
 * {@code Exception deserializing stack "#item:..."} 以及退出世界后 TMRV 重建配方时
 * {@code NullPointerException ... EmiTagKey.registry / EmiTags.getValues}。
 *
 * <p>没有世界就没有注册表可查 ⇒ 标签查询返回空；{@code getIngredient} 直接返回 {@link EmiStack#EMPTY}，
 * 在进入 {@code tagIngredient → TagEmiIngredient → getValues} 之前就短路（该链在 size==1 时会对空列表取下标）。
 * 有世界时行为不变。</p>
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

    @Inject(method = "getIngredient", at = @At("HEAD"), cancellable = true)
    private static void gt$skipIngredientWithoutLevel(
            Class<?> clazz, List<EmiStack> stacks, long amount,
            CallbackInfoReturnable<EmiIngredient> cir
    ) {

        if (Minecraft.getInstance().level == null) {
            cir.setReturnValue(EmiStack.EMPTY);
        }
    }
}
