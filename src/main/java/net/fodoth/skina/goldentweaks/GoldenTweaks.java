package net.fodoth.skina.goldentweaks;

import com.jesz.createdieselgenerators.CDGSpriteShifts;
import com.mojang.logging.LogUtils;
import net.fodoth.skina.goldentweaks.compat.cataclysm.GTCataclysmCompat;
import net.fodoth.skina.goldentweaks.compat.create.GTCreateItems;
import net.fodoth.skina.goldentweaks.compat.kaleidoscope.KaleidoCompat;
import net.fodoth.skina.goldentweaks.compat.kaleidoscope.VillageGarbageStationAddition;
import net.fodoth.skina.goldentweaks.compat.questshop.QSCompat;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftRecipeLoader;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.AFAdditionalCreativeTabs;
import net.fodoth.skina.goldentweaks.event.ExspectrimentsClientCompatEvent;
import net.fodoth.skina.goldentweaks.event.FamiliarProtectionEvent;
import net.fodoth.skina.goldentweaks.event.InvertedFamiliarSpellbookEvent;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.AFAdditionalItems;
import net.fodoth.skina.goldentweaks.compat.create.GTCreateCompat;
import net.fodoth.skina.goldentweaks.event.SalvageCharmEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(GoldenTweaks.MODID)
public class GoldenTweaks {

    public static final String MODID = "goldentweaks";

    public static final Logger LOGGER = LogUtils.getLogger();

    public GoldenTweaks(IEventBus modEventBus, ModContainer container) {

        container.registerConfig(
                ModConfig.Type.CLIENT,
                GoldenTweaksClientConfig.SPEC
        );

        container.registerConfig(
                ModConfig.Type.COMMON,
                GoldenTweaksCommonConfig.SPEC
        );

        if (ModList.get().isLoaded("create")) {
            LOGGER.info("Detected Create, registering Registrate");

            GTCreateCompat.registrate()
                    .registerEventListeners(modEventBus);
            GTCreateItems.register();
        }

        if (ModList.get().isLoaded("apotheosis_things")) {
            LOGGER.info("Detected apotheosis_things, registering compatibility content");

            NeoForge.EVENT_BUS.register(SalvageCharmEvent.class);
        }


        if (ModList.get().isLoaded("alshanex_familiars")) {
            LOGGER.info("Detected alshanex_familiars, registering compatibility content");
            AFAdditionalItems.register(modEventBus);
            AFAdditionalCreativeTabs.register(modEventBus);
            NeoForge.EVENT_BUS.register(InvertedFamiliarSpellbookEvent.class);
            NeoForge.EVENT_BUS.register(FamiliarProtectionEvent.class);
        }

        if (ModList.get().isLoaded("exspectriments")) {
            LOGGER.info("Detected exspectriments, registering compatibility content");
            modEventBus.register(ExspectrimentsClientCompatEvent.class);
        }

        if (ModList.get().isLoaded("kaleidoscope_cookery")) {
            LOGGER.info("Detected kaleidoscope_cookery, registering compatibility content");

            KaleidoCompat.register(modEventBus);
            NeoForge.EVENT_BUS.register(VillageGarbageStationAddition.class);
        }

        if (ModList.get().isLoaded("cataclysm")) {
            LOGGER.info("Detected cataclysm, registering compatibility content");

            GTCataclysmCompat.register(modEventBus);
        }

        if (ModList.get().isLoaded("questshop")) {
            LOGGER.info("Detected questshop, registering compatibility content");

            QSCompat.register(modEventBus);
        }

        if (ModList.get().isLoaded("thaumcraft")) {
            LOGGER.info("Detected thaumcraft, registering compatibility content");

            NeoForge.EVENT_BUS.register(GTThaumcraftRecipeLoader.class);
        }


        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("createdieselgenerators")) {
            LOGGER.info("Detected createdieselgenerators, delayed init");
            event.enqueueWork(CDGSpriteShifts::init);
        }
    }
}
