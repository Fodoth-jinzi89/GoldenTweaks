package net.fodoth.skina.goldentweaks.content.multiblock;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Keeps a plain projector right-click on a multiblock host from reaching the block.
 *
 * <p>The client's cancellation only stops the local interaction; the use packet is still sent, so
 * the server would open the host GUI anyway. This handler cancels on both sides. Sneak-right-click
 * is left alone because that is the builder's gesture.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class GTProjectorRightClickHandler {

    private GTProjectorRightClickHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().isShiftKeyDown()
                || !event.getItemStack().is(GTMultiblockBlocks.PROJECTOR.get())) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (GTMultiblockBlocks.structureForHost(state) == null) {
            return;
        }

        event.setCanceled(true);
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
    }
}
