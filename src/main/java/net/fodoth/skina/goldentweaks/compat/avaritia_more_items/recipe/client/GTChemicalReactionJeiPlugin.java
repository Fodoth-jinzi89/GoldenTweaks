package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipes;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMultiblock;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;

/**
 * Registers the chemical reaction page with a real JEI.
 *
 * <p>Does nothing when TMRV is installed. TMRV re-implements the JEI API on top of EMI, so this
 * plugin would run there too and each reaction would end up with two pages: the native EMI page
 * written for this machine, and TMRV's conversion of the JEI category below.</p>
 *
 * <p>The check is on {@code toomanyrecipeviewers} rather than on {@code emi}: TMRV deliberately
 * makes JEI's own mod lookups report EMI as absent, so asking for EMI here answers "no" while EMI
 * is in fact drawing the screen.</p>
 *
 * <p>The plugin uid lives in the {@code goldentweaks_jei} namespace, like the other JEI plugins in
 * this package.</p>
 */
@JeiPlugin
public final class GTChemicalReactionJeiPlugin implements IModPlugin {

    private static final String AMI = "avaritia_more_items";
    private static final String TMRV = "toomanyrecipeviewers";

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath("goldentweaks_jei", "chemical_reaction");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (!shouldLoad()) {
            return;
        }
        registration.addRecipeCategories(
                new GTChemicalReactionJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (!shouldLoad()) {
            return;
        }

        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        var recipes = level.getRecipeManager()
                .getAllRecipesFor(GTChemicalReactionRecipes.TYPE.get())
                .stream()
                // addRecipes takes the recipe values, not the holders; the page needs no id.
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(GTChemicalReactionJeiCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (!shouldLoad()) {
            return;
        }
        registration.addRecipeCatalyst(
                new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get()),
                GTChemicalReactionJeiCategory.TYPE);
    }

    /** See the class comment: real JEI only, and only when the machine itself exists. */
    private static boolean shouldLoad() {
        return ModList.get().isLoaded(AMI) && !ModList.get().isLoaded(TMRV);
    }
}
