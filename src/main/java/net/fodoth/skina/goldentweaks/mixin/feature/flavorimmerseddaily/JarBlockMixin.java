package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.fidtest.block.JarBlock;
import com.fidtest.block.entity.JarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(JarBlock.class)
public class JarBlockMixin {

    // 缓存粗布物品引用
    @Unique
    private static Item CLOTH_ITEM = null;
    // =========================================================
    // 主交互（右键物品）
    // =========================================================
    /**
     * @author Fodoth_jinzi89
     * @reason No shift required
     */
    @Overwrite
    protected ItemInteractionResult useItemOn(ItemStack heldItem,
                                              BlockState state,
                                              Level level,
                                              BlockPos pos,
                                              Player player,
                                              InteractionHand hand,
                                              BlockHitResult hit) {

        if (!(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean sealed = state.getValue(JarBlock.SEALED);

        // =====================================================
        // 1. 已封坛 - 任意物品右键开坛
        // =====================================================
        if (sealed) {
            if (!level.isClientSide) {
                jar.unseal(player);
                level.setBlock(pos, state.setValue(JarBlock.SEALED, false), 3);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            return ItemInteractionResult.SUCCESS;
        }

        // =====================================================
        // 2. 未封坛
        // =====================================================

        // ---- 粗布封坛 ----
        if (isCloth(heldItem)) {

            if (!level.isClientSide) {

                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }

                if (jar.checkRecipe()) {
                    level.setBlock(pos, state.setValue(JarBlock.SEALED, true), 3);
                    jar.seal();

                    player.displayClientMessage(
                            Component.translatable("message.fidworkblock.jar_recipe_correct"),
                            true
                    );

                    level.playSound(null, pos,
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.BLOCKS,
                            1.0F, 0.6F);

                } else {
                    player.displayClientMessage(
                            Component.translatable("message.fidworkblock.jar_recipe_incorrect"),
                            true
                    );
                }
            }

            return ItemInteractionResult.SUCCESS;
        }

        // ---- 普通放入 ----
        if (!heldItem.isEmpty()) {

            if (!level.isClientSide) {

                ItemStack remainder = jar.addItemToNextSlot(
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

    // =========================================================
    // 空手交互 - 取出产物 / 取出原料
    // =========================================================
    /**
     * @author Fodoth_jinzi89
     * @reason No shift required
     */
    @Overwrite
    protected InteractionResult useWithoutItem(BlockState state,
                                               Level level,
                                               BlockPos pos,
                                               Player player,
                                               BlockHitResult hit) {

        if (!(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) {
            return InteractionResult.PASS;
        }

        boolean sealed = state.getValue(JarBlock.SEALED);

        // =====================================================
        // 已封坛 - 空手开坛
        // =====================================================
        if (sealed) {
            if (!level.isClientSide) {
                jar.unseal(player);
                level.setBlock(pos, state.setValue(JarBlock.SEALED, false), 3);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // =====================================================
        // 未封坛 - 优先取产物
        // =====================================================
        if (jar.hasOutput()) {

            if (!level.isClientSide) {

                ItemStack out = jar.collectOutput();

                if (!out.isEmpty() && !player.isCreative()) {
                    player.getInventory().add(out);
                }

                level.playSound(null, pos,
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.BLOCKS,
                        0.5F, 1.0F);
            }

            return InteractionResult.SUCCESS;
        }

        // =====================================================
        // 无产物时，空手取出已放入的原料（无需 Shift）
        // =====================================================
        if (jar.hasAnyInput()) {

            if (!level.isClientSide) {

                ItemStack removed = jar.removeLastInput();

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
    // cloth 判定
    // =========================================================
    @Unique
    private static boolean isCloth(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        // 方式1：缓存 Item 引用（推荐，性能最好）
        if (CLOTH_ITEM == null) {
            // 从 BuiltInRegistries 获取粗布物品
            CLOTH_ITEM = BuiltInRegistries.ITEM.get(
                    net.minecraft.resources.ResourceLocation.parse("flavor_immersed_daily:coarsecloth")
            );
        }
        return stack.getItem() == CLOTH_ITEM;
    }
}