package net.fodoth.skina.goldentweaks.compat.yesstevemodel;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/** Share immutable numeric expression triples, never their mutable Vector3f evaluation results. */
public final class GTYsmVectors {
    private static final Object2ObjectOpenHashMap<NumericVector, Object> VECTORS = new Object2ObjectOpenHashMap<>();
    private static final ClassValue<Field[]> FIELDS = new ClassValue<>() {
        @Override
        protected Field[] computeValue(Class<?> type) {
            String[] names = type.getName().equals("com.elfmcys.yesstevemodel.o000OOo0ooo00o0OOOOOOO0O")
                    ? new String[] {"OO000o0ooOooooOOOOO0Ooo0"}
                    : type.getName().equals("com.elfmcys.yesstevemodel.oOO0000OO00oO0000OooO0O0")
                    ? new String[] {"O0OO0O0o00o0o00oOoO0o0oO", "oo000ooO0O00oo0OO0ooO000"}
                    : new String[] {"oOo0OO0O0o000OO0O000oo0o", "oOoo00O0o0oO0o0oO00OO0O0", "OO000o0ooOooooOOOOO0Ooo0"};
            Field[] fields = new Field[names.length];
            try {
                for (int i = 0; i < names.length; i++) {
                    fields[i] = type.getDeclaredField(names[i]);
                    fields[i].setAccessible(true);
                }
                return fields;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Missing YSM vector fields", exception);
            }
        }
    };

    private GTYsmVectors() {
    }

    public static void compact(@NotNull Object builder) {
        if (!builder.getClass().getName().equals("com.elfmcys.yesstevemodel.oOO0000OO00oO0000OooO0O0")) return;
        try {
            for (Field field : FIELDS.get(builder.getClass())) {
                Object value = field.get(builder);
                Object shared = share(value);
                if (shared != value) field.set(builder, shared);
            }
        } catch (IllegalAccessException | IllegalStateException exception) {
            // Optional memory optimization: retain the upstream values on incompatible versions.
        }
    }

    public static @Nullable Object share(@Nullable Object value) {
        Object key = numericKey(value);
        if (key == null) return value;
        synchronized (VECTORS) {
            NumericVector numeric = (NumericVector) key;
            Object existing = VECTORS.get(numeric);
            if (existing != null) return existing;
            if (VECTORS.size() < GoldenTweaksClientConfig.YSM_VECTOR_CACHE_LIMIT) VECTORS.put(numeric, value);
        }
        return value;
    }

    static @Nullable Object numericKey(@Nullable Object value) {
        if (value == null || !value.getClass().getName().equals("com.elfmcys.yesstevemodel.O0o00O0oO000O0oOOO0OoOoo")) return null;
        try {
            Field[] fields = FIELDS.get(value.getClass());
            Integer x = bits(fields[0].get(value));
            Integer y = bits(fields[1].get(value));
            Integer z = bits(fields[2].get(value));
            if (x == null || y == null || z == null) return null;
            return new NumericVector(x, y, z);
        } catch (IllegalAccessException | IllegalStateException exception) {
            return null;
        }
    }

    private static @Nullable Integer bits(@Nullable Object expression) throws IllegalAccessException {
        if (expression == null || !expression.getClass().getName().equals("com.elfmcys.yesstevemodel.o000OOo0ooo00o0OOOOOOO0O")) return null;
        return Float.floatToRawIntBits(FIELDS.get(expression.getClass())[0].getFloat(expression));
    }

    private record NumericVector(int x, int y, int z) {
    }
}
