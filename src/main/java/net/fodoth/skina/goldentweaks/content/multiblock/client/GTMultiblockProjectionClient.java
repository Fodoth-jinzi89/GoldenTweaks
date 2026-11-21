package net.fodoth.skina.goldentweaks.content.multiblock.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorOrientation;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTMultiblockPattern;
import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;
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
        if (GTMultiblockBlocks.structureForHost(state) == null) {
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

        ItemStack casing = new ItemStack(GTMultiblockBlocks.SHELL_PE_ITEM.get());
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        PoseStack pose = event.getPoseStack();

        for (int layer = 0; layer < layout.height(); layer++) {
            for (int row = 0; row < layout.depth(); row++) {
                for (int column = 0; column < layout.width(); column++) {
                    if (column == master.column() && layer == master.layer() && row == master.row()) {
                        continue;
                    }
                    if (layout.isWildcard(layout.symbolAt(column, layer, row))) {
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
                    itemRenderer.renderStatic(casing, ItemDisplayContext.NONE, light,
                            OverlayTexture.NO_OVERLAY, pose, buffers, level, 0);
                    pose.popPose();
                }
            }
        }

        buffers.endBatch();
    }
}
