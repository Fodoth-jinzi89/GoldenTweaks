package net.fodoth.skina.goldentweaks.compat.questshop;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.neoforged.bus.api.IEventBus;

public final class QSCompat {

    private QSCompat() {
    }

    public static void register(IEventBus modBus) {
        QSAdditionalItems.ITEMS.register(modBus);
        QSAdditionalTabs.TABS.register(modBus);

        GoldenTweaks.LOGGER.info("QuestShop compatibility loaded.");
    }
}