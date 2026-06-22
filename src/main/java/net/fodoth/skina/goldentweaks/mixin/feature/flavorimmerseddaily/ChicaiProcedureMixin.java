package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.fodoth.skina.goldentweaks.util.FidFoodMappingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.mcreator.flavorimmerseddaily.procedures.吃菜Procedure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

/**
 * 重写吃菜Procedure，支持女仆实体
 */
@Mixin(吃菜Procedure.class)
public class ChicaiProcedureMixin {
    /**
     * @author Goldentweaks
     * @reason 添加对女仆实体的支持，并优化代码结构
     */
    @Overwrite
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null) {
            return;
        }

        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // 1. 播放吃东西音效
        playEatSound(world, x, y, z);

        // 2. 恢复食物值（支持玩家和女仆）
        restoreFood(world, entity);

        // 3. 生成食物粒子效果（支持玩家和女仆）
        spawnFoodParticles(world, pos, entity, block);

        // 4. 更新方块状态（分阶段减少食物）
        int currentBlockState = getBlockStateValue(world, pos);
        updateBlockState(world, pos, currentBlockState);
    }

    /**
     * 播放吃东西音效
     */
    @Unique
    private static void playEatSound(LevelAccessor world, double x, double y, double z) {
        if (!(world instanceof Level level)) {
            return;
        }

        SoundEvent eatSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.eat"));
        if (eatSound != null) {
            if (!level.isClientSide()) {
                level.playSound(null, BlockPos.containing(x, y, z), eatSound, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                level.playLocalSound(x, y, z, eatSound, SoundSource.PLAYERS, 1.0F, 1.0F, false);
            }
        }
    }

    /**
     * 恢复食物值（支持玩家和女仆）
     */
    @Unique
    private static void restoreFood(LevelAccessor world, Entity entity) {
        // 处理玩家
        if (entity instanceof Player player) {
            FoodData foodData = player.getFoodData();
            int currentFood = foodData.getFoodLevel();
            foodData.setFoodLevel(currentFood + 4);
            return;
        }

        // 处理女仆 - 使用女仆的 eat 方法
        if (entity instanceof EntityMaid maid) {
            // 创建模拟的食物物品
            ItemStack dummyFood = new ItemStack(Items.CAKE);

            // 创建食物属性：4点饱食度，1点饱和值
            FoodProperties foodProperties = new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationModifier(1.0F)
                    .build();

            // 调用女仆的 eat 方法
            if (world instanceof Level level) {
                maid.eat(level, dummyFood, foodProperties);
            }
        }
    }

    /**
     * 生成食物粒子效果
     * 支持玩家和女仆，根据方块获取对应的食物物品
     */
    @Unique
    private static void spawnFoodParticles(LevelAccessor world, BlockPos pos, Entity entity, Block block) {
        if (!(world instanceof Level level)) {
            return;
        }

        // 获取方块对应的食物物品
        ItemStack foodStack = getFoodItemStack(block);
        if (foodStack.isEmpty()) {
            // 如果没有对应的食物物品，使用默认的蛋糕
            foodStack = new ItemStack(Items.CAKE);
        }

        // 为玩家生成粒子
        if (entity instanceof Player) {
            // 玩家的食物粒子效果由原版处理，这里不做额外处理
            // 但我们可以生成一些简单的粒子效果
            spawnParticles(level, pos, foodStack, 8);
        }

        // 为女仆生成食物粒子
        if (entity instanceof EntityMaid maid) {
            maid.spawnItemParticles(foodStack, 8);
        }
    }

    /**
     * 根据方块获取对应的食物物品堆
     * 优先使用 FidFoodMappingUtil，如果不是 FID 食物则使用方块自身
     */
    @Unique
    private static ItemStack getFoodItemStack(Block block) {
        // 1. 尝试从 FID 映射中获取
        ItemStack fidFood = FidFoodMappingUtil.getItemStackByBlock(block)
                .orElse(ItemStack.EMPTY);
        if (!fidFood.isEmpty()) {
            return fidFood;
        }

        // 2. 如果不是 FID 食物，使用方块自身的物品
        try {
            // 使用原版的方式获取方块对应的物品
            net.minecraft.world.item.Item item = block.asItem();
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
        } catch (Exception e) {
            // 忽略异常
        }

        // 3. 默认返回蛋糕
        return new ItemStack(Items.CAKE);
    }

    /**
     * 生成简单的粒子效果（用于玩家）
     */
    @Unique
    private static void spawnParticles(Level level, BlockPos pos, ItemStack stack, int count) {
        if (level.isClientSide()) {
            return;
        }

        // 使用原版的粒子生成方式
        // 这里使用食用的粒子效果
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.8D;
        double z = pos.getZ() + 0.5D;

        ItemParticleOption particle =
                new ItemParticleOption(
                        ParticleTypes.ITEM,
                        stack
                );

        // 生成一些简单的粒子
        for (int i = 0; i < count; i++) {
            double xOff = (level.random.nextDouble() - 0.5D) * 0.6D;
            double yOff = (level.random.nextDouble() - 0.5D) * 0.6D + 0.2D;
            double zOff = (level.random.nextDouble() - 0.5D) * 0.6D;

            level.addParticle(
                    particle,
                    x + xOff,
                    y + yOff,
                    z + zOff,
                    xOff * 0.1D,
                    yOff * 0.1D + 0.1D,
                    zOff * 0.1D
            );
        }
    }

    /**
     * 获取方块当前的 blockstate 属性值
     */
    @Unique
    private static int getBlockStateValue(LevelAccessor world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Property<?> property = state.getBlock().getStateDefinition().getProperty("blockstate");

        if (property instanceof IntegerProperty intProp) {
            return state.getValue(intProp);
        }
        return -1;
    }

    /**
     * 更新方块状态
     */
    @Unique
    private static void updateBlockState(LevelAccessor world, BlockPos pos, int currentState) {

        // 先检查方块是否还存在
        if (world.getBlockState(pos).isAir()) {
            return;
        }

        switch (currentState) {
            case 0:
                // 只有当前状态确实是0时才更新
                if (getBlockStateValue(world, pos) == 0) {
                    setBlockState(world, pos, 1);
                }
                break;
            case 1:
                if (getBlockStateValue(world, pos) == 1) {
                    setBlockState(world, pos, 2);
                }
                break;
            case 2:
                if (getBlockStateValue(world, pos) == 2) {
                    world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
                break;
            default:
                break;
        }
    }

            /**
             * 设置方块的 blockstate 属性值
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
}