package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.mcreator.flavorimmerseddaily.FlavorImmersedDailyMod;
import net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModItems;
import net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModParticleTypes;
import net.mcreator.flavorimmerseddaily.network.FlavorImmersedDailyModVariables;
import net.mcreator.flavorimmerseddaily.procedures.MeatmincerRightProcedure;
import net.mcreator.flavorimmerseddaily.procedures.绞肉机更新Procedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
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
import net.minecraft.world.level.block.entity.BlockEntity;
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

@Mixin(MeatmincerRightProcedure.class)
public class MeatmincerRightProcedureMixin {

    /**
     * @reason 重写绞肉机右键交互
     * 状态0 = 有盖子，状态1 = 无盖子
     * 必须盖上盖子才能工作，产物存放在槽位1
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
        boolean hasCover = mainHand.getItem() == FlavorImmersedDailyModItems.MINCER_COVER.get();

        // === 状态0：有盖子（已密封） ===
        if (currentState == 0) {

            // 1.1 直接取下盖子（不需要手持盖子，不需要潜行）
            // 给玩家一个盖子
            if (entity instanceof Player player) {
                ItemStack cover = new ItemStack(FlavorImmersedDailyModItems.MINCER_COVER.get());
                cover.setCount(1);
                ItemHandlerHelper.giveItemToPlayer(player, cover);
            }

            // 将状态设为1（无盖子）
            setBlockState(world, pos, 1);

            // 播放音效
            playSound(world, x, y, z, "block.wood.break");
            return;
        }

        // === 状态1：无盖子 ===
        if (currentState == 1) {

            // 2.1 优先取出槽位1的物品（产物）
            if (getSlotAmount(world, pos, 1) > 0) {
                giveItemToPlayer(world, pos, entity, 1);
                clearSlot(world, pos, 1);
                playSound(world, x, y, z, "entity.item.pickup");
                return;
            }

            // 2.2 槽位0有物品（原料）且手持盖子 → 盖上（继续加工）
            if (getSlotAmount(world, pos, 0) > 0 && hasCover) {
                // 消耗盖子
                mainHand.shrink(1);

                // 设置状态为0（有盖子）
                setBlockState(world, pos, 0);

                // 播放放置音效
                playSound(world, x, y, z, "block.wood.place");

                // 盖上后检查是否有物品需要加工
                if (getSlotAmount(world, pos, 0) > 0 && getSlotAmount(world, pos, 1) == 0) {
                    startMincingProcess(world, x, y, z);
                }
                return;
            }

            // 2.3 槽位0有物品（原料）且不手持盖子 → 取出原料
            if (getSlotAmount(world, pos, 0) > 0) {
                giveItemToPlayer(world, pos, entity, 0);
                clearSlot(world, pos, 0);
                playSound(world, x, y, z, "entity.item.pickup");
                return;
            }

            // 2.4 槽位都空且手持盖子 → 盖上
            if (hasCover) {
                // 消耗盖子
                mainHand.shrink(1);

                // 设置状态为0（有盖子）
                setBlockState(world, pos, 0);

                // 播放放置音效
                playSound(world, x, y, z, "block.wood.place");
                return;
            }

            // 2.5 放入物品到槽位0（槽位都空且不手持盖子）
            if (!mainHand.isEmpty()) {
                // 注册绞肉机位置到全局变量
                registerMincerPosition(world, x, y, z, entity);

                putItemIntoSlot(world, pos, 0, mainHand);
                mainHand.setCount(0);

                // 播放放入音效
                playSound(world, x, y, z, "block.composter.fill");
                return;
            }
        }
    }

    /**
     * 获取绞肉机方块状态
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
     * 设置绞肉机方块状态
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
     * 注册绞肉机位置
     */
    @Unique
    private static void registerMincerPosition(LevelAccessor world, double x, double y, double z, Entity entity) {
        String str = FlavorImmersedDailyModVariables.MapVariables.get(world).meatmincerstr;
        String pos = "#" + x + "," + y + "," + z;
        if (!str.contains(pos)) {
            FlavorImmersedDailyModVariables.MapVariables.get(world).meatmincerstr = str + pos;
            FlavorImmersedDailyModVariables.MapVariables.get(world).syncData(world);
        }
    }

    /**
     * 启动绞肉机工作流程（必须有盖子 - 状态0）
     */
    @Unique
    private static void startMincingProcess(LevelAccessor world, double x, double y, double z) {
        FlavorImmersedDailyMod.queueServerWork(1, () -> {
            int currentState = getBlockState(world, BlockPos.containing(x, y, z));
            // 只有状态0（有盖子）才能工作
            if (currentState == 0) {
                // 检查是否已经在工作
                BlockEntity blockEntity = world.getBlockEntity(BlockPos.containing(x, y, z));
                String workStatus = blockEntity != null ? blockEntity.getPersistentData().getString("awork") : "";

                if (!"aa".equals(workStatus) && !world.isClientSide()) {
                    // 标记开始工作
                    BlockPos bp = BlockPos.containing(x, y, z);
                    BlockEntity be = world.getBlockEntity(bp);
                    BlockState bs = world.getBlockState(bp);
                    if (be != null) {
                        be.getPersistentData().putString("awork", "aa");
                    }
                    if (world instanceof Level level) {
                        level.sendBlockUpdated(bp, bs, bs, 3);
                    }
                }

                // 播放工作音效
                if ("aa".equals(workStatus) && world instanceof Level level) {
                    SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("flavor_immersed_daily:pressure_work"));
                    if (!level.isClientSide()) {
                        if (sound != null) {
                            level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.BLOCKS, 0.2F, 0.3F);
                        }
                    } else {
                        if (sound != null) {
                            level.playLocalSound(x, y, z, sound, SoundSource.BLOCKS, 0.2F, 0.3F, false);
                        }
                    }
                }

                // 添加粒子效果
                world.addParticle((SimpleParticleType) FlavorImmersedDailyModParticleTypes.TREELEAVE.get(),
                        x, y, z, 0.0F, 1.0F, 0.0F);

                // 90 tick 后完成绞肉
                FlavorImmersedDailyMod.queueServerWork(90, () -> {
                    // 多次执行更新 - 这会生成产物到槽位1
                    for (int i = 0; i < 64; i++) {
                        绞肉机更新Procedure.execute(world, x, y, z);
                    }

                    // 产物已经在槽位1了

                    // 标记工作完成
                    if (!world.isClientSide()) {
                        BlockPos bp = BlockPos.containing(x, y, z);
                        BlockEntity be = world.getBlockEntity(bp);
                        BlockState bs = world.getBlockState(bp);
                        if (be != null) {
                            be.getPersistentData().putString("awork", "bb");
                        }
                        if (world instanceof Level level) {
                            level.sendBlockUpdated(bp, bs, bs, 3);
                        }
                    }

                    // 播放完成音效
                    playSound(world, x, y, z, "block.note_block.chime");
                });
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