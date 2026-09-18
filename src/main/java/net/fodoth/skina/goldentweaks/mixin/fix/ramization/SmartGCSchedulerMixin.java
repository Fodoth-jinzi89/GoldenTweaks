package net.fodoth.skina.goldentweaks.mixin.fix.ramization;

import com.konrados.ramization.memory.SmartGCScheduler;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Drops RAMization's forced {@code System.gc()} call.
 *
 * <p>{@code SmartGCScheduler#triggerGC} runs from {@code ServerTickEvent.Post} (see
 * {@code RAMization#onServerTickPost}) and calls {@code System.gc()} right there. On a big modpack heap
 * that is a stop-the-world full collection on the server thread: the log from the 圆理服务端 run contains a
 * {@code Watchdog Thread/ERROR: 服务器已 10 秒无响应} whose server-thread stack is exactly
 * {@code System.gc()} ← {@code SmartGCScheduler.triggerGC} ← {@code onTickEnd} ← {@code RAMization.onServerTickPost}.</p>
 *
 * <p>The JVM already collects young and old generations on its own (the pack runs G1), so this removes the
 * explicit full GC only: RAMization keeps sampling tick times, its heap statistics, its scheduling decisions
 * and its INFO log line - nothing else about it changes.</p>
 */
@Mixin(value = SmartGCScheduler.class, remap = false)
public class SmartGCSchedulerMixin {

    /** One line per game session, so a log can tell this patch is the one running. */
    private static final AtomicBoolean LOGGED = new AtomicBoolean();

    @Redirect(
            method = "triggerGC",
            at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V"),
            remap = false
    )
    private void gt$skipForcedFullGc() {
        if (LOGGED.compareAndSet(false, true)) {
            GoldenTweaks.LOGGER.info(
                    "[GT-RAMization 兼容] 已跳过 tick 内的 System.gc()（避免全量 GC 卡死服务端，本条只打印一次）"
            );
        }
    }
}
