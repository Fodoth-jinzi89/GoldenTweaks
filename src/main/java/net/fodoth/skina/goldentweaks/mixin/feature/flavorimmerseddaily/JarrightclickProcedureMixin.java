package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModItems;
import net.mcreator.flavorimmerseddaily.procedures.Jar_roundProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.mcreator.flavorimmerseddaily.procedures.JarrightclickProcedure")
public class JarrightclickProcedureMixin {

    /**
     * @reason 重写坛子右键交互逻辑，简化并优化行为
     * @author Fodoth_jinzi89
     */
    @Overwrite
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null) {
            return;
        }

        BlockPos pos = BlockPos.containing(x, y, z);

        // 获取坛子当前状态
        int currentState = getBlockState(world, pos);

        // 获取主手物品
        ItemStack mainHand = ItemStack.EMPTY;
        if (entity instanceof LivingEntity) {
            mainHand = ((LivingEntity) entity).getMainHandItem();
        }

        // 检查是否手持粗布
        boolean hasCloth = mainHand.getItem() == FlavorImmersedDailyModItems.COARSECLOTH.get();
        // 检查是否手持钳子
        boolean hasPincers = mainHand.getItem() == FlavorImmersedDailyModItems.PINCERS.get();

        // === 情况1：坛子状态为0（空/未密封） ===
        if (currentState == 0) {

            // 1.1 手持粗布 → 密封坛子，触发合成
            if (hasCloth) {
                // 消耗1个粗布
                mainHand.shrink(1);

                // 将坛子状态设为1（密封）
                setBlockState(world, pos, 1);

                // 播放放置音效
                playSound(world, x, y, z, "block.wool.place");

                // 触发合成检查
                Jar_roundProcedure.execute(world, x, y, z);

                // 重置玩家标记（防止重复触发）
                entity.getPersistentData().putString("JAR", "NO");
                return;
            }

            // 1.2 手持其他物品 → 尝试放入坛子
            if (!mainHand.isEmpty() && !hasPincers) {
                // 查找第一个空槽位（0-3）
                int emptySlot = findEmptySlot(world, pos);

                if (emptySlot != -1) {
                    // 将手中所有物品放入空槽位
                    putItemIntoSlot(world, pos, emptySlot, mainHand);

                    // 播放装填音效
                    playSound(world, x, y, z, "block.composter.fill");

                    // 清空手中的物品（已全部放入）
                    mainHand.setCount(0);
                }
            }
            return;
        }

        // === 情况2：坛子状态为1（密封/有产物） ===
        if (currentState == 1) {
            // 打开坛子，取出所有物品并返还粗布

            // 2.1 从槽位0-4取出所有物品，从坛子顶部弹出
            spawnAllItems(world, pos, entity);

            // 2.2 清空所有槽位
            clearAllSlots(world, pos);

            // 2.3 将坛子状态设为0（空）
            setBlockState(world, pos, 0);

            // 2.4 从坛子顶部弹出1个粗布（模拟揭开封布）
            spawnItemAtTop(world, pos, new ItemStack(FlavorImmersedDailyModItems.COARSECLOTH.get()));

            // 播放打开音效
            playSound(world, x, y, z, "block.wool.break");

            // 重置玩家标记
            entity.getPersistentData().putString("JAR", "NO");
        }
    }

    /**
     * 获取坛子方块状态
     */
    @Unique
    private static int getBlockState(LevelAccessor world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Property<?> property = state.getBlock().getStateDefinition().getProperty("blockstate");
        if (property instanceof IntegerProperty intProp) {
            return state.getValue(intProp);
        }
        return -1;
    }

    /**
     * 设置坛子方块状态
     */
    @Unique
    private static void setBlockState(LevelAccessor world, BlockPos pos, int value) {
        BlockState state = world.getBlockState(pos);
        Property<?> property = state.getBlock().getStateDefinition().getProperty("blockstate");
        if (property instanceof IntegerProperty intProp) {
            if (intProp.getPossibleValues().contains(value)) {
                world.setBlock(pos, state.setValue(intProp, value), 3);
            }
        }
    }

    /**
     * 查找第一个空槽位（0-3）
     */
    @Unique
    private static int findEmptySlot(LevelAccessor world, BlockPos pos) {
        IItemHandler itemHandler = null;

        if (world instanceof ILevelExtension _ext) {
            itemHandler = _ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        }
        if (!(itemHandler instanceof IItemHandlerModifiable handler)) {
            return -1;
        }
        for (int i = 0; i < 4; i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 将手中所有物品放入指定槽位
     */
    @Unique
    private static void putItemIntoSlot(LevelAccessor world, BlockPos pos, int slot, ItemStack stack) {
        if (!(world instanceof ILevelExtension ext)) {
            return;
        }

        IItemHandler itemHandler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (!(itemHandler instanceof IItemHandlerModifiable handler)) {
            return;
        }

        // 检查目标槽位是否为空
        if (!handler.getStackInSlot(slot).isEmpty()) {
            return;
        }

        // 将手中所有物品放入槽位
        ItemStack toPut = stack.copy();
        handler.setStackInSlot(slot, toPut);
    }

    /**
     * 从坛子顶部弹出所有物品（槽位0-4）
     */
    @Unique
    private static void spawnAllItems(LevelAccessor world, BlockPos pos, Entity entity) {
        IItemHandler itemHandler = null;

        if (world instanceof ILevelExtension _ext) {
            itemHandler = _ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        }
        if (!(itemHandler instanceof IItemHandlerModifiable handler)) {
            return;
        }

        // 从槽位0-4取出所有物品，从坛子顶部弹出
        for (int i = 0; i < 5; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack toSpawn = stack.copy();
                spawnItemAtTop(world, pos, toSpawn);
            }
        }
    }

    /**
     * 在坛子顶部生成一个物品掉落物
     */
    @Unique
    private static void spawnItemAtTop(LevelAccessor world, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        if (!(world instanceof Level level)) {
            return;
        }

        // 计算坛子顶部的生成位置（稍微偏上一点，模拟从坛子口弹出）
        double spawnX = pos.getX() + 0.5;
        double spawnY = pos.getY() + 0.8; // 坛子顶部
        double spawnZ = pos.getZ() + 0.5;

        // 给物品一个随机的水平速度，模拟从坛子里弹出的效果
        double motionX = (level.random.nextDouble() - 0.5) * 0.3;
        double motionY = 0.2 + level.random.nextDouble() * 0.2; // 向上弹
        double motionZ = (level.random.nextDouble() - 0.5) * 0.3;

        ItemEntity itemEntity = new ItemEntity(level, spawnX, spawnY, spawnZ, stack);
        itemEntity.setDeltaMovement(motionX, motionY, motionZ);
        itemEntity.setDefaultPickUpDelay(); // 默认拾取延迟

        level.addFreshEntity(itemEntity);
    }

    /**
     * 清空所有槽位
     */
    @Unique
    private static void clearAllSlots(LevelAccessor world, BlockPos pos) {
        IItemHandler itemHandler = null;

        if (world instanceof ILevelExtension _ext) {
            itemHandler = _ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        }
        if (!(itemHandler instanceof IItemHandlerModifiable handler)) {
            return;
        }
        if (handler instanceof IItemHandlerModifiable modifiable) {
            for (int i = 0; i < 5; i++) {
                modifiable.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    /**
     * 播放音效
     */
    @Unique
    private static void playSound(LevelAccessor world, double x, double y, double z, String soundId) {
        if (!(world instanceof Level level)) {
            return;
        }

        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId));
        if (sound == null) {
            return;
        }

        if (!level.isClientSide()) {
            level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            level.playLocalSound(x, y, z, sound, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
    }
}