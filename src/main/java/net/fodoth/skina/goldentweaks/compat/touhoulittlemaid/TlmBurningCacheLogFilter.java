package net.fodoth.skina.goldentweaks.compat.touhoulittlemaid;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.neoforged.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.filter.AbstractFilter;

/**
 * Drops Touhou Little Maid's burning-state cache ERROR from the log without touching any bytecode.
 *
 * <p>On a dedicated server TLM's handler ({@code handler$bnj000$touhou_little_maid$postIsBurningBlock},
 * injected into {@code NodeEvaluator#isBurningBlock}) reflects over the block's class to find an
 * {@code is(Block)} method. Spectrum's block class declares a method taking
 * {@code net.minecraft.client.resources.model.Material}, which does not exist on a dedicated server, so the
 * reflection throws {@code NoClassDefFoundError} and TLM logs an ERROR - once per session, with its
 * burning-state cache for that block simply not populated (it falls back to the vanilla check).</p>
 *
 * <p>Patching that generated handler with Mixin is not worth the risk: its name carries a mapping-dependent
 * hash, it is {@code static} and its signature is {@code (BlockState, CallbackInfoReturnable)} - two wrong
 * guesses there already crashed a live server. So the exact message is filtered at the logging layer
 * instead: a log4j2 {@link AbstractFilter} on TLM's own logger that denies only that one message prefix.
 * Nothing else from TLM is affected, and if the logging internals ever change the installer does nothing.
 * It is installed once from {@code CommonSetupEvent}, long before any player can join.</p>
 */
public final class TlmBurningCacheLogFilter {

    private static final String LOGGER_NAME = "touhou_little_maid";

    private static final String MESSAGE_PREFIX = "Error when checking is(Block) method for caching burning state";

    private static boolean installed;

    private TlmBurningCacheLogFilter() {
    }

    public static void installIfPresent() {
        if (installed || !ModList.get().isLoaded("touhou_little_maid")) {
            return;
        }

        installed = true;

        try {
            if (!(LogManager.getContext(false) instanceof LoggerContext context)) {
                return;
            }

            Logger logger = context.getLogger(LOGGER_NAME);

            logger.addFilter(new AbstractFilter() {

                @Override
                public Result filter(LogEvent event) {
                    return event.getMessage().getFormattedMessage().startsWith(MESSAGE_PREFIX)
                            ? Result.DENY
                            : Result.NEUTRAL;
                }
            });

            GoldenTweaks.LOGGER.debug("[TLM 兼容] 已压制火烧状态缓存的 ERROR 日志（log4j2 过滤器）");
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.debug("[TLM 兼容] 安装 log4j2 过滤器失败，保持原样", t);
        }
    }
}
