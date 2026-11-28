package net.fodoth.skina.goldentweaks.compat.mioicif;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockBlocks;
import net.fodoth.skina.goldentweaks.network.packet.S2CShowMultiblockProjectionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Projector right-click on a Singularity Iteration multiblock controller.
 *
 * <p>A plain right click on an SI controller opens its GUI. The projector must therefore take the
 * click away before the block sees it — and it has to do so on the server as well, because the
 * client's own cancellation only suppresses the local interaction while the use packet still
 * travels.</p>
 *
 * <p>Sneak-right-click builds the shape instead, the same gesture used on our own machines.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class GTMioIcifProjectorHandler {

    private GTMioIcifProjectorHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.getMainHandItem().is(GTMultiblockBlocks.PROJECTOR.get())) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!GTMultiblockBlocks.isMioIcifHost(state)) {
            return;
        }

        if (level.isClientSide) {
            // The projection itself is toggled by the client-only handler in
            // GTMultiblockProjectionClient; here we only make sure the click never reaches the
            // block, which is what stops SI's own GUI from opening.
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);
            event.setCanceled(true);
            return;
        }

        if (player.isShiftKeyDown()) {
            build(level, pos, state, player);
        }

        // Cancelling on the server too is what actually stops the GUI from opening.
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
        event.setCanceled(true);
    }

    /** Fills the shape with its own shell blocks, then shows whatever is still missing. */
    private static void build(Level level, BlockPos pos, BlockState state, Player player) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        GTMioIcifStructures.BuildResult result = GTMioIcifStructures.build(level, pos, state, player);

        if (result.placed() > 0) {
            player.displayClientMessage(
                    Component.translatable("gui.goldentweaks.chemical_reactor.built", result.placed()),
                    true);
        } else if (result.missing() > 0) {
            player.displayClientMessage(
                    Component.translatable("gui.goldentweaks.chemical_reactor.build_no_casing"), true);
        }

        if (result.incomplete() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new S2CShowMultiblockProjectionPacket(pos));
        }
    }
}
