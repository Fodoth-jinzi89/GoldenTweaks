package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.mcreator.flavorimmerseddaily.FlavorImmersedDailyMod;
import net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModItems;
import net.mcreator.flavorimmerseddaily.procedures.TeapotrecipeProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.mcreator.flavorimmerseddaily.procedures.TeapotrightProcedure")
public class TeapotrightProcedureMixin {

    /**
     * @reason 重写茶壶右键交互
     * 状态0 = 无盖子，状态1 = 有盖子
     * 槽位0 = 输出（产物），槽位1-3 = 输入（原料）
     * 空手可以取出产物和原料
     * @author Fodoth_jinzi89
     */
    @Overwrite
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null) {
            return;
        }

        BlockPos pos = BlockPos.containing(x, y, z);
        int currentState = getBlockState(world, pos);

        // 获取主手物品
        ItemStack mainHand = entity instanceof LivingEntity ? ((LivingEntity) entity).getMainHandItem() : ItemStack.EMPTY;
        boolean hasCover = mainHand.getItem() == FlavorImmersedDailyModItems.TEAPOTCOVER.get();
        boolean hasPincers = mainHand.getItem() == FlavorImmersedDailyModItems.PINCERS.get();
        boolean isEmptyHand = mainHand.isEmpty();

        // === 状态1：有盖子（已密封） ===
        if (currentState == 1) {

            // 1.1 直接取下盖子（不需要手持盖子，不需要潜行）
            // 给玩家一个盖子
            if (entity instanceof Player player) {
                ItemStack cover = new ItemStack(FlavorImmersedDailyModItems.TEAPOTCOVER.get());
                cover.setCount(1);
                ItemHandlerHelper.giveItemToPlayer(player, cover);
            }

            // 将状态设为0（无盖子）
            setBlockState(world, pos, 0);

            // 播放音效
            playSound(world, x, y, z, "block.wood.break");
            return;
        }

        // === 状态0：无盖子 ===
        if (currentState == 0) {

            // 2.1 空手才能取出物品（产物优先，然后是原料）
            if (isEmptyHand) {
                // 优先取出槽位0的产物
                if (getSlotAmount(world, pos, 0) > 0) {
                    giveItemToPlayer(world, pos, entity, 0);
                    clearSlot(world, pos, 0);
                    playSound(world, x, y, z, "entity.item.pickup");
                    return;
                }

                // 然后依次取出槽位1-3的原料
                for (int i = 1; i <= 3; i++) {
                    if (getSlotAmount(world, pos, i) > 0) {
                        giveItemToPlayer(world, pos, entity, i);
                        clearSlot(world, pos, i);
                        playSound(world, x, y, z, "entity.item.pickup");
                        return;
                    }
                }

                // 如果所有槽位都空，什么都不做
                return;
            }

            // 2.2 手持盖子且至少有一个输入槽位有原料 → 盖上（开始煮茶）
            if (hasCover && hasAnyInput(world, pos)) {
                // 消耗盖子
                mainHand.shrink(1);

                // 设置状态为1（有盖子）
                setBlockState(world, pos, 1);

                // 播放放置音效
                playSound(world, x, y, z, "block.lantern.place");

                // 启动煮茶过程
                startBrewingProcess(world, x, y, z);
                return;
            }

            // 2.3 槽位都空且手持盖子 → 盖上（不工作）
            if (!hasAnyInput(world, pos) && getSlotAmount(world, pos, 0) == 0 && hasCover) {
                // 消耗盖子
                mainHand.shrink(1);

                // 设置状态为1（有盖子）
                setBlockState(world, pos, 1);

                // 播放放置音效
                playSound(world, x, y, z, "block.lantern.place");
                return;
            }

            // 2.4 查找空余的输入槽位（1-3）
            int emptySlot = findEmptyInputSlot(world, pos);

            // 2.5 放入物品到空余的输入槽位（不手持盖子，不手持钳子，手里有东西）
            if (emptySlot != -1 && !hasCover && !hasPincers) {
                putItemIntoSlot(world, pos, emptySlot, mainHand);
                mainHand.setCount(0);

                // 播放放入音效
                playSound(world, x, y, z, "item.bucket.fill");
                return;
            }

            // 2.6 如果手持其他物品但没有空余槽位 → 播放低音提示
            if (!hasCover && !hasPincers) {
                playSound(world, x, y, z, "block.note_block.bass");
            }
        }
    }

    /**
     * 获取茶壶方块状态
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
     * 设置茶壶方块状态
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
     * 获取指定槽位的物品数量
     */
    @Unique
    private static int getSlotAmount(LevelAccessor world, BlockPos pos, int slot) {
        if (!(world instanceof ILevelExtension ext)) {
            return 0;
        }
        IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (handler != null) {
            return handler.getStackInSlot(slot).getCount();
        }
        return 0;
    }

    /**
     * 获取指定槽位的物品
     */
    @Unique
    private static ItemStack getSlotItem(LevelAccessor world, BlockPos pos, int slot) {
        if (!(world instanceof ILevelExtension ext)) {
            return ItemStack.EMPTY;
        }
        IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (handler != null) {
            return handler.getStackInSlot(slot).copy();
        }
        return ItemStack.EMPTY;
    }

    /**
     * 将物品放入指定槽位
     */
    @Unique
    private static void putItemIntoSlot(LevelAccessor world, BlockPos pos, int slot, ItemStack stack) {
        if (!(world instanceof ILevelExtension ext)) {
            return;
        }
        IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (handler instanceof IItemHandlerModifiable modifiable) {
            ItemStack toPut = stack.copy();
            modifiable.setStackInSlot(slot, toPut);
        }
    }

    /**
     * 清空指定槽位
     */
    @Unique
    private static void clearSlot(LevelAccessor world, BlockPos pos, int slot) {
        if (!(world instanceof ILevelExtension ext)) {
            return;
        }
        IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (handler instanceof IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    /**
     * 给玩家物品（从槽位取出）
     */
    @Unique
    private static void giveItemToPlayer(LevelAccessor world, BlockPos pos, Entity entity, int slot) {
        if (!(entity instanceof Player player)) {
            return;
        }
        ItemStack stack = getSlotItem(world, pos, slot);
        if (!stack.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, stack);
        }
    }

    /**
     * 查找第一个空余的输入槽位（1-3）
     */
    @Unique
    private static int findEmptyInputSlot(LevelAccessor world, BlockPos pos) {
        if (!(world instanceof ILevelExtension ext)) {
            return -1;
        }
        IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (handler != null) {
            for (int i = 1; i <= 3; i++) {
                if (handler.getStackInSlot(i).isEmpty()) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 检查是否有任何输入槽位（1-3）有物品
     */
    @Unique
    private static boolean hasAnyInput(LevelAccessor world, BlockPos pos) {
        if (!(world instanceof ILevelExtension ext)) {
            return false;
        }
        IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (handler != null) {
            for (int i = 1; i <= 3; i++) {
                if (!handler.getStackInSlot(i).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 启动煮茶工作流程（必须有盖子 - 状态1）
     */
    @Unique
    private static void startBrewingProcess(LevelAccessor world, double x, double y, double z) {
        // 播放煮茶音效
        playSound(world, x, y, z, "flavor_immersed_daily:boil");

        // 80 tick 后完成煮茶
        FlavorImmersedDailyMod.queueServerWork(80, () -> {
            int currentState = getBlockState(world, BlockPos.containing(x, y, z));
            // 只有状态1（有盖子）才能完成煮茶
            if (currentState == 1) {
                // 多次执行配方更新 - 这会消耗槽位1-3的原料，生成产物到槽位0
                for (int i = 0; i < 64; i++) {
                    TeapotrecipeProcedure.execute(world, x, y, z);
                }

                // 播放完成音效
                playSound(world, x, y, z, "block.note_block.chime");
            }
        });
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