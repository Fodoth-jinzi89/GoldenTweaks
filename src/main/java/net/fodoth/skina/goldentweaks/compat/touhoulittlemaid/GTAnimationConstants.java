package net.fodoth.skina.goldentweaks.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.value.DoubleValue;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.value.IValue;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.Vector3v;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.BoneKeyFrame;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.TransitionKeyFrame;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.LinearKeyFrame;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.CatmullRomKeyFrame;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;

import java.util.Map;
import java.util.IdentityHashMap;

public final class GTAnimationConstants {

    private static final Interner<String> BONE_NAMES = Interners.newWeakInterner();

    private static final BoneKeyFrame[] EMPTY_FRAMES = new BoneKeyFrame[0];

    private static final DoubleValue NEGATIVE_ZERO = new DoubleValue(-0.0D);
    private static final Vector3v[] VECTORS = new Vector3v[27];
    private static final TransitionKeyFrame[] TRANSITIONS = new TransitionKeyFrame[VECTORS.length * VECTORS.length];
    private static final Long2ObjectOpenHashMap<DoubleValue> CONSTANTS = new Long2ObjectOpenHashMap<>();
    private static final Map<ConstantVector, Vector3v> NUMERIC_VECTORS = new Object2ObjectOpenHashMap<>();
    private static final Map<Vector3v, Boolean> CONSTANT_VECTOR_IDENTITIES = new IdentityHashMap<>();
    private static final Map<LinearFrame, LinearKeyFrame> LINEAR_FRAMES = new Object2ObjectOpenHashMap<>();
    private static final Map<CatmullFrame, CatmullRomKeyFrame> CATMULL_FRAMES = new Object2ObjectOpenHashMap<>();
    private static final Map<TransitionFrame, TransitionKeyFrame> NUMERIC_TRANSITIONS = new Object2ObjectOpenHashMap<>();

    static {
        for (int i = 0; i < VECTORS.length; i++) {
            VECTORS[i] = new Vector3v(component(i / 9), component(i / 3 % 3), component(i % 3));
            CONSTANT_VECTOR_IDENTITIES.put(VECTORS[i], Boolean.TRUE);
        }
    }

    private GTAnimationConstants() {
    }

    public static void clearNumericCaches() {
        synchronized (LINEAR_FRAMES) {
            LINEAR_FRAMES.clear();
        }
        synchronized (CATMULL_FRAMES) {
            CATMULL_FRAMES.clear();
        }
        synchronized (NUMERIC_TRANSITIONS) {
            NUMERIC_TRANSITIONS.clear();
        }
        synchronized (NUMERIC_VECTORS) {
            NUMERIC_VECTORS.clear();
            CONSTANT_VECTOR_IDENTITIES.clear();
            for (Vector3v vector : VECTORS) {
                CONSTANT_VECTOR_IDENTITIES.put(vector, Boolean.TRUE);
            }
        }
        synchronized (CONSTANTS) {
            CONSTANTS.clear();
        }
    }

    public static String reuseBoneName(String name) {
        return name == null ? null : BONE_NAMES.intern(name);
    }

    public static BoneKeyFrame[] reuseEmptyFrames(BoneKeyFrame[] frames) {
        return frames.length == 0 ? EMPTY_FRAMES : frames;
    }

    public static TransitionKeyFrame reuseTransition(double duration, Vector3v begin, Vector3v post,
                                                     Operation<TransitionKeyFrame> original) {
        if (Double.doubleToRawLongBits(duration) == 0L) {
            int beginIndex = -1;
            int postIndex = -1;
            for (int i = 0; i < VECTORS.length; i++) {
                if (begin == VECTORS[i]) beginIndex = i;
                if (post == VECTORS[i]) postIndex = i;
            }
            if (beginIndex >= 0 && postIndex >= 0) {
                synchronized (TRANSITIONS) {
                    int index = beginIndex * VECTORS.length + postIndex;
                    if (TRANSITIONS[index] == null) {
                        TRANSITIONS[index] = original.call(duration, begin, post);
                    }
                    return TRANSITIONS[index];
                }
            }
        }
        synchronized (NUMERIC_VECTORS) {
            if (!CONSTANT_VECTOR_IDENTITIES.containsKey(begin) || !CONSTANT_VECTOR_IDENTITIES.containsKey(post)) {
                return original.call(duration, begin, post);
            }
        }
        TransitionFrame key = new TransitionFrame(Double.doubleToRawLongBits(duration), begin, post);
        synchronized (NUMERIC_TRANSITIONS) {
            TransitionKeyFrame cached = NUMERIC_TRANSITIONS.get(key);
            if (cached != null) {
                return cached;
            }
            TransitionKeyFrame result = original.call(duration, begin, post);
            if (NUMERIC_TRANSITIONS.size() < GoldenTweaksClientConfig.MAID_ANIMATION_TRANSITION_CACHE_LIMIT) {
                NUMERIC_TRANSITIONS.put(key, result);
            }
            return result;
        }
    }

    public static DoubleValue reuse(double value, Operation<DoubleValue> original) {
        // Keep both signs of zero, including the negative zero produced by rotation flips.
        if (value == 0.0D) {
            return Double.doubleToRawLongBits(value) == 0L ? DoubleValue.ZERO : NEGATIVE_ZERO;
        }
        if (value == 1.0D) {
            return DoubleValue.ONE;
        }
        long bits = Double.doubleToRawLongBits(value);
        // ponytail: one lock during animation parsing; the fixed cap also bounds old reload values.
        synchronized (CONSTANTS) {
            DoubleValue cached = CONSTANTS.get(bits);
            if (cached != null) {
                return cached;
            }
            DoubleValue result = original.call(value);
            if (CONSTANTS.size() < GoldenTweaksClientConfig.MAID_ANIMATION_CONSTANT_CACHE_LIMIT) {
                CONSTANTS.put(bits, result);
            }
            return result;
        }
    }

