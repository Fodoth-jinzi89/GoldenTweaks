package net.fodoth.skina.goldentweaks.compat.academy;

import java.util.ArrayDeque;
import java.util.Collection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * 把超电磁炮的方块破坏分摊到多 tick，减轻单 tick 负担。
 *
 * <p>高倍率硬币的破坏区是很大很长的实心圆柱（射线数量随半径平方增长），一次性扫完会把服务端主线程
 * 卡住。这里把每条射线排进队列，在 {@link ServerTickEvent.Post} 里按时间预算分帧执行：每个 tick 只
 * 消耗 {@link #BUDGET_NANOS}，其余留给后续 tick，主线程不再长时间停顿。</p>
 *
 * <p>所有访问都发生在服务端主线程（射击与 tick 事件），因此不需要额外同步。</p>
 */
public final class GTRailgunDestruction {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** 每 tick 用于破坏的时间预算（纳秒），约 10ms。 */
    private static final long BUDGET_NANOS = 10_000_000L;

    private static final ArrayDeque<Ray> QUEUE = new ArrayDeque<>();

    /** 排入一次射击的全部射线（服务端主线程调用）。 */
    public static void enqueue(GTRailgunDestroyer owner, ServerLevel level, ServerPlayer player,
                               Collection<Vec3> origins, Vec3 direction, double maxDistance, double energy) {
        for (Vec3 origin : origins) {
            QUEUE.addLast(new Ray(owner, level, player, origin, direction, maxDistance, energy));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (QUEUE.isEmpty()) {
            return;
        }

        long deadline = System.nanoTime() + BUDGET_NANOS;
        Ray ray;
        while ((ray = QUEUE.pollFirst()) != null) {
            try {
                // 玩家中途掉线就放弃这条射线，避免用已移除的实体做破坏/权限判定
                if (!ray.player.isRemoved()) {
                    ray.owner.gt$destroyRay(ray.level, ray.player, ray.origin, ray.direction, ray.maxDistance, ray.energy);
                }
            } catch (Throwable t) {
                LOGGER.error("[GT] railgun destruction ray failed", t);
            }
            // 每帧至少执行一条射线（单条射线的耗时本身有界），再用预算卡住后续射线
            if (System.nanoTime() >= deadline) {
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        QUEUE.clear();
    }

    private record Ray(GTRailgunDestroyer owner, ServerLevel level, ServerPlayer player,
                       Vec3 origin, Vec3 direction, double maxDistance, double energy) {
    }

    private GTRailgunDestruction() {
    }
}
