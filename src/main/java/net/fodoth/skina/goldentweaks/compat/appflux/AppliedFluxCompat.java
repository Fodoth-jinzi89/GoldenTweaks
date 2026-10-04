package net.fodoth.skina.goldentweaks.compat.appflux;

import appeng.api.upgrades.Upgrades;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Makes the ae2helpers result-import card a registered upgrade of the pattern-provider blocks, so it shows up in
 * the AppliedFlux / extendedae_plus upgrade sidebar tooltip ("Compatible Upgrades: ...").
 */
public final class AppliedFluxCompat {

    private static final String IMPORT_CARD_ID = "ae2helpers:result_import_card";

    private static final String[] PROVIDER_ITEM_IDS = {
            "ae2:pattern_provider",
            "ae2:cable_pattern_provider",
            "extendedae:ex_pattern_provider",
            "extendedae:ex_pattern_provider_part",
    };

    private AppliedFluxCompat() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("appflux") || !ModList.get().isLoaded("ae2helpers")) {
            return;
        }

        event.enqueueWork(() -> {
            Item card = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(IMPORT_CARD_ID)).orElse(null);
            if (card == null) {
                return;
            }

            for (String id : PROVIDER_ITEM_IDS) {
                Item machine = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id)).orElse(Items.AIR);
                if (machine != Items.AIR) {
                    Upgrades.add(card, machine, 1, "gui.ae2helpers.import_card");
                }
            }

            GoldenTweaks.LOGGER.info(
                    "AppliedFlux + ae2helpers: registered the result import card for pattern providers");
        });
    }
}
