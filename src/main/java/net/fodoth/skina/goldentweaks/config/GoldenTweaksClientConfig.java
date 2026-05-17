package net.fodoth.skina.goldentweaks.config;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.event.ClientSetupEvent;
import net.fodoth.skina.goldentweaks.util.DSAMode;
import net.fodoth.skina.goldentweaks.util.DSAVariant;
import net.fodoth.skina.goldentweaks.util.GTState;
import net.fodoth.skina.goldentweaks.util.SmartCullingType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;


public final class GoldenTweaksClientConfig {

    public static final ModConfigSpec SPEC;

    // DSA
    public static final ModConfigSpec.EnumValue<DSAMode> DSA_MODE;
    public static final ModConfigSpec.EnumValue<DSAVariant> DSA_VARIANT;

    // Building
    public static final ModConfigSpec.BooleanValue VERTEX_FORMAT_CACHE;
    public static final ModConfigSpec.IntValue RENDER_CYCLE_POOL_SIZE;

    // Misc
    public static final ModConfigSpec.BooleanValue RENDERBUFFER_DEPTH;
    public static final ModConfigSpec.BooleanValue FAST_MATH;
    public static final ModConfigSpec.BooleanValue TEX_BARRIER;
    public static final ModConfigSpec.BooleanValue BATCH_TEXT_RENDERING;

    public static final ModConfigSpec.EnumValue<SmartCullingType> SMART_CULLING;

    // Debug
    public static final ModConfigSpec.BooleanValue DEBUG_GUI_INSPECTOR;
    public static final ModConfigSpec.BooleanValue DEBUG_GUI_COPY_TO_CLIPBOARD;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        /*
         * Building
         */
        builder.translation(key("category.building"));
        builder.push("building");

        DSA_MODE = builder
                .translation(key("dsa"))
                .comment(comment("dsa"))
                .defineEnum("dsa", DSAMode.ALL);

        DSA_VARIANT = builder
                .translation(key("dsa_variant"))
                .comment(comment("dsa_variant"))
                .defineEnum("dsaVariant", DSAVariant.CORE);

        VERTEX_FORMAT_CACHE = builder
                .translation(key("vertex_format_cache"))
                .comment(comment("vertex_format_cache"))
                .define("vertexFormatCache", false);

        RENDER_CYCLE_POOL_SIZE = builder
                .translation(key("render_cycle_pool_size"))
                .comment(comment("render_cycle_pool_size"))
                .defineInRange("renderCyclePoolSize", 256, 128, 432);

        builder.pop();

        /*
         * Misc
         */
        builder.translation(key("category.misc"));
        builder.push("misc");

        RENDERBUFFER_DEPTH = builder
                .translation(key("renderbuffer_depth"))
                .comment(comment("renderbuffer_depth"))
                .define("renderbufferDepth", true);

        FAST_MATH = builder
                .translation(key("fast_math"))
                .comment(comment("fast_math"))
                .define("fastMath", true);

        TEX_BARRIER = builder
                .translation(key("tex_barrier"))
                .comment(comment("tex_barrier"))
                .define("texBarrier", true);

        BATCH_TEXT_RENDERING = builder
                .translation(key("batch_text_rendering"))
                .comment(comment("batch_text_rendering"))
                .define("batchTextRendering", true);

        SMART_CULLING = builder
                .translation(key("smart_culling"))
                .comment(comment("smart_culling"))
                .defineEnum("smartCulling", SmartCullingType.BASE);

        builder.pop();

        /*
         * Debug
         */
        builder.translation(key("category.debug"));
        builder.push("debug");

        DEBUG_GUI_INSPECTOR = builder
                .translation(key("debug_gui_inspector"))
                .comment(comment("debug_gui_inspector"))
                .define("debugGuiInspector", false);

        DEBUG_GUI_COPY_TO_CLIPBOARD = builder
                .translation(key("debug_gui_copy_to_clipboard"))
                .comment(comment("debug_gui_copy_to_clipboard"))
                .define("debugGuiCopyToClipboard", true);

        builder.pop();

        SPEC = builder.build();
    }

    public static boolean isARB() {
        return DSA_VARIANT.get() == DSAVariant.ARB;
    }

    public static boolean canCreateRenderbuffer() {
        return ClientSetupEvent.GL46
                && RENDERBUFFER_DEPTH.get()
                && !ModList.get().isLoaded("iris");
    }

    public static boolean hasDSA(DSAMode target) {
        if (!GTState.isReady()) return false;
        return computeDSA(target);
    }

    public static boolean doFastMath() {
        if (!GTState.isReady()) return false;
        return FAST_MATH.get();
    }

    public static boolean computeDSA(DSAMode target) {
        boolean cfg =
                (DSA_MODE.get() == DSAMode.ALL || DSA_MODE.get() == target)
                        && ModList.get().isLoaded("sodium");

        return ClientSetupEvent.DSA && cfg;
    }

    public static boolean doDebugGuiInspector() {
        if (!GTState.isReady()) return false;
        return DEBUG_GUI_INSPECTOR.get();
    }

    public static boolean doDebugGuiCopyToClipboard() {
        if (!GTState.isReady()) return false;
        return DEBUG_GUI_COPY_TO_CLIPBOARD.get();
    }

    private static String key(String path) {
        return "config." + GoldenTweaks.MODID + "." + path;
    }

    private static String comment(String path) {
        return key(path) + ".comment";
    }

    private GoldenTweaksClientConfig() {
    }
}