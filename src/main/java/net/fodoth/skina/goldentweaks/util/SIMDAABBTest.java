package net.fodoth.skina.goldentweaks.util;

import net.fodoth.skina.goldentweaks.GoldenTweaks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SIMDAABBTest {

    private static final boolean SIMD_AVAILABLE;

    private static final Object SPEC;

    private static final Method FROM_ARRAY;
    private static final Method MUL;
    private static final Method ADD;
    private static final Method LT;
    private static final Method ANY_TRUE;

    static {

        boolean available = false;

        Object spec = null;

        Method fromArray = null;
        Method mul = null;
        Method add = null;
        Method lt = null;
        Method anyTrue = null;

        try {

            Class<?> floatVectorClass =
                    Class.forName("jdk.incubator.vector.FloatVector");

            Class<?> vectorClass =
                    Class.forName("jdk.incubator.vector.Vector");

            Class<?> vectorSpeciesClass =
                    Class.forName("jdk.incubator.vector.VectorSpecies");

            Class<?> vectorMaskClass =
                    Class.forName("jdk.incubator.vector.VectorMask");

            Field preferredField =
                    floatVectorClass.getField("SPECIES_PREFERRED");

            spec = preferredField.get(null);

            fromArray =
                    floatVectorClass.getMethod(
                            "fromArray",
                            vectorSpeciesClass,
                            float[].class,
                            int.class
                    );

            mul =
                    floatVectorClass.getMethod(
                            "mul",
                            float.class
                    );

            add =
                    floatVectorClass.getMethod(
                            "add",
                            vectorClass
                    );

            lt =
                    floatVectorClass.getMethod(
                            "lt",
                            float.class
                    );

            anyTrue =
                    vectorMaskClass.getMethod(
                            "anyTrue"
                    );

            available = true;

            GoldenTweaks.LOGGER.info(
                    "SIMD Vector API detected and enabled"
            );

        } catch (Throwable t) {

            GoldenTweaks.LOGGER.warn(
                    "SIMD Vector API unavailable, falling back to scalar"
            );
        }

        SIMD_AVAILABLE = available;

        SPEC = spec;

        FROM_ARRAY = fromArray;
        MUL = mul;
        ADD = add;
        LT = lt;
        ANY_TRUE = anyTrue;
    }

    public static boolean test(
            float[] nx,
            float[] ny,
            float[] nz,
            float[] nw,
            float cx,
            float cy,
            float cz,
            float radius
    ) {

        if (!SIMD_AVAILABLE) {
            return scalarTest(
                    nx,
                    ny,
                    nz,
                    nw,
                    cx,
                    cy,
                    cz,
                    radius
            );
        }

        try {

            int length = getSpeciesLength();

            int bound = 6 - (6 % length);

            int i = 0;

            for (; i < bound; i += length) {

                Object vx = FROM_ARRAY.invoke(
                        null,
                        SPEC,
                        nx,
                        i
                );

                Object vy = FROM_ARRAY.invoke(
                        null,
                        SPEC,
                        ny,
                        i
                );

                Object vz = FROM_ARRAY.invoke(
                        null,
                        SPEC,
                        nz,
                        i
                );

                Object vw = FROM_ARRAY.invoke(
                        null,
                        SPEC,
                        nw,
                        i
                );

                Object dist =
                        ADD.invoke(
                                ADD.invoke(
                                        MUL.invoke(vx, cx),
                                        MUL.invoke(vy, cy)
                                ),
                                ADD.invoke(
                                        MUL.invoke(vz, cz),
                                        vw
                                )
                        );

                Object mask =
                        LT.invoke(dist, -radius);

                boolean any =
                        (boolean) ANY_TRUE.invoke(mask);

                if (any) {
                    return false;
                }
            }

            for (; i < 6; i++) {

                float dist =
                        nx[i] * cx +
                                ny[i] * cy +
                                nz[i] * cz +
                                nw[i];

                if (dist < -radius) {
                    return false;
                }
            }

            return true;

        } catch (Throwable t) {

            GoldenTweaks.LOGGER.warn(
                    "SIMD execution failed, fallback to scalar",
                    t
            );

            return scalarTest(
                    nx,
                    ny,
                    nz,
                    nw,
                    cx,
                    cy,
                    cz,
                    radius
            );
        }
    }

    private static int getSpeciesLength() throws Exception {

        Method lengthMethod =
                SPEC.getClass().getMethod("length");

        return (int) lengthMethod.invoke(SPEC);
    }

    private static boolean scalarTest(
            float[] nx,
            float[] ny,
            float[] nz,
            float[] nw,
            float cx,
            float cy,
            float cz,
            float radius
    ) {

        for (int i = 0; i < 6; i++) {

            float dist =
                    nx[i] * cx +
                            ny[i] * cy +
                            nz[i] * cz +
                            nw[i];

            if (dist < -radius) {
                return false;
            }
        }

        return true;
    }

    private SIMDAABBTest() {
    }
}