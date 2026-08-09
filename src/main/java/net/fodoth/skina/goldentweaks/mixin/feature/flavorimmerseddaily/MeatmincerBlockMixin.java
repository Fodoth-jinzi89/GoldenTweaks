package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.fidtest.block.MeatmincerBlock;
import com.fidtest.block.entity.MeatmincerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MeatmincerBlock.class)
public class MeatmincerBlockMixin {

    /**
     * @reason 去掉潜行要求，直接右键取下盖子或取出物品
     * @author Fodoth_jinzi89
     */
    @Overwrite
    public ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MeatmincerBlockEntity mincer)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean sealed = state.getValue(MeatmincerBlock.SEALED);

        // === 密封状态 ===
        if (sealed) {
            // 直接取下盖子（不需要潜行）
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            mincer.unseal(player);
            level.setBlock(pos, state.setValue(MeatmincerBlock.SEALED, false), 3);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            return ItemInteractionResult.SUCCESS;
        }

        // === 未密封状态 ===

        // 正在工作
        if (mincer.isWorking()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 手持物品
        if (!heldItem.isEmpty()) {
            // 手持盖子
            if (isMincerCover(heldItem)) {
                if (level.isClientSide) {
                    return ItemInteractionResult.SUCCESS;
                }
                if (mincer.checkRecipe()) {
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    level.setBlock(pos, state.setValue(MeatmincerBlock.SEALED, true), 3);
                    mincer.seal();
                    player.displayClientMessage(
                            Component.translatable("message.fidworkblock.meatmincer_recipe_correct"),
                            true
                    );
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 0.6F);
                } else {
                    player.displayClientMessage(
                            Component.translatable("message.fidworkblock.meatmincer_recipe_incorrect"),
                            true
                    );
                }
                return ItemInteractionResult.SUCCESS;
            }

            // 放入物品
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            ItemStack remainder = mincer.addItemToSlot(
                    player.getAbilities().instabuild ? heldItem.copy() : heldItem
            );
            if (!player.isCreative()) {
                player.setItemInHand(hand, remainder);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 0.8F);
            return ItemInteractionResult.SUCCESS;
        }

        // 空手：优先取出输入
        if (mincer.hasAnyInput()) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            ItemStack removed = mincer.removeInput();
            if (!removed.isEmpty() && !player.isCreative()) {
                player.getInventory().add(removed);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            return ItemInteractionResult.SUCCESS;
        }

        // 取出输出
        if (mincer.hasOutput()) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            ItemStack output = mincer.collectOutput();
            if (!output.isEmpty() && !player.isCreative()) {
                player.getInventory().add(output);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * @reason 去掉潜行要求，直接右键取下盖子或取出物品
     * @author Fodoth_jinzi89
     */
    @Overwrite
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MeatmincerBlockEntity mincer)) {
            return InteractionResult.PASS;
        }

        boolean sealed = state.getValue(MeatmincerBlock.SEALED);

        // === 密封状态 ===
        if (sealed) {
            // 直接取下盖子（不需要潜行）
            if (level.isClientSide) {
                return InteractionResult.CONSUME;
            }
            mincer.unseal(player);
            level.setBlock(pos, state.setValue(MeatmincerBlock.SEALED, false), 3);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        // === 未密封状态 ===

        // 正在工作
        if (mincer.isWorking()) {
            return InteractionResult.PASS;
        }

        // 空手：优先取出输入
        if (mincer.hasAnyInput()) {
            if (level.isClientSide) {
                return InteractionResult.CONSUME;
            }
            ItemStack removed = mincer.removeInput();
            if (!removed.isEmpty() && !player.isCreative()) {
                player.getInventory().add(removed);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        // 取出输出
        if (mincer.hasOutput()) {
            if (level.isClientSide) {
                return InteractionResult.CONSUME;
            }
            ItemStack output = mincer.collectOutput();
            if (!output.isEmpty() && !player.isCreative()) {
                player.getInventory().add(output);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /**
     * 判断是否是绞肉机盖子
     */
    @Unique
    private static boolean isMincerCover(ItemStack stack) {
        return !stack.isEmpty() && stack.is(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("flavor_immersed_daily", "mincer_cover")));
    }
}