package net.fodoth.skina.goldentweaks.compat.touhoulostmaid;

/**
 * 祭坛召唤期间的标记。
 * <p>
 * 东方小女仆的祭坛（多方块鸟居）在 {@code AltarRecipe#spawnOutputEntity} 里现场造一只无主的
 * {@code EntityMaid}（spawn_box 走装箱、reborn_maid 走胶片复原），加入世界时会被迷失模组的
 * {@code ModEntityJoinLevelEvent} 判定为迷失女仆。这里在祭坛真正把女仆加进世界的那一小段时间
 * 置位，供迷失模组事件侧跳过判定，从而让祭坛召唤的女仆和未装迷失模组时一样可正常驯服。
 * <p>
 * 实体加入世界与祭坛配方结算都在服务端线程同步执行，因此一个布尔量即可。
 */
public final class AltarSummonGuard {

    private static boolean summoning;

    public static void begin() {
        summoning = true;
    }

    public static void end() {
        summoning = false;
    }

    public static boolean isSummoning() {
        return summoning;
    }

    private AltarSummonGuard() {
    }
}
