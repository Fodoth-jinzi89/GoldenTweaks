package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactionRecipes;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipe;
import java.util.List;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipes;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMultiblock;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

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

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath("goldentweaks_jei", "chemical_reaction");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        if (!shouldLoad()) {
            return;
        }
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new GTChemicalReactionJeiCategory(guiHelper),
                new GTChemicalReactionJeiCategory(guiHelper,
                        GTChemicalReactionJeiCategory.ADVANCED_TYPE, "advanced_chemical_reaction",
                        new ItemStack(GTAdvancedChemicalReactorMultiblock.MACHINE_ITEM.get())));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (!shouldLoad()) {
            return;
        }

        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        // addRecipes takes the recipe values, not the holders; the page needs no id.
        registration.addRecipes(GTChemicalReactionJeiCategory.TYPE,
                valuesOf(level, GTChemicalReactionRecipes.TYPE.get()));
        registration.addRecipes(GTChemicalReactionJeiCategory.ADVANCED_TYPE,
                valuesOf(level, GTAdvancedChemicalReactionRecipes.TYPE.get()));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (!shouldLoad()) {
            return;
        }
        // Both machines serve the basic page: the advanced kettle runs the basic set too.
        registration.addRecipeCatalyst(
                new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get()),
                GTChemicalReactionJeiCategory.TYPE);
        registration.addRecipeCatalyst(
                new ItemStack(GTAdvancedChemicalReactorMultiblock.MACHINE_ITEM.get()),
                GTChemicalReactionJeiCategory.TYPE);
        registration.addRecipeCatalyst(
                new ItemStack(GTAdvancedChemicalReactorMultiblock.MACHINE_ITEM.get()),
                GTChemicalReactionJeiCategory.ADVANCED_TYPE);
    }

    /** The recipes of one type, as plain values. */
    private static List<GTChemicalReactionRecipe> valuesOf(
            net.minecraft.world.level.Level level,
            net.minecraft.world.item.crafting.RecipeType<GTChemicalReactionRecipe> type) {
        return level.getRecipeManager().getAllRecipesFor(type).stream()
                .map(RecipeHolder::value)
                .toList();
    }

    /**
     * Real JEI only, and only when the machine itself exists.
     *
     * <p>EMI's presence is what decides this, not whether TMRV reports itself: TMRV runs the JEI
     * API on top of EMI and hides its own mod id from the very lookups this check would use. With
     * EMI on the classpath the native pages already cover every recipe, so this plugin stands
     * down; otherwise it is the only viewer integration there is.</p>
     */
    private static boolean shouldLoad() {
        return ModList.get().isLoaded(AMI) && !hasEmi();
    }

    /** Whether EMI is present. Asked of the classpath, which no shim can disguise. */
    private static boolean hasEmi() {
        try {
            Class.forName("dev.emi.emi.api.EmiPlugin", false,
                    GTChemicalReactionJeiPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException absent) {
            return false;
        }
    }
}