    public static LinearKeyFrame reuseLinear(double start, double duration, Vector3v begin, Vector3v end,
                                             Vector3v post, Operation<LinearKeyFrame> original) {
        synchronized (NUMERIC_VECTORS) {
            if (!CONSTANT_VECTOR_IDENTITIES.containsKey(begin) || !CONSTANT_VECTOR_IDENTITIES.containsKey(end)
                    || !CONSTANT_VECTOR_IDENTITIES.containsKey(post)) {
                return original.call(start, duration, begin, end, post);
            }
        }
        LinearFrame key = new LinearFrame(Double.doubleToRawLongBits(start), Double.doubleToRawLongBits(duration),
                begin, end, post);
        synchronized (LINEAR_FRAMES) {
            LinearKeyFrame cached = LINEAR_FRAMES.get(key);
            if (cached != null) {
                return cached;
            }
            LinearKeyFrame result = original.call(start, duration, begin, end, post);
            if (LINEAR_FRAMES.size() < GoldenTweaksClientConfig.MAID_ANIMATION_LINEAR_CACHE_LIMIT) {
                LINEAR_FRAMES.put(key, result);
            }
            return result;
        }
    }

    public static CatmullRomKeyFrame reuseCatmull(double start, double duration, Vector3v left, Vector3v begin,
                                                 Vector3v end, Vector3v right, Vector3v post,
                                                 Operation<CatmullRomKeyFrame> original) {
        synchronized (NUMERIC_VECTORS) {
            if (!CONSTANT_VECTOR_IDENTITIES.containsKey(left) || !CONSTANT_VECTOR_IDENTITIES.containsKey(begin)
                    || !CONSTANT_VECTOR_IDENTITIES.containsKey(end) || !CONSTANT_VECTOR_IDENTITIES.containsKey(right)
                    || !CONSTANT_VECTOR_IDENTITIES.containsKey(post)) {
                return original.call(start, duration, left, begin, end, right, post);
            }
        }
        CatmullFrame key = new CatmullFrame(Double.doubleToRawLongBits(start), Double.doubleToRawLongBits(duration),
                left, begin, end, right, post);
        synchronized (CATMULL_FRAMES) {
            CatmullRomKeyFrame cached = CATMULL_FRAMES.get(key);
            if (cached != null) {
                return cached;
            }
            CatmullRomKeyFrame result = original.call(start, duration, left, begin, end, right, post);
            if (CATMULL_FRAMES.size() < GoldenTweaksClientConfig.MAID_ANIMATION_CATMULL_CACHE_LIMIT) {
                CATMULL_FRAMES.put(key, result);
            }
            return result;
        }
    }

    public static Vector3v reuseVector(IValue x, IValue y, IValue z, Operation<Vector3v> original) {
        int xIndex = componentIndex(x);
        int yIndex = componentIndex(y);
        int zIndex = componentIndex(z);
        if (xIndex >= 0 && yIndex >= 0 && zIndex >= 0) {
            return VECTORS[xIndex * 9 + yIndex * 3 + zIndex];
        }
        // Exact classes exclude expressions implemented by DoubleValue subclasses.
        if (x != null && y != null && z != null && x.getClass() == DoubleValue.class
                && y.getClass() == DoubleValue.class && z.getClass() == DoubleValue.class) {
            ConstantVector key = new ConstantVector((DoubleValue) x, (DoubleValue) y, (DoubleValue) z);
            synchronized (NUMERIC_VECTORS) {
                Vector3v cached = NUMERIC_VECTORS.get(key);
                if (cached != null) {
                    return cached;
                }
                Vector3v result = original.call(x, y, z);
                if (NUMERIC_VECTORS.size() < GoldenTweaksClientConfig.MAID_ANIMATION_VECTOR_CACHE_LIMIT) {
                    NUMERIC_VECTORS.put(key, result);
                    CONSTANT_VECTOR_IDENTITIES.put(result, Boolean.TRUE);
                }
                return result;
            }
        }
        return original.call(x, y, z);
    }

    // DoubleValue inherits Object equality: keep the original axis references, not just their numbers.
    private record ConstantVector(DoubleValue x, DoubleValue y, DoubleValue z) {
    }

    // Vector3v inherits Object equality; preserve the exact immutable vector references.
    private record LinearFrame(long startBits, long durationBits, Vector3v begin, Vector3v end, Vector3v post) {
    }

    private record CatmullFrame(long startBits, long durationBits, Vector3v left, Vector3v begin,
                               Vector3v end, Vector3v right, Vector3v post) {
    }

    private record TransitionFrame(long durationBits, Vector3v begin, Vector3v post) {
    }

    private static DoubleValue component(int index) {
        return index == 0 ? DoubleValue.ZERO : index == 1 ? DoubleValue.ONE : NEGATIVE_ZERO;
    }

    private static int componentIndex(IValue value) {
        return value == DoubleValue.ZERO ? 0 : value == DoubleValue.ONE ? 1 : value == NEGATIVE_ZERO ? 2 : -1;
    }
}
