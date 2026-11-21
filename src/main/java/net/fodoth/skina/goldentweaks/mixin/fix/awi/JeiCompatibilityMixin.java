package net.fodoth.skina.goldentweaks.mixin.fix.awi;

import dev.emi.emi.registry.EmiRecipes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Pseudo
@Mixin(targets = "com.yanny.awi.jei.compatibility.JeiCompatibility", remap = false)
public abstract class JeiCompatibilityMixin {

    @Shadow @Final
    private static Map<?, ?> dimensions;

    @Inject(method = "registerCategories", at = @At("HEAD"), cancellable = true)
    private void gt$preferNativeEmi(CallbackInfo ci) {
        if (EmiRecipes.categories.stream().anyMatch(category -> category.getClass().getName().equals(
                "com.yanny.awi.emi.compatibility.EmiCompatibility$RecipeCategory"))) {
            // Native EMI has already registered the same worldgen data before TMRV runs JEI plugins.
            dimensions.clear();
            ci.cancel();
        }
    }
}
