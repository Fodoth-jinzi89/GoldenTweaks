package net.fodoth.skina.goldentweaks.util.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.Mth;

import java.math.BigDecimal;
import java.util.OptionalDouble;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public final class GTMath {

    public static final float EPSILON = 1.0E-6F;
    public static final float EPSILON2 = 1.0E-4F;

    public static final float E = (float) Math.E;
    public static final float HALF_E = 1.3591409F;
    public static final float PI_INV = 0.31830987F;

    public static final Codec<BigDecimal> BIG_DECIMAL = Codec.DOUBLE.flatXmap(
            d -> {
                try {
                    return DataResult.success(BigDecimal.valueOf(d));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            },
            bd -> {
                try {
                    return DataResult.success(bd.doubleValue());
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }
    );

    private GTMath() {}

    /* ------------------------------------------------------------ */
    /* basic math
    /* ------------------------------------------------------------ */

    public static float pow(float v, float p) {
        return (float) Math.pow(v, p);
    }

    public static float exp(float v) {
        return (float) Math.exp(v);
    }

    public static float tan(float x) {
        return Mth.sin(x) / Mth.cos(x);
    }

    public static float cot(float x) {
        return 1.0f / tan(x);
    }

    public static float asin(float x) {
        boolean neg = x < 0;
        x = Math.abs(x);

        float r = Mth.sqrt(1.0f - x) * (-0.0187293F * x + 0.074261F);
        r = r * x - 0.2121144F;
        r = r * x + 1.5707288F;
        r = 1.5707963F - r * Mth.sqrt(1.0f - x);

        return neg ? -r : r;
    }

    public static float acos(float x) {
        return (float) (Math.PI / 2.0) - asin(x);
    }

    public static int sqrt(int i) {
        return (int) Mth.sqrt(i);
    }

    public static int cbrt(int i) {
        return (int) Math.cbrt(i);
    }

    public static int sin(int x) {
        return (int) Mth.sin(x);
    }

    public static int cos(int x) {
        return (int) Mth.cos(x);
    }

    public static int tan(int x) {
        return sin(x) / cos(x);
    }

    public static int cot(int x) {
        return 1 / tan(x);
    }

    public static double cot(double x) {
        return 1.0 / Math.tan(x);
    }

    /* ------------------------------------------------------------ */
    /* clamp / min / max
    /* ------------------------------------------------------------ */

    public static short clamp(short v, short min, short max) {
        return (short) Mth.clamp(v, min, max);
    }

    public static byte clamp(byte v, byte min, byte max) {
        return (byte) Mth.clamp(v, min, max);
    }

    public static byte min(byte a, byte b) {
        return (byte) (a <= b ? a : b);
    }

    public static byte max(byte a, byte b) {
        return (byte) (a >= b ? a : b);
    }

    public static short min(short a, short b) {
        return (short) (a <= b ? a : b);
    }

    public static short max(short a, short b) {
        return (short) (a >= b ? a : b);
    }

    /* ------------------------------------------------------------ */
    /* vector utilities (轻量保留版)
    /* ------------------------------------------------------------ */

    public static int lengthSquared(int x, int y) {
        return x * x + y * y;
    }

    public static int lengthSquared(int x, int y, int z) {
        return x * x + y * y + z * z;
    }

    public static int lengthSquared(int x, int y, int z, int w) {
        return x * x + y * y + z * z + w * w;
    }

    public static int length(int x, int y) {
        return (int) Mth.sqrt(lengthSquared(x, y));
    }

    public static int length(int x, int y, int z) {
        return (int) Mth.sqrt(lengthSquared(x, y, z));
    }

    public static int length(int x, int y, int z, int w) {
        return (int) Mth.sqrt(lengthSquared(x, y, z, w));
    }

    /* ------------------------------------------------------------ */

    public static float lengthSquared(float x, float y) {
        return x * x + y * y;
    }

    public static float lengthSquared(float x, float y, float z) {
        return x * x + y * y + z * z;
    }

    public static float lengthSquared(float x, float y, float z, float w) {
        return x * x + y * y + z * z + w * w;
    }

    public static float length(float x, float y) {
        return Mth.sqrt(lengthSquared(x, y));
    }

    public static float length(float x, float y, float z) {
        return Mth.sqrt(lengthSquared(x, y, z));
    }

    public static float length(float x, float y, float z, float w) {
        return Mth.sqrt(lengthSquared(x, y, z, w));
    }

    /* ------------------------------------------------------------ */

    public static double lengthSquared(double x, double y) {
        return x * x + y * y;
    }

    public static double lengthSquared(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    public static double lengthSquared(double x, double y, double z, double w) {
        return x * x + y * y + z * z + w * w;
    }

    public static double length(double x, double y) {
        return Math.sqrt(lengthSquared(x, y));
    }

    public static double length(double x, double y, double z) {
        return Math.sqrt(lengthSquared(x, y, z));
    }

    public static double length(double x, double y, double z, double w) {
        return Math.sqrt(lengthSquared(x, y, z, w));
    }

    /* ------------------------------------------------------------ */
    /* average
    /* ------------------------------------------------------------ */

    public static int average(int... v) {
        return IntStream.of(v).sum() / v.length;
    }

    public static float average(float... v) {
        float sum = 0;
        for (float f : v) sum += f;
        return sum / v.length;
    }

    public static double average(double... v) {
        OptionalDouble opt = DoubleStream.of(v).average();
        return opt.orElse(0.0);
    }

    /* ------------------------------------------------------------ */
    /* misc
    /* ------------------------------------------------------------ */

    public static int sign(float v) {
        return Float.compare(v, 0.0f);
    }

    public static int hypot(int x, int y) {
        return (int) Math.hypot(x, y);
    }

    public static int trunc(int value) {
        return value - value % 2;
    }

    public static float fact(float v) {
        int r = 1;
        for (int i = 1; i <= v; i++) {
            r *= i;
        }
        return r;
    }

    /* ------------------------------------------------------------ */
    /* NaN / Inf
    /* ------------------------------------------------------------ */

    public static boolean isNan(BigDecimal d) {
        return Double.isNaN(d.doubleValue());
    }

    public static boolean isInfinite(BigDecimal d) {
        return Double.isInfinite(d.doubleValue());
    }

    public static boolean isNanOrInfinite(BigDecimal d) {
        return isNan(d) || isInfinite(d);
    }

    public static float invLen(float x, float y, float z) {
        return 1.0F / GTMath.length(x, y, z);
    }
}
