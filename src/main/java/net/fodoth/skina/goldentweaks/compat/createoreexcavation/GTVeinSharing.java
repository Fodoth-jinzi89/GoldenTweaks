package net.fodoth.skina.goldentweaks.compat.createoreexcavation;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Create Ore Excavation 的「矿脉分享」奖励计算。
 *
 * <p>在这个模组里一条矿脉就是一个区块：{@code OreVeinGenerator} 只会把矿脉放在
 * {@code RandomSpreadStructurePlacement} 算出的那一个区块里，而矿机也只会读自己所在区块的 {@code OreData}，
 * 所以同一区块里的矿机必然开采同一条矿脉。这里按区块统计矿机的所有者，返回「额外分享者」的数量。</p>
 */
public final class GTVeinSharing {

    private GTVeinSharing() {
    }

    /**
     * @return 该矿脉上除第一个玩家之外还有多少个不同玩家的矿机；未开启分享时恒为 0。
     */
    public static int getSharingSteps(Level level, BlockPos pos) {

        if (level == null || !GoldenTweaksCommonConfig.isOreVeinSharingEnabled()) {
            return 0;
        }

        LevelChunk chunk = level.getChunkAt(pos);
        Set<UUID> owners = new HashSet<>(4);
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof IGTOreVeinOwner owner) {
                UUID id = owner.gt$getVeinOwner();
                if (id != null) {
                    owners.add(id);
                }
            }
        }
        return Math.max(0, owners.size() - 1);
    }

    /**
     * 运行时间倍率：每多一个分享者减少一份时间，且不低于配置的下限。
     */
    public static double getTimeMultiplier(int steps) {

        if (steps <= 0) {
            return 1.0D;
        }

        double multiplier = 1.0D - GoldenTweaksCommonConfig.getOreVeinSpeedBonus() * steps;
        return Math.max(GoldenTweaksCommonConfig.getOreVeinSpeedFloor(), Math.min(1.0D, multiplier));
    }

    /**
     * 双倍产出概率：每多一个分享者增加一份概率，且不超过配置的上限。
     */
    public static double getDoubleOutputChance(int steps) {

        if (steps <= 0) {
            return 0.0D;
        }

        double chance = GoldenTweaksCommonConfig.getOreVeinYieldBonus() * steps;
        return Math.max(0.0D, Math.min(GoldenTweaksCommonConfig.getOreVeinDoubleOutputCap(), chance));
    }

    public static boolean rollDoubleOutput(Level level, int steps) {

        double chance = getDoubleOutputChance(steps);
        return chance > 0.0D && level.random.nextDouble() < chance;
    }
}
