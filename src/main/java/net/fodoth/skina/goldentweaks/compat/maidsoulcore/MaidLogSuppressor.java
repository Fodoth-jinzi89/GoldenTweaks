package net.fodoth.skina.goldentweaks.compat.maidsoulcore;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.neoforged.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.filter.AbstractFilter;

/**
 * 按客户端配置「心契誓约日志抑制」压制女仆核心与 ApricityUI 的全部日志输出。
 *
 * <p>在客户端安装（单机/客户端启动的集成服务器与服务端共用同一 log4j 上下文），
 * 通过 log4j2 {@link AbstractFilter} 在 logger 层直接 DENY；关闭该选项后过滤器返回 NEUTRAL，
 * 日志立即恢复。GoldenTweaks 自己发送的聊天提示不经过这两个 logger，因此不会被压制。</p>
 */
public final class MaidLogSuppressor {

    private static final String[] LOGGER_NAMES = {
            "com.maidsoul.brain.forge.MaidSoulCoreForgeMod",
            "com.sighs.apricityui.ApricityUI"
    };

    private static boolean installed;

    private MaidLogSuppressor() {
    }

    public static void installIfPresent() {
        if (installed || !ModList.get().isLoaded("maidsoulcore")) {
            return;
        }

        installed = true;

        try {
            if (!(LogManager.getContext(false) instanceof LoggerContext context)) {
                return;
            }

            AbstractFilter filter = new AbstractFilter() {
                @Override
                public Result filter(LogEvent event) {
                    return GoldenTweaksClientConfig.doSuppressMaidLogs() ? Result.DENY : Result.NEUTRAL;
                }
            };

            for (String name : LOGGER_NAMES) {
                context.getLogger(name).addFilter(filter);
            }

            GoldenTweaks.LOGGER.debug("[女仆核心] 已安装日志抑制过滤器（log4j2）");
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.debug("[女仆核心] 安装日志过滤器失败，保持原样", t);
        }
    }
}
