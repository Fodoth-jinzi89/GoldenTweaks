package net.fodoth.skina.goldentweaks.compat.yesstevemodel;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/** Only the immutable, single-float YSM constant is eligible, never a model or evaluation context. */
public final class GTYsmConstants {
    private static final Int2ObjectOpenHashMap<Object> CONSTANTS = new Int2ObjectOpenHashMap<>();
    private static final ClassValue<Field> VALUE_FIELD = new ClassValue<>() {
        @Override
        protected Field computeValue(Class<?> type) {
            try {
                Field field = type.getDeclaredField("OO000o0ooOooooOOOOO0Ooo0");
                field.setAccessible(true);
                return field;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Missing YSM constant value", exception);
            }
        }
    };

    private GTYsmConstants() {
    }

    public static @Nullable Object share(@Nullable Object value) {
        if (value == null || !value.getClass().getName().equals("com.elfmcys.yesstevemodel.o000OOo0ooo00o0OOOOOOO0O")) {
            return value;
        }
        try {
            int bits = Float.floatToRawIntBits(VALUE_FIELD.get(value.getClass()).getFloat(value));
            synchronized (CONSTANTS) {
                Object existing = CONSTANTS.get(bits);
                if (existing != null) return existing;
                // Bounded across reloads; constants cannot retain model resources or world state.
                if (CONSTANTS.size() < GoldenTweaksClientConfig.YSM_CONSTANT_CACHE_LIMIT) CONSTANTS.put(bits, value);
            }
        } catch (IllegalAccessException | IllegalStateException exception) {
            return value;
        }
        return value;
    }
}
