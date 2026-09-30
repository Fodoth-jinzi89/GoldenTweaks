package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.golems.GolemCore;
import thaumcraft.common.entities.golem.ThaumcraftGolemEntity;

/**
 * 神秘傀儡简化模式。
 *
 * <p>开启后傀儡不再寻路，固定停在原点（放下傀儡的位置，也就是容器/器械所在的那一格），
 * 直接从原点与工作范围内的方块和实体交互：收获核心照常收获范围内所有成熟作物，
 * 聚集核心直接拾取范围内掉落物并放进原点容器，其余核心（装填/清空/分类/使用/液体/源质）
 * 也改为原地隔空完成。检索间隔固定为 1 秒（20 tick），避免每 tick 做范围扫描。</p>
 *
 * <p>顺带解决移植版的两个问题：傀儡因为走不到目标而卡住不动（现在不需要移动），
 * 以及聚集傀儡拿着物品却一直往原点走、走不到就放不进容器（现在直接放进容器）。</p>
 *
 * <p>守卫（GUARD）与屠夫（BUTCHER）核心依赖移动追击，不参与简化模式，保持移植版行为。</p>
 */
public final class GTGolemSimplify {

    /** 简化模式下的核心工作/检索间隔：20 tick = 1 秒。 */
    public static final int INTERVAL_TICKS = 20;

    /** 与原点的距离超过该值（2 格）时把傀儡拉回原点。 */
    private static final double HOME_SNAP_DISTANCE_SQR = 4.0D;

    private GTGolemSimplify() {
    }

    public static boolean isEnabled() {
        return GoldenTweaksCommonConfig.isSimplifiedGolems();
    }

    /**
     * 是否对该傀儡启用简化模式。未绑定原点（{@code hasHome} 为假）时原点不可用，
     * 此时退回移植版行为更安全；守卫与屠夫核心同样不启用。
     */
    public static boolean appliesTo(ThaumcraftGolemEntity golem) {
        if (!isEnabled() || !golem.hasHome()) {
            return false;
        }

        int core = golem.getCore();
        return core >= 0 && core != GolemCore.GUARD.id && core != GolemCore.BUTCHER.id;
    }

    /**
     * 傀儡被推离原点（活塞、爆炸、被别的模组搬运）时返回原点坐标，否则返回 {@code null}。
     */
    @Nullable
    public static BlockPos homeSnapTarget(ThaumcraftGolemEntity golem) {
        if (!appliesTo(golem)) {
            return null;
        }

        BlockPos home = golem.home();
        return golem.distanceToSqr(Vec3.atCenterOf(home)) > HOME_SNAP_DISTANCE_SQR ? home : null;
    }
}
