package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipe;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipes;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client.GTChemicalReactorScreen;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMultiblock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

/**
 * Registers the chemical reaction page with EMI.
 *
 * <p>Guarded on {@code avaritia_more_items}: without AMI the kettle is not registered at all, so
 * touching its recipe type here would fail.</p>
 */
@EmiEntrypoint
public final class GTChemicalReactionEmiPlugin implements EmiPlugin {

    private static final String AMI = "avaritia_more_items";

    @Override
    public void register(EmiRegistry registry) {
        if (!ModList.get().isLoaded(AMI)) {
            return;
        }

        registry.addCategory(GTChemicalReactionEmiRecipe.CATEGORY);
        registry.addWorkstation(GTChemicalReactionEmiRecipe.CATEGORY,
                EmiStack.of(new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get())));

        @Nullable HolderLookup.Provider registries = registries();
        registry.getRecipeManager()
                .getAllRecipesFor(GTChemicalReactionRecipes.TYPE.get())
                .forEach(holder -> registry.addRecipe(toEmiRecipe(holder, registries)));

        // EMI places its own panels around whatever space a screen announces. The kettle draws a
        // side pane outside its main panel, so without this it lays the recipe list straight over
        // that pane — in both its collapsed and expanded sizes.
        registry.addScreenBoundsProvider(GTChemicalReactorScreen.class,
                screen -> new Bounds(screen.panelLeft(), screen.panelTop(),
                        screen.sidebarRight() - screen.panelLeft(),
                        screen.sidebarBottom() - screen.panelTop()));
    }

    private static GTChemicalReactionEmiRecipe toEmiRecipe(
            RecipeHolder<GTChemicalReactionRecipe> holder, @Nullable HolderLookup.Provider registries) {
        return new GTChemicalReactionEmiRecipe(holder.id(), holder.value(), registries);
    }

    /**
     * Registry access for the AE keys.
     *
     * <p>Only needed when a recipe mentions an AE stack, and only resolvable once a level exists;
     * returning {@code null} simply leaves those slots empty instead of breaking the whole page.</p>
     */
    @Nullable
    private static HolderLookup.Provider registries() {
        var level = Minecraft.getInstance().level;
        return level == null ? null : level.registryAccess();
    }
}
