package net.fodoth.skina.goldentweaks.mixin.fix.extendedae_plus;

import com.extendedae_plus.compat.JeiRuntimeCompat;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.stack.EmiStack;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.fodoth.skina.goldentweaks.compat.ae2.GTAe2CraftConfirmBookmark;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

    // extendedae_plus 的「添加书签」（Shift + 合成计划左下角的取消按钮 → CraftConfirmMenu#goBack）原本把缺失
    // 物品交给 JEI 的 BookmarkOverlay；本包 JEI 是 TMRV 的桩，getBookmarkOverlay 不是真正的 BookmarkOverlay，
    // 于是整段静默失败。这里把入参改投 EMI 收藏夹（与 AEQC / neoecoae 的「收藏缺失」同一落点）。

    @Inject(method = "addBookmark(Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void gt$addItemBookmarkToEmi(ItemStack stack, CallbackInfo ci) {
        if (stack != null) {
            GTAe2CraftConfirmBookmark.favoriteIntoEmi(EmiStack.of(stack));
        }
        ci.cancel();
    }

    @Inject(method = "addBookmark(Lnet/neoforged/neoforge/fluids/FluidStack;)V", at = @At("HEAD"), cancellable = true)
    private static void gt$addFluidBookmarkToEmi(FluidStack stack, CallbackInfo ci) {
        if (stack != null) {
            GTAe2CraftConfirmBookmark.favoriteIntoEmi(NeoForgeEmiStack.of(stack));
        }
        ci.cancel();
    }
}
