package net.fodoth.skina.goldentweaks.compat.astralsorcery;

import java.util.Map;

/**
 * 星辉魔法星能力升级曲线的平滑实现。
 *
 * <p>原版每条「到达 k 级所需经验」是 {@code 150 + 100 * floor(1.2^k)}，指数爆炸：{@code floor}
 * 返回 int，约 88 级后每级固定 ~2.1e11，升到 100 级要 2.6e12 经验。这里换成单调递增的幂次曲线
 * {@code e_k = 0.99 * k^1.5}：全程只升不降、平滑无分段；1~364 级总经验约 1.0e6，且每一级都远低于原公式。</p>
 *
 * <p>1 级不足 1 点经验的部分按 1 兜底：若允许出现 0 经验，相邻等级的总经验相等，
 * 升级进度计算会出现 0/0。</p>
 */
public final class GTPerkLevelCurve {

    private static final double SCALE = 0.99D;

    private static final double EXPONENT = 1.5D;

    public static void fill(Map<Integer, Long> totalExpLevelRequired, int maxLevel) {

        totalExpLevelRequired.clear();

        long cumulative = 0L;

        for (int level = 1; level <= maxLevel; level++) {
            cumulative += increment(level);
            totalExpLevelRequired.put(level, cumulative);
        }
    }

    private static long increment(int level) {

        double raw = SCALE * Math.pow(level, EXPONENT);

        if (!(raw > 1.0D)) {
            return 1L;
        }

        return Math.round(raw);
    }

    private GTPerkLevelCurve() {
    }
}
