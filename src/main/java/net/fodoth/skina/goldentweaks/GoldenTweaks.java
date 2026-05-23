package net.fodoth.skina.goldentweaks;

import com.mojang.logging.LogUtils;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.AFAdditionalCreativeTabs;
import net.fodoth.skina.goldentweaks.registry.alshanex_familiars.AFAdditionalItemsRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
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

        if (ModList.get().isLoaded("alshanex_familiars")) {
            LOGGER.info("Detected alshanex_familiars, registering compatibility content");
            AFAdditionalItemsRegistry.register(modEventBus);
            AFAdditionalCreativeTabs.register(modEventBus);
        }
    }
}