package net.fodoth.skina.goldentweaks.compat.geckolib;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

/** Weak sharing only for GeckoLib's immutable Constant record. */
public final class GTGeckoConstants {
    private static final Interner<Object> CONSTANTS = Interners.newWeakInterner();

    private GTGeckoConstants() {
    }

    public static Object share(Object value) {
        if (value == null || !value.getClass().getName().equals("software.bernie.geckolib.loading.math.value.Constant")) {
            return value;
        }
        try {
            // Record equality distinguishes signed zero but canonicalizes NaNs; preserve NaN payloads.
            double number = (double) value.getClass().getMethod("value").invoke(value);
            return Double.isNaN(number) ? value : CONSTANTS.intern(value);
        } catch (ReflectiveOperationException exception) {
            return value;
        }
    }
}
