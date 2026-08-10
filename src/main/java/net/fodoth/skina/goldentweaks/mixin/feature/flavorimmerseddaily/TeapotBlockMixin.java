package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.fidtest.block.TeapotBlock;
import com.fidtest.block.entity.TeapotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TeapotBlock.class)
public class TeapotBlockMixin {

    @Unique
    private static Item TEAPOT_COVER_ITEM = null;

    /**
     * @author Fodoth_jinzi89
     * @reason No shift required - 任意物品右键开坛，盖子封坛
     */
    @Overwrite
    protected ItemInteractionResult useItemOn(ItemStack heldItem,
                                              BlockState state,
                                              Level level,
                                              BlockPos pos,
                                              Player player,
                                              InteractionHand hand,
                                              BlockHitResult hit) {

        if (!(level.getBlockEntity(pos) instanceof TeapotBlockEntity teapot)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean sealed = state.getValue(TeapotBlock.SEALED);

        // =====================================================
        // 1. 已封坛 - 任意物品右键开坛
        // =====================================================
        if (sealed) {
            if (!level.isClientSide) {
                teapot.unseal(player);
                level.setBlock(pos, state.setValue(TeapotBlock.SEALED, false), 3);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            return ItemInteractionResult.SUCCESS;
        }

        // =====================================================
        // 2. 未封坛
        // =====================================================

        // 检查是否在工作（煮茶中）
        if (teapot.isWorking()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // ---- 茶壶盖封坛 ----
        if (isTeapotCover(heldItem)) {

            if (!level.isClientSide) {

                if (teapot.checkRecipe()) {

                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    level.setBlock(pos, state.setValue(TeapotBlock.SEALED, true), 3);
                    teapot.seal();

                    player.displayClientMessage(
                            Component.translatable("message.fidworkblock.teapot_recipe_correct"),
                            true
                    );

                    level.playSound(null, pos,
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.BLOCKS,
                            1.0F, 0.6F);

                } else {
                    player.displayClientMessage(
                            Component.translatable("message.fidworkblock.teapot_recipe_incorrect"),
                            true
                    );
                }
            }

            return ItemInteractionResult.SUCCESS;
        }

        // ---- 普通放入 ----
        if (!heldItem.isEmpty()) {

            if (!level.isClientSide) {

                ItemStack remainder = teapot.addItemToNextSlot(
                        player.getAbilities().instabuild ? heldItem.copy() : heldItem
                );

                if (!player.isCreative()) {
                    player.setItemInHand(hand, remainder);
                }

                level.playSound(null, pos,
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.BLOCKS,
                        1.0F, 0.8F);
            }

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * @author Fodoth_jinzi89
     * @reason No shift required - 空手取出产物/原料
     */
    @Overwrite
    protected InteractionResult useWithoutItem(BlockState state,
                                               Level level,
                                               BlockPos pos,
                                               Player player,
                                               BlockHitResult hit) {

        if (!(level.getBlockEntity(pos) instanceof TeapotBlockEntity teapot)) {
            return InteractionResult.PASS;
        }

        boolean sealed = state.getValue(TeapotBlock.SEALED);

        // =====================================================
        // 已封坛 - 空手开坛
        // =====================================================
        if (sealed) {
            if (!level.isClientSide) {
                teapot.unseal(player);
                level.setBlock(pos, state.setValue(TeapotBlock.SEALED, false), 3);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // =====================================================
        // 未封坛 - 检查是否在工作
        // =====================================================
        if (teapot.isWorking()) {
            return InteractionResult.PASS;
        }

        // =====================================================
        // 优先取产物
        // =====================================================
        if (teapot.hasOutput()) {

            if (!level.isClientSide) {

                ItemStack output = teapot.collectOutput();

                if (!output.isEmpty() && !player.isCreative()) {
                    player.getInventory().add(output);
                }

                level.playSound(null, pos,
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.BLOCKS,
                        0.5F, 1.0F);
            }

            return InteractionResult.SUCCESS;
        }

        // =====================================================
        // 无产物时，取出已放入的原料（无需 Shift）
        // =====================================================
        if (teapot.hasAnyInput()) {

            if (!level.isClientSide) {

                ItemStack removed = teapot.removeLastInput();

                if (!player.isCreative()) {
                    player.getInventory().add(removed);
                }

                level.playSound(null, pos,
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.BLOCKS,
                        0.5F, 1.0F);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // =========================================================
    // 茶壶盖判定 - 使用缓存 Item 引用
    // =========================================================
    @Unique
    private static boolean isTeapotCover(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (TEAPOT_COVER_ITEM == null) {
            TEAPOT_COVER_ITEM = BuiltInRegistries.ITEM.get(
                    ResourceLocation.parse("flavor_immersed_daily:teapotcover")
            );
        }

        return stack.getItem() == TEAPOT_COVER_ITEM;
    }

    @Inject(
            method = "onRemove",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldenTweaks$onRemove(BlockState state,
                                       Level level,
                                       BlockPos pos,
                                       BlockState newState,
                                       boolean moved,
                                       CallbackInfo ci) {

        // 同种方块状态变化，不处理
        if (state.is(newState.getBlock())) {
            return;
        }

        if (level.getBlockEntity(pos) instanceof TeapotBlockEntity be) {

            // 已封坛时掉落茶壶盖
            if (state.getValue(TeapotBlock.SEALED)) {
                Containers.dropItemStack(
                        level,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        new ItemStack(getTeapotCoverItem())
                );
            }

            // 掉落容器内容
            ItemStackHandler inv = be.getInventory();

            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);

                if (!stack.isEmpty()) {
                    Containers.dropItemStack(
                            level,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            stack.copy()
                    );
                }
            }

            // 红石更新
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }

        if (state.hasBlockEntity()) {
            level.removeBlockEntity(pos);
        }

        ci.cancel();
    }

    @Unique
    private static Item getTeapotCoverItem() {
        if (TEAPOT_COVER_ITEM == null) {
            TEAPOT_COVER_ITEM = BuiltInRegistries.ITEM.get(
                    ResourceLocation.parse("flavor_immersed_daily:teapotcover")
            );
        }
        return TEAPOT_COVER_ITEM;
    }

    /**
     * @author Fodoth_jinzi89
     * @reason Sable compat
     */
    @Overwrite
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean movedByPiston) {

        Containers.dropContentsOnDestroy(state, newState, level, pos);

        if (state.hasBlockEntity() && !state.is(newState.getBlock())) {
            level.removeBlockEntity(pos);
        }
    }
}