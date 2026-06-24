package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.fidtest.block.SteamboxBlock;
import com.fidtest.block.entity.SteamboxBlockEntity;
import com.fidtest.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SteamboxBlock.class)
public class SteamboxBlockMixin {


    @Unique
    private static final int MAX_LAYERS = 16;

    @Unique
    private static final ResourceLocation TIDY_WATER_ID = ResourceLocation.fromNamespaceAndPath("flavor_immersed_daily", "tidywater");



    /* =========================================================
     * 转接 useWithoutItem 到我们的逻辑
     * ========================================================= */
    @Inject(
            method = "useWithoutItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldenTweaks$handleUseWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (level.isClientSide) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        goldenTweaks$handleEmptyHandDirect(level, state, pos, player, hit, cir);
    }

    /* =========================================================
     * 完全接管物品右键点击（包括空手）
     * ========================================================= */
    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldenTweaks$handleUseItemOn(
            ItemStack held,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (level.isClientSide) {
            cir.setReturnValue(ItemInteractionResult.CONSUME);
            return;
        }

        // 空手处理
        if (held.isEmpty() || held.getItem() == Items.AIR) {
            goldenTweaks$handleEmptyHand(level, state, pos, player, hit, cir);
            return;
        }

        // 特殊水处理
        if (goldenTweaks$isTidyWater(held)) {
            goldenTweaks$handleTidyWater(level, pos, player, held, cir);
            return;
        }

        // 蒸笼堆叠处理
        if (held.is(ModBlocks.STEAMBOX.asItem())) {
            if (player.isSprinting()) {
                // Shift+右键：一次放置多层（最多堆叠数量）
                goldenTweaks$placeMultipleSteambox(level, state, pos, player, held, hit, cir);
            } else {
                // 普通右键：放置单个蒸笼
                goldenTweaks$placeSingleSteambox(level, state, pos, player, held, hit, cir);
            }
        }
    }

    /* =========================================================
     * 处理空手交互（用于 useItemOn）
     * ========================================================= */
    @Unique
    private void goldenTweaks$handleEmptyHand(
            Level level,
            BlockState state,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        BlockPos bottom = goldenTweaks$findBottom(level, pos);
        if (!pos.equals(bottom)) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }

