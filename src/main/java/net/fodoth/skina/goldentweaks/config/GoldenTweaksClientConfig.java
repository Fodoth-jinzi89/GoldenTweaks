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
    /** 把 tooltip 索引推迟到重载之后的后台线程构建（进世界更快；完成前 `$` 类搜索暂不含 tooltip）。 */
    public static final ModConfigSpec.BooleanValue EMI_DEFER_SEARCH_INDEX;
    /** 神秘时代/scex 要素扫描里跳过对每个物品的无用 JEI UID 探错（一次重载省约 22 秒）。 */
    public static final ModConfigSpec.BooleanValue JEI_ASPECT_SCAN_SKIP_UID;
    /** 创造模式标签分组挪到重载之后的后台线程（先按注册顺序可见，随后自动排好序；省 2.65 秒主线程停顿）。 */
    public static final ModConfigSpec.BooleanValue EMI_DEFER_CREATIVE_GROUPS;
    /** mekanicalcreate 的两趟配方目录预计算（约 11 秒）挪到后台；刚进世界时这两类配方暂时看不到。 */
    public static final ModConfigSpec.BooleanValue MEKANICALCREATE_DEFER_RECIPES;
    /** 启动后按启动器传入的 --width/--height 重设窗口尺寸。 */
    public static final ModConfigSpec.BooleanValue WINDOW_FOLLOWS_LAUNCHER;
    /** 星辉魔法降星点光柱：AS 自己的特效管线在部分整合包里画不出像素，这里用原版渲染类型自绘一根。 */
    public static final ModConfigSpec.BooleanValue ASTRALSORCERY_FOCAL_COLUMN;
    /** 女仆核心：首次进入世界时自动弹出的新手教程界面。 */
    public static final ModConfigSpec.BooleanValue DISABLE_MAID_SOUL_TUTORIAL_POPUP;
    /** 女仆核心的 AUI 控制台输入框无法使用中文输入法：让 IMBlocker 把 ApricityUI 界面视为可输入界面。 */
    public static final ModConfigSpec.BooleanValue FIX_MAID_SOUL_AUI_IME;
    /** 女仆核心 / 心契誓约 的快捷键在打开任意界面（聊天栏等）时不再触发。 */
    public static final ModConfigSpec.BooleanValue BLOCK_MAID_HOTKEYS_IN_GUI;
    /** 心契誓约大模型连接总开关：关闭时不再尝试连接大模型，改为在聊天栏提示。 */
    public static final ModConfigSpec.BooleanValue ENABLE_MAID_LLM;
    /** 抑制女仆核心与 ApricityUI 的全部日志输出。 */
    public static final ModConfigSpec.BooleanValue SUPPRESS_MAID_LOGS;
    /** 生物群系丰富的「异常方块」（错误方块）：只随机原版方块，且每 10 秒才变化一次。 */
    public static final ModConfigSpec.BooleanValue OPTIMIZE_ERROR_BLOCK;

    // Debug
    public static final ModConfigSpec.BooleanValue DEBUG_GUI_INSPECTOR;
    public static final ModConfigSpec.BooleanValue DEBUG_GUI_COPY_TO_CLIPBOARD;

    // Pickup
    public static final ModConfigSpec.BooleanValue ENABLE_PICKUP;
    public static final ModConfigSpec.DoubleValue BASE_PICKUP_REACH;
    public static final ModConfigSpec.DoubleValue EXTENDED_REACH_BONUS;
    public static final ModConfigSpec.DoubleValue SEARCH_BOX_INFLATE;
    public static final ModConfigSpec.DoubleValue TRACE_BIAS;
    public static final ModConfigSpec.IntValue MAX_TARGETS;
    public static final ModConfigSpec.IntValue CONTINUOUS_PICKUP_INTERVAL;
    public static final ModConfigSpec.BooleanValue ALLOW_THROUGH_WALLS;
    public static final ModConfigSpec.BooleanValue ALLOW_STANDING_PICKUP;
    public static final ModConfigSpec.BooleanValue ALLOW_SNEAK_PICKUP;
    public static final ModConfigSpec.BooleanValue ALLOW_FULL_INVENTORY_PICKUP;
    public static final ModConfigSpec.BooleanValue BLOCK_USE;
    public static final ModConfigSpec.BooleanValue ALLOW_CONTINUOUS_PICKUP;
    public static final ModConfigSpec.BooleanValue ALLOW_EXPERIENCE_ORB_PICKUP;
    public static final ModConfigSpec.IntValue PICKUP_DELAY_THRESHOLD;
    public static final ModConfigSpec.BooleanValue ALLOW_INFINITE_DELAY;
    public static final ModConfigSpec.BooleanValue LOOTR_QUICK_LOOT;
    public static final ModConfigSpec.BooleanValue LOOTR_PLAY_OPEN_ANIMATION;
    public static final ModConfigSpec.BooleanValue LOOTR_SHOW_FLYING_ITEMS;
    public static final ModConfigSpec.IntValue LOOTR_PICKUP_GROUPS;
    public static final ModConfigSpec.IntValue LOOTR_HOLD_PICKUP_INTERVAL;

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

        EMI_DEFER_SEARCH_INDEX = builder
                .translation(key("emi_defer_search_index"))
                .comment(comment("emi_defer_search_index"))
                .define("emiDeferSearchIndex", true);

        JEI_ASPECT_SCAN_SKIP_UID = builder
                .translation(key("jei_aspect_scan_skip_uid"))
                .comment(comment("jei_aspect_scan_skip_uid"))
                .define("jeiAspectScanSkipUid", true);

        EMI_DEFER_CREATIVE_GROUPS = builder
                .translation(key("emi_defer_creative_groups"))
                .comment(comment("emi_defer_creative_groups"))
                .define("emiDeferCreativeGroups", true);

        MEKANICALCREATE_DEFER_RECIPES = builder
                .translation(key("mekanicalcreate_defer_recipes"))
                .comment(comment("mekanicalcreate_defer_recipes"))
                .define("mekanicalcreateDeferRecipes", true);

        WINDOW_FOLLOWS_LAUNCHER = builder
                .translation(key("window_follows_launcher"))
                .comment(comment("window_follows_launcher"))
                .define("windowFollowsLauncherSize", true);

        ASTRALSORCERY_FOCAL_COLUMN = builder
                .translation(key("astralsorcery_focal_column"))
                .comment(comment("astralsorcery_focal_column"))
                .define("astralsorceryFocalColumn", true);

        DISABLE_MAID_SOUL_TUTORIAL_POPUP = builder
                .translation(key("disable_maid_soul_tutorial_popup"))
                .comment(comment("disable_maid_soul_tutorial_popup"))
                .define("disableMaidSoulTutorialPopup", true);

        FIX_MAID_SOUL_AUI_IME = builder
                .translation(key("fix_maid_soul_aui_ime"))
                .comment(comment("fix_maid_soul_aui_ime"))
                .define("fixMaidSoulAuiIme", true);

        BLOCK_MAID_HOTKEYS_IN_GUI = builder
                .translation(key("block_maid_hotkeys_in_gui"))
                .comment(comment("block_maid_hotkeys_in_gui"))
                .define("blockMaidHotkeysInGui", true);

        ENABLE_MAID_LLM = builder
                .translation(key("enable_maid_llm"))
                .comment(comment("enable_maid_llm"))
                .define("enableMaidLlm", false);

        SUPPRESS_MAID_LOGS = builder
                .translation(key("suppress_maid_logs"))
                .comment(comment("suppress_maid_logs"))
                .define("suppressMaidLogs", true);

        OPTIMIZE_ERROR_BLOCK = builder
                .translation(key("optimize_error_block"))
                .comment(comment("optimize_error_block"))
                .define("optimizeErrorBlock", true);

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

        /*
         * Pickup
         *
         * 这些设定属于客户端：每个玩家各自配置，客户端把结果同步给服务端，
         * 服务端只按经过范围裁剪后的值处理右键拾取请求，因此各玩家配置可以不同。
         */
        builder.translation(key("pickup"));
        builder.push("pickup");

        ENABLE_PICKUP = builder
                .translation(key("pickup.enable"))
                .comment(comment("pickup.enable"))
                .define("enablePickup", false);

        BASE_PICKUP_REACH = builder
                .translation(key("pickup.base_pickup_reach"))
                .comment(comment("pickup.base_pickup_reach"))
                .defineInRange("basePickupReach", 5.0D, 1.0D, 128.0D);

        EXTENDED_REACH_BONUS = builder
                .translation(key("pickup.extended_reach_bonus"))
                .comment(comment("pickup.extended_reach_bonus"))
                .defineInRange("extendedReachBonus", 17.0D, 0.0D, 128.0D);

        SEARCH_BOX_INFLATE = builder
                .translation(key("pickup.search_box_inflate"))
                .comment(comment("pickup.search_box_inflate"))
                .defineInRange("searchBoxInflate", 1.2D, 0.0D, 16.0D);

        TRACE_BIAS = builder
                .translation(key("pickup.trace_bias"))
                .comment(comment("pickup.trace_bias"))
                .defineInRange("traceBias", 1.0D, 0.0D, 64.0D);

        MAX_TARGETS = builder
                .translation(key("pickup.max_targets"))
                .comment(comment("pickup.max_targets"))
                .defineInRange("maxTargets", 64, 1, 1024);

        ALLOW_THROUGH_WALLS = builder
                .translation(key("pickup.allow_through_walls"))
                .comment(comment("pickup.allow_through_walls"))
                .define("allowThroughWalls", false);

        ALLOW_STANDING_PICKUP = builder
                .translation(key("pickup.allow_standing_pickup"))
                .comment(comment("pickup.allow_standing_pickup"))
                .define("allowStandingPickup", true);

        ALLOW_SNEAK_PICKUP = builder
                .translation(key("pickup.allow_sneak_pickup"))
                .comment(comment("pickup.allow_sneak_pickup"))
                .define("allowSneakPickup", false);

        ALLOW_FULL_INVENTORY_PICKUP = builder
                .translation(key("pickup.allow_full_inventory_pickup"))
                .comment(comment("pickup.allow_full_inventory_pickup"))
                .define("allowFullInventoryPickup", false);

        BLOCK_USE = builder
                .translation(key("pickup.block_use"))
                .comment(comment("pickup.block_use"))
                .define("blockUse", true);

        ALLOW_CONTINUOUS_PICKUP = builder
                .translation(key("pickup.allow_continuous_pickup"))
                .comment(comment("pickup.allow_continuous_pickup"))
                .define("allowContinuousPickup", true);

        ALLOW_EXPERIENCE_ORB_PICKUP = builder
                .translation(key("pickup.allow_experience_orb_pickup"))
                .comment(comment("pickup.allow_experience_orb_pickup"))
                .define("allowExperienceOrbPickup", true);

        CONTINUOUS_PICKUP_INTERVAL = builder
                .translation(key("pickup.continuous_pickup_interval"))
                .comment(comment("pickup.continuous_pickup_interval"))
                .defineInRange("continuousPickupInterval", 2, 1, Integer.MAX_VALUE);

        PICKUP_DELAY_THRESHOLD = builder
                .translation(key("pickup.pickup_delay_threshold"))
                .comment(comment("pickup.pickup_delay_threshold"))
                .defineInRange("pickupDelayThreshold", 32766, 0, 32766);

        ALLOW_INFINITE_DELAY = builder
                .translation(key("pickup.allow_infinite_delay"))
                .comment(comment("pickup.allow_infinite_delay"))
                .define("allowInfiniteDelay", false);

        LOOTR_QUICK_LOOT = builder
                .translation(key("pickup.lootr_quick_loot"))
                .comment(comment("pickup.lootr_quick_loot"))
                .define("lootrQuickLoot", false);

        LOOTR_PLAY_OPEN_ANIMATION = builder
                .translation(key("pickup.lootr_play_open_animation"))
                .comment(comment("pickup.lootr_play_open_animation"))
                .define("lootrPlayOpenAnimation", true);

        LOOTR_SHOW_FLYING_ITEMS = builder
                .translation(key("pickup.lootr_show_flying_items"))
                .comment(comment("pickup.lootr_show_flying_items"))
                .define("lootrShowFlyingItems", true);

        LOOTR_PICKUP_GROUPS = builder
                .translation(key("pickup.lootr_pickup_groups"))
                .comment(comment("pickup.lootr_pickup_groups"))
                .defineInRange("lootrPickupGroups", 0, 0, 64);

        LOOTR_HOLD_PICKUP_INTERVAL = builder
                .translation(key("pickup.lootr_hold_pickup_interval"))
                .comment(comment("pickup.lootr_hold_pickup_interval"))
                .defineInRange("lootrHoldPickupInterval", 5, 1, Integer.MAX_VALUE);

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

    public static boolean doAstralSorceryFocalColumn() {
        if (!GTState.isReady()) return true;
        return ASTRALSORCERY_FOCAL_COLUMN.get();
    }

    public static boolean doDisableMaidSoulTutorialPopup() {
        if (!GTState.isReady()) return true;
        return DISABLE_MAID_SOUL_TUTORIAL_POPUP.get();
    }

    public static boolean doFixMaidSoulAuiIme() {
        if (!GTState.isReady()) return true;
        return FIX_MAID_SOUL_AUI_IME.get();
    }

    public static boolean doBlockMaidHotkeysInGui() {
        if (!GTState.isReady()) return true;
        return BLOCK_MAID_HOTKEYS_IN_GUI.get();
    }

    /**
     * 心契誓约大模型连接总开关。客户端配置未加载时（例如专用服务器）返回 true，即不拦截，
     * 避免把服务端的大模型连接误伤掉。
     */
    public static boolean isMaidLlmEnabled() {
        if (!GTState.isReady()) return true;
        try {
            return ENABLE_MAID_LLM.get();
        } catch (Throwable t) {
            return true;
        }
    }

    public static boolean doSuppressMaidLogs() {
        if (!GTState.isReady()) return false;
        try {
            return SUPPRESS_MAID_LOGS.get();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isOptimizeErrorBlockEnabled() {
        if (!GTState.isReady()) return true;
        return OPTIMIZE_ERROR_BLOCK.get();
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
