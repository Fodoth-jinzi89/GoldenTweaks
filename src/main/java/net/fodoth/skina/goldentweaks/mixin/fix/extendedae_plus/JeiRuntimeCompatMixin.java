package net.fodoth.skina.goldentweaks.mixin.fix.extendedae_plus;

import com.extendedae_plus.compat.JeiRuntimeCompat;
import mezz.jei.api.ingredients.ITypedIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(value = JeiRuntimeCompat.class, remap = false)
public abstract class JeiRuntimeCompatMixin {
    @Inject(method = "getIngredientUnderMouse()Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void gt$disableJeiOverlayLookup(CallbackInfoReturnable<Optional<ITypedIngredient<?>>> cir) {
        cir.setReturnValue(Optional.empty());
    }

    @Inject(method = "getIngredientUnderMouse(DD)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void gt$disableJeiOverlayLookupAt(double mouseX, double mouseY,
                                                      CallbackInfoReturnable<Optional<ITypedIngredient<?>>> cir) {
        cir.setReturnValue(Optional.empty());
    }

    @Inject(method = "getBookmarkList", at = @At("HEAD"), cancellable = true)
    private static void gt$disableJeiBookmarkLookup(CallbackInfoReturnable<List<ITypedIngredient<?>>> cir) {
        cir.setReturnValue(List.of());
    }

    @Inject(method = "getBookmarkUnderMouse()Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void gt$disableJeiBookmarkUnderMouseLookup(CallbackInfoReturnable<Optional<?>> cir) {
        cir.setReturnValue(Optional.empty());
    }

    @Inject(method = "getRecipeBookmarkUnderMouse()Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void gt$disableJeiRecipeBookmarkLookup(CallbackInfoReturnable<Optional<?>> cir) {
        cir.setReturnValue(Optional.empty());
    }
}
