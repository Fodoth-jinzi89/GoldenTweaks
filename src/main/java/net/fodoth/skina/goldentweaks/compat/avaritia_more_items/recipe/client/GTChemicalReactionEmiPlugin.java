package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactionRecipes;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipe;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipes;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTVacuumCoolerMultiblock;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTVacuumCoolingRecipes;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client.GTAdvancedChemicalReactorScreen;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client.GTChemicalReactorScreen;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client.GTVacuumCoolerScreen;
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

        @Nullable HolderLookup.Provider registries = registries();

        registry.addCategory(GTChemicalReactionEmiRecipe.CATEGORY);
        // The advanced kettle accepts the basic recipe set as well, so it is a valid machine for
        // this page — a player reading a basic reaction should see both.
        registry.addWorkstation(GTChemicalReactionEmiRecipe.CATEGORY,
                EmiStack.of(new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get())));
        registry.addWorkstation(GTChemicalReactionEmiRecipe.CATEGORY,
                EmiStack.of(new ItemStack(GTAdvancedChemicalReactorMultiblock.MACHINE_ITEM.get())));
        registry.getRecipeManager()
                .getAllRecipesFor(GTChemicalReactionRecipes.TYPE.get())
                .forEach(holder -> registry.addRecipe(
                        new GTChemicalReactionEmiRecipe(holder.id(), holder.value(), registries)));

        // The advanced kettle has its own entry: same panel, but filing its recipes under the basic
        // one would suggest the basic machine can run them.
        registry.addCategory(GTChemicalReactionEmiRecipe.ADVANCED_CATEGORY);
        registry.addWorkstation(GTChemicalReactionEmiRecipe.ADVANCED_CATEGORY,
                EmiStack.of(new ItemStack(GTAdvancedChemicalReactorMultiblock.MACHINE_ITEM.get())));
        registry.getRecipeManager()
                .getAllRecipesFor(GTAdvancedChemicalReactionRecipes.TYPE.get())
                .forEach(holder -> registry.addRecipe(new GTChemicalReactionEmiRecipe(
                        holder.id(), holder.value(), registries,
                        GTChemicalReactionEmiRecipe.ADVANCED_CATEGORY)));

        // The vacuum cooler: its own page, because filing a cooling reaction under the kettle would
        // suggest the kettle can run it.
        registry.addCategory(GTChemicalReactionEmiRecipe.VACUUM_CATEGORY);
        registry.addWorkstation(GTChemicalReactionEmiRecipe.VACUUM_CATEGORY,
                EmiStack.of(new ItemStack(GTVacuumCoolerMultiblock.MACHINE_ITEM.get())));
        registry.getRecipeManager()
                .getAllRecipesFor(GTVacuumCoolingRecipes.TYPE.get())
                .forEach(holder -> registry.addRecipe(new GTChemicalReactionEmiRecipe(
                        holder.id(), holder.value(), registries,
                        GTChemicalReactionEmiRecipe.VACUUM_CATEGORY)));

        // EMI places its own panels around whatever space a screen announces. The kettle draws a
        // side pane outside its main panel, so without this it lays the recipe list straight over
        // that pane — in both its collapsed and expanded sizes.
        //
        // Registered per screen class: EMI matches providers by exact class, so each window needs its
        // own entry or it would be laid out with no bounds and have its sidebar overlapped.
        registry.addScreenBoundsProvider(GTChemicalReactorScreen.class,
                GTChemicalReactionEmiPlugin::kettleBounds);
        registry.addScreenBoundsProvider(GTAdvancedChemicalReactorScreen.class,
                GTChemicalReactionEmiPlugin::kettleBounds);
        registry.addScreenBoundsProvider(GTVacuumCoolerScreen.class,
                GTChemicalReactionEmiPlugin::kettleBounds);
    }

    /** The area the kettle's window occupies, side pane included. */
    private static Bounds kettleBounds(GTChemicalReactorScreen screen) {
        return new Bounds(screen.panelLeft(), screen.panelTop(),
                screen.sidebarRight() - screen.panelLeft(),
                screen.sidebarBottom() - screen.panelTop());
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
