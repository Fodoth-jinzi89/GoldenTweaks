package net.fodoth.skina.goldentweaks.content.multiblock.common;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.network.packet.S2CShowMultiblockProjectionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Projector left-click behaviour for the {@code avaritia_more_items} host.
 *
 * <p>Lives in the compat package because it talks to {@link GTMachineBlockEntity}
 * directly. Registering it only when AMI is present is what keeps the common projector code free
 * of that dependency.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class GTProjectorHostHandler {

    private GTProjectorHostHandler() {
    }

    /**
     * Re-aims, spins or mirrors the kettle depending on where it was hit.
     *
     * <p>Only {@code START} is handled: a held button fires repeatedly, and a machine that turned
     * on every repeat would be unusable.</p>
     */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) {
            return;
        }

        Player player = event.getEntity();
        if (!player.getMainHandItem().is(GTMultiblockBlocks.PROJECTOR.get())) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof GTMachineBlock)) {
            return;
        }

        Direction face = event.getFace();
        // A sneak-left-click can arrive without a face (the client then reports no hit side), and
        // the mirror toggle is the one action that does not need it. Everything else falls back to
        // the side the player is looking at, which is the face that was effectively clicked.
        if (face == null) {
            face = player.getDirection().getOpposite();
        }

        BlockState rotated = GTMultiblockBlocks.rotateHost(state, face, player.isShiftKeyDown());
        if (rotated == null) {
            return;
        }

        if (!level.isClientSide) {
            level.setBlock(pos, rotated, 3);
            if (level.getBlockEntity(pos) instanceof GTMachineBlockEntity tile) {
                // The stored shape was matched against the old orientation, and an orientation
                // change is not something the periodic validity check can notice, so the machine
                // has to be matched again from scratch.
                tile.reorient();
            }
        }

        // The projector redirects the click; it must not also start mining the block. Cancelling
        // is what actually suppresses that: both the client and the server only check
        // `isCanceled()`, and in creative the destroy happens without any progress check, so
        // clearing `useBlock` alone would still remove the host.
        event.setCanceled(true);
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
    }

    /**
     * Builds the structure for the player when the projector is used on a host while sneaking.
     *
     * <p>Sneaking plus right click is the natural "do the setup for me" gesture and cannot be
     * confused with opening the GUI, which is a plain right click. The projector's own building
     * pass only fills casing cells, so ports stay the player's job.</p>
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isShiftKeyDown()
                || !player.getMainHandItem().is(GTMultiblockBlocks.PROJECTOR.get())) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof GTMachineBlock)) {
            return;
        }
        if (level.isClientSide) {
            // Let the client report success so the arm swing matches, but do not build there.
            event.setUseBlock(TriState.FALSE);
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof GTMachineBlockEntity tile)) {
            return;
        }

        GTMultiblockBuilder.Result result = tile.buildStructure(player);

        // Report by outcome, not by count. Nothing to place means the casing was already complete,
        // which is a success, not a failure — saying "nothing to place" would be wrong there and
        // would fire on every repeat build in creative, where the supply is unlimited.
        if (result.placed() > 0) {
            player.displayClientMessage(
                    Component.translatable("gui.goldentweaks.chemical_reactor.built", result.placed()), true);
        } else if (result.missing() > 0) {
            // Cells are still empty *and* nothing could be added: the player has no casing left.
            player.displayClientMessage(
                    Component.translatable("gui.goldentweaks.chemical_reactor.build_no_casing"), true);
        }

        // Only an unfinished build needs the projection: it shows which cells are still empty. A
        // build that filled everything would just flash and vanish once the machine forms, so
        // nothing is sent in that case.
        if (result.incomplete() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new S2CShowMultiblockProjectionPacket(pos));
        }

        // Never let the sneak-right-click reach the block: that would open the GUI instead.
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
        event.setCanceled(true);
    }
}
