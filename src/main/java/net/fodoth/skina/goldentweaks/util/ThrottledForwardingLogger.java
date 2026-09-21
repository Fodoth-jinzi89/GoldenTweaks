package net.fodoth.skina.goldentweaks.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 把被接管模组的 log4j Logger 转发到 GoldenTweaks 的 logger，同时做去重与配额限制以免刷屏。
 *
 * <p>DEBUG/TRACE 直接丢弃；WARN/ERROR/FATAL 始终转发；INFO 与 WARN 各限一份预算并去除完全重复的消息。</p>
 */
public class ThrottledForwardingLogger extends AbstractLogger {

    private static final int INFO_BUDGET = 24;
    private static final int WARN_BUDGET = 96;

    private final Logger target;
    private final String prefix;
    private final Set<String> seen = ConcurrentHashMap.newKeySet();
    private final AtomicInteger infoUsed = new AtomicInteger();
    private final AtomicInteger warnUsed = new AtomicInteger();

    public ThrottledForwardingLogger(Logger target, String prefix) {
        this.target = target;
        this.prefix = prefix;
    }

    @Override
    public Level getLevel() {
        return Level.ALL;
    }

    @Override
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
        if (message == null) {
            return;
        }
        String text = message.getFormattedMessage();
        if (text == null) {
            return;
        }
        String line = prefix + text;

        if (level == Level.ERROR || level == Level.FATAL) {
            log(target, level, line, t);
            return;
        }
        if (level == Level.WARN) {
            if (seen.add(text) && warnUsed.incrementAndGet() <= WARN_BUDGET) {
                log(target, level, line, t);
            }
            return;
        }
        if (seen.add(text) && infoUsed.incrementAndGet() <= INFO_BUDGET) {
            log(target, level, line, t);
        }
    }

    private static void log(Logger logger, Level level, String line, Throwable t) {
        if (level == Level.ERROR || level == Level.FATAL) {
            if (t != null) {
                logger.error(line, t);
            } else {
                logger.error(line);
            }
        } else if (level == Level.WARN) {
            if (t != null) {
                logger.warn(line, t);
            } else {
                logger.warn(line);
            }
        } else {
            if (t != null) {
                logger.info(line, t);
            } else {
                logger.info(line);
            }
        }
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable t) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence message, Throwable t) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object message, Throwable t) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Throwable t) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object... params) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return forwardable(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return forwardable(level);
    }

    private static boolean forwardable(Level level) {
        return level != null && level != Level.DEBUG && level != Level.TRACE && level != Level.OFF;
    }
}
