package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockBlocks;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortTier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Tints the multiblock parts whose artwork is drawn grey.
 *
 * <p>The control circuits and the port rings are all one grey sheet, coloured at draw time. That is
 * what lets a single port texture serve nine voltages: the model names one ring, and the colour
 * comes from the tier the block was registered at.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTMultiblockClientExtensions {

    private static final int CONTROL_CIRCUIT_BASE_TINT = 0xFFC9C9C9;
    private static final int CONTROL_CIRCUIT_OVERCLOCK_4_2_TINT = 0xFF90CCF8;
    private static final int CONTROL_CIRCUIT_PARALLEL_1_TINT = 0xFF74CD80;
    /** The fourfold parallel part shares the parallel circuit's green: same family, denser icon. */
    private static final int CONTROL_CIRCUIT_PARALLEL_4X_TINT = CONTROL_CIRCUIT_PARALLEL_1_TINT;

    /** Model tint index 0 is the ring. */
    private static final int TINT_RING = 0;

    private GTMultiblockClientExtensions() {
    }

    /**
     * The tint the block renderer multiplies the ring by.
     *
     * <p>MAX answers white because its ring is an animated texture that already carries its own
     * colour: a tint would both darken it and freeze one moment of the spectrum into the mesh,
     * which is built once and then reused.</p>
     */
    private static int tintOf(GTPortTier tier) {
        if (tier == GTPortTier.MAX) {
            return 0xFFFFFFFF;
        }
        return 0xFF000000 | tier.colour();
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? CONTROL_CIRCUIT_BASE_TINT : 0xFFFFFFFF,
                GTMultiblockBlocks.CONTROL_CIRCUIT_BASE.get());
        event.register((stack, tintIndex) -> tintIndex == 0 ? CONTROL_CIRCUIT_OVERCLOCK_4_2_TINT : 0xFFFFFFFF,
                GTMultiblockBlocks.CONTROL_CIRCUIT_OVERCLOCK_4_2.get());
        event.register((stack, tintIndex) -> tintIndex == 0 ? CONTROL_CIRCUIT_PARALLEL_1_TINT : 0xFFFFFFFF,
                GTMultiblockBlocks.CONTROL_CIRCUIT_PARALLEL_1.get());
        event.register((stack, tintIndex) -> tintIndex == 0 ? CONTROL_CIRCUIT_PARALLEL_4X_TINT : 0xFFFFFFFF,
                GTMultiblockBlocks.CONTROL_CIRCUIT_PARALLEL_4X.get());

        // One registration per port item, each answering with its own tier's colour. The tier is a
        // field on the item, so the answer is a constant rather than a lookup per frame.
        GTMultiblockBlocks.PORT_ITEMS.values().forEach(modes ->
                modes.values().forEach(tiers ->
                        tiers.forEach((tier, item) -> event.register(
                                (stack, tintIndex) -> tintIndex == TINT_RING
                                        ? tintOf(tier) : 0xFFFFFFFF,
                                item.get()))));
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        GTMultiblockBlocks.PORT_BLOCKS.values().forEach(modes ->
                modes.values().forEach(tiers ->
                        tiers.forEach((tier, block) -> event.register(
                                (state, level, pos, tintIndex) -> tintIndex == TINT_RING
                                        ? tintOf(tier) : 0xFFFFFFFF,
                                block.get()))));
    }
}
