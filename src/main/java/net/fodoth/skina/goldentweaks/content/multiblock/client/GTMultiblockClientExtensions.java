package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTMultiblockClientExtensions {

    private static final int CONTROL_CIRCUIT_BASE_TINT = 0xFFC9C9C9;
    private static final int CONTROL_CIRCUIT_OVERCLOCK_4_2_TINT = 0xFF90CCF8;
    private static final int CONTROL_CIRCUIT_PARALLEL_1_TINT = 0xFF74CD80;

    private GTMultiblockClientExtensions() {
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? CONTROL_CIRCUIT_BASE_TINT : 0xFFFFFFFF,
                GTMultiblockBlocks.CONTROL_CIRCUIT_BASE.get());
        event.register((stack, tintIndex) -> tintIndex == 0 ? CONTROL_CIRCUIT_OVERCLOCK_4_2_TINT : 0xFFFFFFFF,
                GTMultiblockBlocks.CONTROL_CIRCUIT_OVERCLOCK_4_2.get());
        event.register((stack, tintIndex) -> tintIndex == 0 ? CONTROL_CIRCUIT_PARALLEL_1_TINT : 0xFFFFFFFF,
                GTMultiblockBlocks.CONTROL_CIRCUIT_PARALLEL_1.get());
    }
}
