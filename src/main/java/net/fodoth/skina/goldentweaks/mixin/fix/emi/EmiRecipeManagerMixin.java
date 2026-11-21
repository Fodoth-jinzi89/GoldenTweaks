package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.registry.EmiRecipes;
import net.fodoth.skina.goldentweaks.compat.emi.GTWorkstationRecipes;
import net.fodoth.skina.goldentweaks.compat.emi.GTRecipeIndexes;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;
import java.util.Map;

@Mixin(targets = "dev.emi.emi.registry.EmiRecipes$Manager", remap = false)
public abstract class EmiRecipeManagerMixin {

    @Shadow private Map<ResourceLocation, EmiRecipe> byId;
    @Shadow private Map<EmiStack, List<EmiRecipe>> byInput;
    @Shadow private Map<EmiStack, List<EmiRecipe>> byOutput;

    @Inject(method = "<init>(Ljava/util/List;Ljava/util/Map;Ljava/util/List;Z)V", at = @At("RETURN"))
    private void gt$shareWorkstationArrays(CallbackInfo ci) {
        GTWorkstationRecipes.compact(EmiRecipes.byWorkstation);
        byId = GTRecipeIndexes.compactIds(byId);
        GTRecipeIndexes.compactSmallLists(byInput);
        GTRecipeIndexes.compactSmallLists(byOutput);
    }
}