        List<BlockPos> layers = goldenTweaks$collectLayers(level, bottom);
        if (layers.isEmpty()) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }

        if (goldenTweaks$anyWorking(level, layers)) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }

        if (goldenTweaks$checkOutput(level, layers)) {
            goldenTweaks$harvest(level, layers, player);
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
            return;
        }

        if (goldenTweaks$allPacked(level, layers)) {
            goldenTweaks$unpack(level, layers, player);
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
            return;
        }

        if (goldenTweaks$anyInput(level, layers)) {
            goldenTweaks$packAllLayers(level, layers, player);
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
            return;
        }

        cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    /* =========================================================
     * 处理空手交互（用于 useWithoutItem）
     * ========================================================= */
    @Unique
    private void goldenTweaks$handleEmptyHandDirect(
            Level level,
            BlockState state,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        BlockPos bottom = goldenTweaks$findBottom(level, pos);
        if (!pos.equals(bottom)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        List<BlockPos> layers = goldenTweaks$collectLayers(level, bottom);
        if (layers.isEmpty()) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        if (goldenTweaks$anyWorking(level, layers)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        if (goldenTweaks$checkOutput(level, layers)) {
            goldenTweaks$harvest(level, layers, player);
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        if (goldenTweaks$allPacked(level, layers)) {
            goldenTweaks$unpack(level, layers, player);
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        if (goldenTweaks$anyInput(level, layers)) {
            goldenTweaks$packAllLayers(level, layers, player);
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        cir.setReturnValue(InteractionResult.PASS);
    }

    /* =========================================================
     * 处理特殊水
     * ========================================================= */
    @Unique
    private void goldenTweaks$handleTidyWater(
            Level level,
            BlockPos clickedPos,
            Player player,
            ItemStack held,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        BlockPos bottom = goldenTweaks$findBottom(level, clickedPos);
        if (!clickedPos.equals(bottom)) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }

        List<BlockPos> layers = goldenTweaks$collectLayers(level, bottom);
        if (layers.isEmpty()) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }

        if (goldenTweaks$anyWorking(level, layers)) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }

        if (!goldenTweaks$allPacked(level, layers)) {
            player.displayClientMessage(
                    Component.translatable("message.fidworkblock.steambox_need_pack_first"),
                    true
            );
            cir.setReturnValue(ItemInteractionResult.FAIL);
            return;
        }

        int height = layers.size();
        if (held.getCount() < height) {
            player.displayClientMessage(
                    Component.translatable("message.fidworkblock.steambox_need_water", height),
                    true
            );
            cir.setReturnValue(ItemInteractionResult.FAIL);
            return;
        }

        if (!player.isCreative()) {
            held.shrink(height);
        }

        goldenTweaks$startAllLayers(level, layers);
        cir.setReturnValue(ItemInteractionResult.SUCCESS);
    }

    /* =========================================================
     * 放置单个蒸笼（普通右键）- 支持带数据的物品
     * ========================================================= */
    @Unique
    private void goldenTweaks$placeSingleSteambox(
            Level level,
            BlockState state,
            BlockPos clickedPos,
            Player player,
            ItemStack held,
            BlockHitResult hit,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (hit.getDirection() == Direction.DOWN) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }

        BlockPos bottom = goldenTweaks$findBottom(level, clickedPos);
        if (bottom == null) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }

        List<BlockPos> layers = goldenTweaks$collectLayers(level, bottom);
        int currentHeight = layers.size();

        if (currentHeight >= MAX_LAYERS) {
            player.displayClientMessage(
                    Component.translatable("message.fidworkblock.steambox_max_layers", MAX_LAYERS),
                    true
            );
            cir.setReturnValue(ItemInteractionResult.FAIL);
            return;
        }

        if (goldenTweaks$placeOneSteambox(level, state, bottom, currentHeight, player, held)) {
            player.displayClientMessage(
                    Component.translatable("message.fidworkblock.steambox_placed_layer",
                            currentHeight + 1, MAX_LAYERS),
                    true
            );
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
        } else {
            cir.setReturnValue(ItemInteractionResult.FAIL);
        }
    }

    /* =========================================================
     * 放置多个蒸笼（Shift+右键）- 支持堆叠数据的批量放置
     * ========================================================= */
    @Unique
    private void goldenTweaks$placeMultipleSteambox(
            Level level,
            BlockState state,
            BlockPos clickedPos,
            Player player,
            ItemStack held,
            BlockHitResult hit,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (hit.getDirection() == Direction.DOWN) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }

        BlockPos bottom = goldenTweaks$findBottom(level, clickedPos);
        if (bottom == null) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }

        List<BlockPos> layers = goldenTweaks$collectLayers(level, bottom);
        int currentHeight = layers.size();

        if (currentHeight >= MAX_LAYERS) {
            player.displayClientMessage(
                    Component.translatable("message.fidworkblock.steambox_max_layers", MAX_LAYERS),
                    true
            );
            cir.setReturnValue(ItemInteractionResult.FAIL);
            return;
        }

        // 计算可以放置的数量：受限于堆叠数量、最大高度和是否创造模式
        int maxPlace = MAX_LAYERS - currentHeight;
        int toPlace = Math.min(maxPlace, player.isCreative() ? maxPlace : held.getCount());

        if (toPlace <= 0) {
            cir.setReturnValue(ItemInteractionResult.FAIL);
            return;
        }

        // 保存模板数据（所有堆叠的蒸笼数据相同）
        boolean isPacked = SteamboxBlock.isPackedItem(held);
        ItemStack templateStack = held.copy();

        int placed = 0;
        BlockPos placePos = bottom.above(currentHeight);
        Direction facing = state.getValue(SteamboxBlock.FACING);

        for (int i = 0; i < toPlace; i++) {
            if (!level.getBlockState(placePos).isAir()) {
                break;
            }

            // 每个新蒸笼使用模板数据的副本
            ItemStack newStack = templateStack.copy();

            BlockState newState = ModBlocks.STEAMBOX.get().defaultBlockState()
                    .setValue(SteamboxBlock.BOTTOM, false)
                    .setValue(SteamboxBlock.PACKED, isPacked)
                    .setValue(SteamboxBlock.WORKING, false)
                    .setValue(SteamboxBlock.FACING, facing);

            level.setBlock(placePos, newState, 3);

            // 如果有数据，复制到方块实体
            if (isPacked) {
                BlockEntity be = level.getBlockEntity(placePos);
                if (be instanceof SteamboxBlockEntity sb) {
                    sb.loadFromItemStack(newStack);
                    sb.setPacked(true);
                    sb.setChanged();
                }
            }

            level.playSound(null, placePos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

            placed++;
            placePos = placePos.above();

            if (!player.isCreative()) {
                held.shrink(1);
                if (held.isEmpty()) {
                    break;
                }
            }
        }

        if (placed > 0) {
            player.displayClientMessage(
                    Component.translatable("message.fidworkblock.steambox_placed_layers",
                            placed, currentHeight + placed, MAX_LAYERS),
                    true
            );
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
        } else {
            cir.setReturnValue(ItemInteractionResult.FAIL);
        }
    }

    /* =========================================================
     * 放置一个蒸笼的通用方法 - 支持带数据的物品
     * ========================================================= */
    @Unique
    private boolean goldenTweaks$placeOneSteambox(
            Level level,
            BlockState state,
            BlockPos bottom,
            int currentHeight,
            Player player,
            ItemStack held
    ) {
        BlockPos placePos = bottom.above(currentHeight);
        if (!level.getBlockState(placePos).isAir()) {
            return false;
        }

        boolean isPacked = SteamboxBlock.isPackedItem(held);
        ItemStack dataStack = held.copy();

        BlockState newState = ModBlocks.STEAMBOX.get().defaultBlockState()
                .setValue(SteamboxBlock.BOTTOM, false)
                .setValue(SteamboxBlock.PACKED, isPacked)
                .setValue(SteamboxBlock.WORKING, false)
                .setValue(SteamboxBlock.FACING, state.getValue(SteamboxBlock.FACING));

        level.setBlock(placePos, newState, 3);

        // 如果有数据，复制到方块实体
        if (isPacked) {
            BlockEntity be = level.getBlockEntity(placePos);
            if (be instanceof SteamboxBlockEntity sb) {
                sb.loadFromItemStack(dataStack);
                sb.setPacked(true);
                sb.setChanged();
            }
        }

        level.playSound(null, placePos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (!player.isCreative()) {
            held.shrink(1);
        }

        return true;
    }

    /* =========================================================
     * 收集所有层
     * ========================================================= */
    @Unique
    private List<BlockPos> goldenTweaks$collectLayers(Level level, BlockPos bottom) {
        List<BlockPos> list = new ArrayList<>();
        BlockPos p = bottom;
        int i = 0;

        while (i < MAX_LAYERS && level.getBlockState(p).is(ModBlocks.STEAMBOX.get())) {
            list.add(p);
            p = p.above();
            i++;
        }

        return list;
    }

    /* =========================================================
     * 查找最底层
     * ========================================================= */
    @Unique
    private BlockPos goldenTweaks$findBottom(Level level, BlockPos pos) {
        BlockPos p = pos;
        while (level.getBlockState(p.below()).is(ModBlocks.STEAMBOX.get())) {
            p = p.below();
        }
        return level.getBlockState(p).is(ModBlocks.STEAMBOX.get()) ? p : null;
    }

    /* =========================================================
     * 状态检查方法
     * ========================================================= */
    @Unique
    private boolean goldenTweaks$anyInput(Level level, List<BlockPos> layers) {
        for (BlockPos p : layers) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof SteamboxBlockEntity sb && sb.hasAnyInput()) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean goldenTweaks$checkOutput(Level level, List<BlockPos> layers) {
        for (BlockPos p : layers) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof SteamboxBlockEntity entity && entity.hasOutput()) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean goldenTweaks$allPacked(Level level, List<BlockPos> layers) {
        for (BlockPos p : layers) {
            BlockState state = level.getBlockState(p);
            if (!state.getValue(SteamboxBlock.PACKED)) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private boolean goldenTweaks$anyWorking(Level level, List<BlockPos> layers) {
        for (BlockPos p : layers) {
            BlockState state = level.getBlockState(p);
            if (state.getValue(SteamboxBlock.WORKING)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean goldenTweaks$isTidyWater(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return itemId.equals(TIDY_WATER_ID);
    }

    /* =========================================================
     * PACK ALL - 打包所有层
     * ========================================================= */
    @Unique
    private void goldenTweaks$packAllLayers(Level level, List<BlockPos> layers, @Nullable Player player) {
        int highestNonEmpty = -1;

        // 找最高非空层
        for (int i = 0; i < layers.size(); i++) {
            BlockEntity be = level.getBlockEntity(layers.get(i));

            if (be instanceof SteamboxBlockEntity sb && sb.hasAnyInput()) {
                highestNonEmpty = i;
            }
        }

        // 全空，直接返回
        if (highestNonEmpty < 0) {
            return;
        }

        // 删除顶部空层并返还蒸笼
        for (int i = layers.size() - 1; i > highestNonEmpty; i--) {
            BlockPos pos = layers.get(i);

            ItemStack drop = new ItemStack(ModBlocks.STEAMBOX.asItem());

            if (player != null && !(player instanceof FakePlayer)) {
                goldenTweaks$give(player, drop);
            } else {
                Containers.dropItemStack(
                        level,
                        pos.getX() + 0.5,
                        pos.getY() + 1.0,
                        pos.getZ() + 0.5,
                        drop
                );
            }

            level.removeBlock(pos, false);
        }

        // 打包保留层
        for (int i = 0; i <= highestNonEmpty; i++) {
            BlockPos pos = layers.get(i);

            BlockState state = level.getBlockState(pos);
            level.setBlock(
                    pos,
                    state.setValue(SteamboxBlock.PACKED, true),
                    3
            );

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SteamboxBlockEntity sb) {
                sb.setPacked(true);
                sb.setChanged();
            }
        }
    }

    /* =========================================================
     * UNPACK - 收回所有层（带数据）
     * ========================================================= */
    @Unique
    private void goldenTweaks$unpack(Level level, List<BlockPos> layers, Player player) {
        for (int i = layers.size() - 1; i >= 0; i--) {
            BlockPos p = layers.get(i);
            BlockEntity be = level.getBlockEntity(p);
            ItemStack stack = new ItemStack(ModBlocks.STEAMBOX.asItem());

            if (be instanceof SteamboxBlockEntity sb) {
                sb.saveToItemStack(stack);
                sb.clearItems();
            }

            goldenTweaks$give(player, stack);
            level.removeBlock(p, false);
        }
    }

    /* =========================================================
     * HARVEST - 收获所有产物
     * ========================================================= */
    @Unique
    private void goldenTweaks$harvest(Level level, List<BlockPos> layers, Player player) {
        BlockPos bottomPos = layers.getFirst();
        List<ItemStack> allItems = new ArrayList<>();

        for (int i = layers.size() - 1; i >= 0; i--) {
            BlockPos p = layers.get(i);
            BlockEntity be = level.getBlockEntity(p);

            if (be instanceof SteamboxBlockEntity sb) {
                allItems.addAll(sb.getAllItems());
                sb.clearItems();
            }
        }

        for (ItemStack s : allItems) {
            Containers.dropItemStack(
                    level,
                    bottomPos.getX() + 0.5,
                    bottomPos.getY() + 0.5,
                    bottomPos.getZ() + 0.5,
                    s
            );
        }

        for (int i = layers.size() - 1; i >= 0; i--) {
            BlockPos p = layers.get(i);
            goldenTweaks$give(player, new ItemStack(ModBlocks.STEAMBOX.asItem()));
            level.removeBlock(p, false);
        }

        level.playSound(null, bottomPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
    }

    /* =========================================================
     * START WORK - 启动所有层的工作
     * ========================================================= */
    @Unique
    private void goldenTweaks$startAllLayers(Level level, List<BlockPos> layers) {
        int maxTime = 0;

        for (BlockPos p : layers) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof SteamboxBlockEntity sb) {
                if (sb.startProcessing()) {
                    maxTime = Math.max(maxTime, sb.getTotalProcessTime());
                }
            }
        }

        for (BlockPos p : layers) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof SteamboxBlockEntity sb) {
                sb.setTotalProcessTime(maxTime);
            }
        }

        for (BlockPos p : layers) {
            BlockState state = level.getBlockState(p);
            level.setBlock(p, state.setValue(SteamboxBlock.WORKING, true), 3);
        }
    }

    /* =========================================================
     * Helper 方法
     * ========================================================= */
    @Unique
    private void goldenTweaks$give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}