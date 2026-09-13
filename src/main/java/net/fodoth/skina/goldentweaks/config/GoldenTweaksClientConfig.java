package net.fodoth.skina.goldentweaks.config;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.GTState;
import net.neoforged.neoforge.common.ModConfigSpec;


public final class GoldenTweaksClientConfig {

    public static final ModConfigSpec SPEC;

    // Misc
    public static final ModConfigSpec.BooleanValue DISABLE_BUILDING_WANDS_BLOCK_PREVIEW;
    public static final ModConfigSpec.IntValue SEARCH_TRIGGER_THRESHOLD;

    // Debug
    public static final ModConfigSpec.BooleanValue DEBUG_GUI_INSPECTOR;
    public static final ModConfigSpec.BooleanValue DEBUG_GUI_COPY_TO_CLIPBOARD;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        /*
         * Misc
         */
        builder.translation(key("category.misc"));
        builder.push("misc");

        DISABLE_BUILDING_WANDS_BLOCK_PREVIEW = builder
                .translation(key("disable_building_wands_block_preview"))
                .comment(comment("disable_building_wands_block_preview"))
                .define("disableBuildingWandsBlockPreview", true);

        SEARCH_TRIGGER_THRESHOLD = builder
                .translation(key("search_trigger_threshold"))
                .comment(comment("search_trigger_threshold"))
                .defineInRange("searchTriggerThreshold", 10, 0, Integer.MAX_VALUE);

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
