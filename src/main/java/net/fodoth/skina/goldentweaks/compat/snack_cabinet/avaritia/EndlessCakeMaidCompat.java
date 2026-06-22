package net.fodoth.skina.goldentweaks.compat.snack_cabinet.avaritia;

import com.github.tartaricacid.touhoulittlemaid.api.block.IMaidEdibleBlock;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import committee.nova.mods.avaritia.common.block.cake.EndlessCakeBlock;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 无尽蛋糕的女仆兼容实现
 */
public class EndlessCakeMaidCompat implements IMaidEdibleBlock {

    @Override
    public boolean shouldMoveTo(EntityMaid maid, BlockPos pos, BlockState state) {
        // 判断是否为无尽蛋糕，且下方有零食架
        return state.getBlock() instanceof EndlessCakeBlock && IMaidEdibleBlock.belowIsSnackStand(maid, pos);
    }

    @Override
    public int getFavorabilityPoints(EntityMaid maid, BlockPos pos, BlockState state) {
        // 无尽蛋糕比较珍贵，给 4 点好感度
        return 4;
    }

    @Override
    public boolean consume(EntityMaid maid, BlockPos pos, BlockState state) {
        // 检查是否为无尽蛋糕
        if (!(state.getBlock() instanceof EndlessCakeBlock)) {
            return false;
        }

        Level level = maid.level();

        ItemStack cakeStack = new ItemStack(ModBlocks.endless_cake.asItem());
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(20)           // 恢复 20 点饱食度
                .saturationModifier(1.0F)   // 恢复 20 点饱和值
                .alwaysEdible()              // 任何时候都能吃
                .build();

        // 调用女仆的 eat 方法
        maid.eat(level, cakeStack, foodProperties);
        // 1. 应用无尽蛋糕的药水效果
        applyEndlessCakeEffects(maid);

        // 2. 生成粒子效果（与原版无尽蛋糕一致）
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    pos.getX() + 0.5D,
                    pos.getY() + 1.0D,
                    pos.getZ() + 0.5D,
                    10,
                    0.5D,
                    0.5D,
                    0.5D,
                    0.1D
            );
        }

        // 3. 播放吃东西音效
        maid.playSound(SoundEvents.GENERIC_EAT);

        // 4. 生成食物粒子（展示女仆吃的是什么）
        maid.spawnItemParticles(new ItemStack(state.getBlock().asItem()), 8);

        // 注意：无尽蛋糕不会减少，所以不需要修改方块状态或移除方块

        return true;
    }

    /**
     * 应用无尽蛋糕的增益效果到女仆
     */
    private void applyEndlessCakeEffects(EntityMaid maid) {
        // 移除中毒效果
        maid.removeEffect(MobEffects.POISON);

        // 添加所有增益效果
        // 生命恢复 II - 20秒 (400 ticks)
        maid.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));

        // 抗性提升 - 5分钟 (6000 ticks)
        maid.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0));

        // 火焰抗性 - 5分钟 (6000 ticks)
        maid.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));

        // 伤害吸收 IV - 2分钟 (2400 ticks)
        maid.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3));
    }

    @Override
    public boolean canPlaceAsFood(EntityMaid maid, ItemStack stack, int slotIndex) {
        // 判断物品是否为无尽蛋糕
        return stack.getItem() == ModBlocks.endless_cake.asItem();
    }


    @Override
    public boolean placeAsFood(EntityMaid maid, BlockPos pos, ItemStack stack, int slotIndex) {
        // 检查是否为无尽蛋糕
        if (!canPlaceAsFood(maid, stack, slotIndex)) {
            return false;
        }

        // 从背包中扣除一个物品
        var availableInv = maid.getAvailableInv(true);
        ItemStack extracted = availableInv.extractItem(slotIndex, 1, false);

        if (extracted.isEmpty()) {
            return false;
        }

        // 放置方块
        return maid.placeItemBlock(pos, extracted);
    }
}
