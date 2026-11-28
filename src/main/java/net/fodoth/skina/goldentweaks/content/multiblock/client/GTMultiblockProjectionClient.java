package net.fodoth.skina.goldentweaks.content.multiblock.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.basic_chemical_plant.GTChemicalReactorOrientation;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockBuilder;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockPattern;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTMioIcifStructures;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Client half of the projector: toggling the projection and drawing it.
 *
 * <p>The projection is never a block in the world. Every cell of the structure is drawn as one
 * shrunken block model, exactly the way ValkyrieLib previews its multiblocks; a cell that already
 * holds the real block simply hides the model inside it, so only the missing parts are visible.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTMultiblockProjectionClient {

    /** Same shrink the reference preview uses, relative to one full block. */
    private static final float PROJECTION_SCALE = 0.625F;

    /** Beyond this distance the host is too far away to be worth drawing. */
    private static final double VISIBLE_RANGE_SQR = 64.0D * 64.0D;

    private GTMultiblockProjectionClient() {
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(GTProjectionStructures.INSTANCE);
    }

    /**
     * Plain right-click with the projector toggles the projection.
     *
     * <p>Sneak-right-click is left to the builder, which fills in the casing from the player's
     * inventory; left-click already re-aims the host.</p>
     */
    @SubscribeEvent(receiveCanceled = true)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().isShiftKeyDown()
                || !event.getItemStack().is(GTMultiblockBlocks.PROJECTOR.get())) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!GTMultiblockBlocks.hasProjection(state)) {
            return;
        }

        if (event.getLevel().isClientSide) {
            GTMultiblockProjection.toggle(event.getPos());
        }

        // The projector consumes the click: a plain right click would otherwise open the GUI.
        event.setCanceled(true);
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            GTMultiblockProjection.clear();
            return;
        }

        // Age out the projection before anything else, so an expired one stops drawing even if the
        // rest of this method would have returned early.
        GTMultiblockProjection.tick();

        BlockPos hostPos = GTMultiblockProjection.hostPos();
        if (hostPos == null) {
            return;
        }
        if (!level.dimension().equals(GTMultiblockProjection.dimension())) {
            GTMultiblockProjection.clear();
            return;
        }
        if (!level.isLoaded(hostPos)) {
            return;
        }

        BlockState hostState = level.getBlockState(hostPos);

        // Singularity Iteration controllers carry no structure file; their shapes come from SI's
        // own blueprints, and their cells are offsets rather than our layer/row/column indices.
        if (GTMioIcifStructures.available()) {
            GTMioIcifStructures.Layout siLayout = GTMioIcifStructures.layoutFor(hostState);
            if (siLayout != null) {
                drawSiProjection(level, hostPos, hostState, siLayout, event, minecraft);
                return;
            }
        }

        ResourceLocation structureId = GTMultiblockBlocks.structureForHost(hostState);
        GTMultiblockBlocks.HostOrientation orientation = GTMultiblockBlocks.hostOrientation(hostState);
        if (structureId == null || orientation == null || orientation.formed()) {
            GTMultiblockProjection.clear();
            return;
        }

        GTMultiblockPattern layout = GTProjectionStructures.INSTANCE.layout(structureId);
        if (layout == null) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        if (camera.distanceToSqr(
                hostPos.getX() + 0.5D, hostPos.getY() + 0.5D, hostPos.getZ() + 0.5D) > VISIBLE_RANGE_SQR) {
            return;
        }

        Direction forwards = GTChemicalReactorOrientation.patternForwards(orientation.facing());
        Direction up = GTChemicalReactorOrientation.up(orientation.facing(), orientation.roll());

        GTMultiblockPattern.Master master = layout.master();
        BlockPos anchor = GTChemicalReactorOrientation.frontTopLeft(
                hostPos, forwards, up, orientation.mirrored(),
                master.column(),
                layout.height() - 1 - master.layer(),
                layout.depth() - 1 - master.row());

        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        PoseStack pose = event.getPoseStack();

        for (int layer = 0; layer < layout.height(); layer++) {
            for (int row = 0; row < layout.depth(); row++) {
                for (int column = 0; column < layout.width(); column++) {
                    if (column == master.column() && layer == master.layer() && row == master.row()) {
                        continue;
                    }
                    char symbol = layout.symbolAt(column, layer, row);
                    // Each cell previews with what its own letter means. A cell that accepts a port
                    // or a casing is shown as the casing: the preview is there to say "something
                    // goes here", and the port is the player's choice to make.
                    Block ghost = GTMultiblockBuilder.casingFor(layout, symbol);
                    if (ghost == null) {
                        continue;
                    }
                    BlockPos pos = GTChemicalReactorOrientation.cell(
                            anchor, forwards, up, orientation.mirrored(),
                            layout.height(), layout.depth(), column, layer, row);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }

                    pose.pushPose();
                    pose.translate(pos.getX() + 0.5D - camera.x,
                            pos.getY() + 0.5D - camera.y,
                            pos.getZ() + 0.5D - camera.z);
                    pose.scale(PROJECTION_SCALE, PROJECTION_SCALE, PROJECTION_SCALE);
                    int light = LevelRenderer.getLightColor(level, pos);
                    itemRenderer.renderStatic(new ItemStack(ghost), ItemDisplayContext.NONE, light,
                            OverlayTexture.NO_OVERLAY, pose, buffers, level, 0);
                    pose.popPose();
                }
            }
        }

        buffers.endBatch();
    }

    /**
     * Draws the projection of a Singularity Iteration multiblock.
     *
     * <p>SI's cells are offsets from the controller, and SI's own validator is the only thing that
     * decides whether the machine forms — so the cells for the controller's current facing are
     * exactly what the player must be shown. The steam tunnel generates its shape per facing, and
     * the fusion tokamak turns its offsets, both already reflected in {@code cells(facing)}.</p>
     *
     * <p>A cell that already holds its block is skipped rather than drawn: the point of the
     * projection is the parts that are still missing.</p>
     */
    private static void drawSiProjection(ClientLevel level, BlockPos hostPos, BlockState hostState,
                                         GTMioIcifStructures.Layout layout, RenderLevelStageEvent event,
                                         Minecraft minecraft) {
        if (GTMioIcifStructures.formed(hostState)) {
            GTMultiblockProjection.clear();
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        if (camera.distanceToSqr(
                hostPos.getX() + 0.5D, hostPos.getY() + 0.5D, hostPos.getZ() + 0.5D) > VISIBLE_RANGE_SQR) {
            return;
        }

        Direction facing = GTMioIcifStructures.facing(hostState);

        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        PoseStack pose = event.getPoseStack();

        for (GTMioIcifStructures.Cell cell : layout.cells(facing)) {
            BlockPos pos = hostPos.offset(cell.x(), cell.y(), cell.z());
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (level.getBlockState(pos).is(cell.block())) {
                continue;
            }

            pose.pushPose();
            pose.translate(pos.getX() + 0.5D - camera.x,
                    pos.getY() + 0.5D - camera.y,
                    pos.getZ() + 0.5D - camera.z);
            pose.scale(PROJECTION_SCALE, PROJECTION_SCALE, PROJECTION_SCALE);
            int light = LevelRenderer.getLightColor(level, pos);
            itemRenderer.renderStatic(new ItemStack(cell.block()), ItemDisplayContext.NONE, light,
                    OverlayTexture.NO_OVERLAY, pose, buffers, level, 0);
            pose.popPose();
        }

        buffers.endBatch();
    }
}
