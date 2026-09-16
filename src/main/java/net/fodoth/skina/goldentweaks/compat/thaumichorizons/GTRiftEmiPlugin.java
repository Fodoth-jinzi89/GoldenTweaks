package net.fodoth.skina.goldentweaks.compat.thaumichorizons;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.neoforged.fml.ModList;

/**
 * EMI page for rift crafting, mirroring Create's "mysterious conversion" page: offering → rift →
 * product.
 */
@EmiEntrypoint
public class GTRiftEmiPlugin implements EmiPlugin {

    private static final String HORIZONS_MODID = "thaumichorizons";

    @Override
    public void register(EmiRegistry registry) {
        // Guard before touching any Thaumic Horizons class.
        if (!ModList.get().isLoaded(HORIZONS_MODID)) {
            return;
        }

        GTRiftEmiRecipe.register(registry);
    }
}
