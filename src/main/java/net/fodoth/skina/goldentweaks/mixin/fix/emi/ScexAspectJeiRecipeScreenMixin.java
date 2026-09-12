package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.screen.RecipeScreen;
import net.fodoth.skina.goldentweaks.util.emi.ScexAspectJeiScrollHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeScreen.class, remap = false)
public abstract class ScexAspectJeiRecipeScreenMixin {
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$scrollJeiGrid(double mouseX, double mouseY, double horizontalAmount,
                                            double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (ScexAspectJeiScrollHandler.scroll(mouseX, mouseY, verticalAmount)) {
            cir.setReturnValue(true);
        }
    }
}
