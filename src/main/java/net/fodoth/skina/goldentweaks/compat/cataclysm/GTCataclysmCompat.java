package net.fodoth.skina.goldentweaks.compat.cataclysm;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class GTCataclysmCompat {

    private GTCataclysmCompat() {
    }

    public static void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(GTCataclysmEvents.class);
        modEventBus.register(GTCataclysmModEvents.class);
    }
}
