package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.jei.PackingMachineJeiPlugin;
import java.util.Collection;
import java.util.List;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import net.fodoth.skina.goldentweaks.compat.createaerofreight.GTRecipeViewerCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PackingMachineJeiPlugin.class, remap = false)
public abstract class PackingMachineJeiPluginMixin {
    @Redirect(method = "refreshRecipes", at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/IRecipeManager;addRecipes(Lmezz/jei/api/recipe/RecipeType;Ljava/util/List;)V"))
    private <T> void gt$add(IRecipeManager manager, RecipeType<T> type, List<T> recipes) {
        if (GTRecipeViewerCompat.supportsRuntimeChanges(manager)) manager.addRecipes(type, recipes);
    }

    @Redirect(method = "refreshRecipes", at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/IRecipeManager;hideRecipes(Lmezz/jei/api/recipe/RecipeType;Ljava/util/Collection;)V"))
    private <T> void gt$hide(IRecipeManager manager, RecipeType<T> type, Collection<T> recipes) {
        if (GTRecipeViewerCompat.supportsRuntimeChanges(manager)) manager.hideRecipes(type, recipes);
    }

    @Redirect(method = "refreshRecipes", at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/IRecipeManager;unhideRecipes(Lmezz/jei/api/recipe/RecipeType;Ljava/util/Collection;)V"))
    private <T> void gt$unhide(IRecipeManager manager, RecipeType<T> type, Collection<T> recipes) {
        if (GTRecipeViewerCompat.supportsRuntimeChanges(manager)) manager.unhideRecipes(type, recipes);
    }
}
