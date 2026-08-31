package net.fodoth.skina.goldentweaks.compat.compactmachines;

import net.neoforged.bus.api.IEventBus;

public final class CompactMachinesCompat {

    private CompactMachinesCompat() {
    }

    public static void register(IEventBus modEventBus) {
        CompactMachinesAdditionalBlocks.BLOCKS.register(modEventBus);
        CompactMachinesAdditionalBlocks.ITEMS.register(modEventBus);
    }
}
