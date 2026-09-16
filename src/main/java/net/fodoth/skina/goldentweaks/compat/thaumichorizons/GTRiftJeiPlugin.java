package net.fodoth.skina.goldentweaks.compat.thaumichorizons;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

/**
 * JEI page for rift crafting, mirroring Create's "mysterious conversion" page: offering → rift →
 * product. EMI renders the same page through its JEI compatibility layer.
 */
@JeiPlugin
public class GTRiftJeiPlugin implements IModPlugin {

    private static final String HORIZONS_MODID = "thaumichorizons";

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "rift_crafting");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        if (hasHorizons()) {
            GTRiftJeiCategory.register(registration);
        }
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (hasHorizons()) {
            GTRiftJeiCategory.registerRecipes(registration);
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (hasHorizons()) {
            GTRiftJeiCategory.registerCatalyst(registration);
        }
    }

    /** Guarded before touching any Thaumic Horizons class. */
    private static boolean hasHorizons() {
        return ModList.get().isLoaded(HORIZONS_MODID);
    }
}
