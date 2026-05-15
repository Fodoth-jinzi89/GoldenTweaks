package net.fodoth.skina.goldentweaks.mixin.shut;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import shcm.shsupercm.fabric.citresewn.CITResewn;

import java.lang.reflect.Field;

@Mixin(value = CITResewn.class, remap = false)
public class CITResewnLoggerMixin {

    @Unique
    private static Logger goldentweaks$getLogger() {

        try {

            Field field = CITResewn.class.getDeclaredField("LOG");

            field.setAccessible(true);

            Object obj = field.get(null);

            if (obj instanceof Logger logger) {
                return logger;
            }

        } catch (Throwable t) {

            GoldenTweaks.LOGGER.warn(
                    "Failed to reflect CITResewn logger",
                    t
            );
        }

        return (Logger) GoldenTweaks.LOGGER;
    }
}