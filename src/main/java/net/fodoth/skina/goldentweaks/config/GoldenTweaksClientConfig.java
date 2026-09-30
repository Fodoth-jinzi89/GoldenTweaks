package net.fodoth.skina.goldentweaks.config;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.GTState;
import net.neoforged.neoforge.common.ModConfigSpec;


public final class GoldenTweaksClientConfig {

    public static final ModConfigSpec SPEC;

    // Misc
    public static final ModConfigSpec.BooleanValue DISABLE_BUILDING_WANDS_BLOCK_PREVIEW;
    /** EMI 搜索内核：用 gram 倒排索引 + 位集求值替换 EMI 的「多重集合物化 + 全量逐项扫描」。 */
    public static final ModConfigSpec.BooleanValue EMI_FAST_SEARCH;
    /** 跳过 vanilla 后缀数组的存储与排序（秒级重载开销），仅在搜索内核启用时有意义。 */
    public static final ModConfigSpec.BooleanValue EMI_SKIP_VANILLA_SEARCH_INDEX;
    /** 启动后按启动器传入的 --width/--height 重设窗口尺寸。 */
    public static final ModConfigSpec.BooleanValue WINDOW_FOLLOWS_LAUNCHER;

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

        EMI_FAST_SEARCH = builder
                .translation(key("emi_fast_search"))
                .comment(comment("emi_fast_search"))
                .define("emiFastSearch", true);

        EMI_SKIP_VANILLA_SEARCH_INDEX = builder
                .translation(key("emi_skip_vanilla_search_index"))
                .comment(comment("emi_skip_vanilla_search_index"))
                .define("emiSkipVanillaSearchIndex", true);

        WINDOW_FOLLOWS_LAUNCHER = builder
                .translation(key("window_follows_launcher"))
                .comment(comment("window_follows_launcher"))
                .define("windowFollowsLauncherSize", true);

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

    public static boolean doFollowLauncherWindowSize() {
        return WINDOW_FOLLOWS_LAUNCHER.get();
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
