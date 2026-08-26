package net.fodoth.skina.goldentweaks.mixin.shut;

import dev.emi.emi.runtime.EmiLog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

/**
 * Suppress harmless JECharacters / PinIn async search spam.
 */
@Mixin(value = EmiLog.class, remap = false)
public abstract class EmiLogMixin {

    /**
     * @author GoldenTweaks
     * @reason Suppress harmless PinIn async search exceptions.
     */
    @Overwrite
    public static void error(String str) {

        // keep normal EMI errors
        EmiLog.warn(str);
    }

    /**
     * @author GoldenTweaks
     * @reason Suppress harmless PinIn async search exceptions.
     */
    @Overwrite
    public static void error(String str, Throwable t) {

        if (goldentweaks$shouldSuppress(str, t)) {
            return;
        }

        EmiLog.LOG.error(str, t);
    }

    @Unique
    private static boolean goldentweaks$shouldSuppress(
            String str,
            Throwable t
    ) {

        if (str == null || t == null) {
            return false;
        }

        // EMI async search spam
        if (!str.contains("Error when attempting to search")) {
            return false;
        }

        if (t instanceof NullPointerException) {
            return true;
        }

        // PinIn / JECharacters stack
        String throwable = t.toString();

        if (throwable.contains("me.towdium.pinin")) {
            return true;
        }

        for (StackTraceElement element : t.getStackTrace()) {

            String className = element.getClassName();

            if (className.startsWith("me.towdium.pinin")
                    || className.startsWith("me.towdium.jecharacters")) {
                return true;
            }
        }

        return false;
    }
}
