package net.fodoth.skina.goldentweaks.compat.yesstevemodel;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public final class GTYsmSplineFrames {
    private static final Object2ObjectOpenHashMap<NumericFrame, Object> FRAMES = new Object2ObjectOpenHashMap<>();
    private static final ClassValue<Field[]> FIELDS = new ClassValue<>() {
        @Override
        protected Field[] computeValue(Class<?> type) {
            try {
                Class<?> base = type.getSuperclass();
                Field[] fields = {base.getDeclaredField("oOo0OO0O0o000OO0O000oo0o"),
                        base.getDeclaredField("oOoo00O0o0oO0o0oO00OO0O0"),
                        base.getDeclaredField("OOo0o0000Ooo0o00OO0oOOoO"),
                        type.getDeclaredField("Oo0O0OoOo0O0oOoo0000O0oO"),
                        type.getDeclaredField("O0o0OoOOooOo0O0OOoo0Oo00"),
                        type.getDeclaredField("OoooO0OO0000O00oo0Oo00OO"),
                        type.getDeclaredField("oOOO00ooO0oOOoOOo0OoOOOo")};
                for (Field field : fields) field.setAccessible(true);
                return fields;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Missing YSM spline frame fields", exception);
            }
        }
    };

    private GTYsmSplineFrames() {
    }

    public static @Nullable Object share(@Nullable Object value) {
        if (value == null || !value.getClass().getName().equals("com.elfmcys.yesstevemodel.O000oOo0o0OOo0000oooo0O0")) return value;
        try {
            Field[] fields = FIELDS.get(value.getClass());
            float start = fields[0].getFloat(value);
            float duration = fields[1].getFloat(value);
            if (Float.isNaN(start) || Float.isNaN(duration)) return value;
            Object begin = GTYsmVectors.numericKey(fields[2].get(value));
            Object end = GTYsmVectors.numericKey(fields[3].get(value));
            Object post = GTYsmVectors.numericKey(fields[4].get(value));
            Object right = GTYsmVectors.numericKey(fields[5].get(value));
            Object after = GTYsmVectors.numericKey(fields[6].get(value));
            if (begin == null || end == null || post == null || right == null || after == null) return value;
            NumericFrame key = new NumericFrame(Float.floatToRawIntBits(start), Float.floatToRawIntBits(duration), begin, end, post, right, after);
            synchronized (FRAMES) {
                Object existing = FRAMES.get(key);
                if (existing != null) return existing;
                // Bounded across reloads; pure numeric frames cannot retain animation contexts.
                if (FRAMES.size() < GoldenTweaksClientConfig.YSM_SPLINE_FRAME_CACHE_LIMIT) FRAMES.put(key, value);
            }
        } catch (IllegalAccessException | IllegalStateException exception) {
            return value;
        }
        return value;
    }

    private record NumericFrame(int start, int duration, Object begin, Object end, Object post, Object right, Object after) {
    }
}
