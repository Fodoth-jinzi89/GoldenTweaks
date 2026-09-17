package net.fodoth.skina.goldentweaks.config;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.EarthShockHarmMode;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig.*;
import static net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig.*;

public class GoldenTweaksConfigScreen {

    public static Screen create(Screen parent) {

        try {

            Class<?> configBuilderClass = Class.forName(
                    "me.shedaniel.clothconfig2.api.ConfigBuilder"
            );

            Object builder = configBuilderClass
                    .getMethod("create")
                    .invoke(null);

            configBuilderClass
                    .getMethod("setParentScreen", Screen.class)
                    .invoke(builder, parent);

            configBuilderClass
                    .getMethod("setTitle", Component.class)
                    .invoke(builder, Component.translatable("config.goldentweaks.title"));

            Object eb = configBuilderClass
                    .getMethod("entryBuilder")
                    .invoke(builder);

            var getOrCreateCategory = configBuilderClass
                    .getMethod("getOrCreateCategory", Component.class);

            // =========================================================
            // Pickup
            // =========================================================
            Object pickup = getOrCreateCategory.invoke(
                    builder,
                    Component.translatable("config.goldentweaks.pickup")
            );

            ConfigScreenHelper.addDouble(pickup, eb,
                    "config.goldentweaks.pickup.base_pickup_reach",
                    BASE_PICKUP_REACH, 5.0, 1.0, 128.0);

            ConfigScreenHelper.addDouble(pickup, eb,
                    "config.goldentweaks.pickup.extended_reach_bonus",
                    EXTENDED_REACH_BONUS, 17.0, 0.0, 128.0);

            ConfigScreenHelper.addDouble(pickup, eb,
                    "config.goldentweaks.pickup.search_box_inflate",
                    SEARCH_BOX_INFLATE, 1.2, 0.0, 16.0);

            ConfigScreenHelper.addDouble(pickup, eb,
                    "config.goldentweaks.pickup.trace_bias",
                    TRACE_BIAS, 1.0, 0.0, 64.0);

            ConfigScreenHelper.addInt(pickup, eb,
                    "config.goldentweaks.pickup.max_targets",
                    MAX_TARGETS, 64, 1, 1024);

            ConfigScreenHelper.addBool(pickup, eb,
                    "config.goldentweaks.pickup.allow_through_walls",
                    ALLOW_THROUGH_WALLS, false);

            ConfigScreenHelper.addBool(pickup, eb,
                    "config.goldentweaks.pickup.allow_sneak_pickup",
                    ALLOW_SNEAK_PICKUP, false);

            ConfigScreenHelper.addBool(pickup, eb,
                    "config.goldentweaks.pickup.block_use",
                    BLOCK_USE, true);

            ConfigScreenHelper.addBool(pickup, eb,
                    "config.goldentweaks.pickup.allow_continuous_pickup",
                    ALLOW_CONTINUOUS_PICKUP, true);

            ConfigScreenHelper.addInt(pickup, eb,
                    "config.goldentweaks.pickup.continuous_pickup_interval",
                    CONTINUOUS_PICKUP_INTERVAL, 2, 1, Integer.MAX_VALUE);

            ConfigScreenHelper.addBool(pickup, eb,
                    "config.goldentweaks.pickup.allow_experience_orb_pickup",
                    ALLOW_EXPERIENCE_ORB_PICKUP, true);

            ConfigScreenHelper.addInt(pickup, eb,
                    "config.goldentweaks.pickup.pickup_delay_threshold",
                    PICKUP_DELAY_THRESHOLD, 32766, 0, 32766);

            ConfigScreenHelper.addBool(pickup, eb,
                    "config.goldentweaks.pickup.allow_infinite_delay",
                    ALLOW_INFINITE_DELAY, false);

            ConfigScreenHelper.addBool(pickup, eb,
                    "config.goldentweaks.pickup.lootr_quick_loot",
                    LOOTR_QUICK_LOOT, true);
            ConfigScreenHelper.addBool(pickup, eb, "config.goldentweaks.pickup.lootr_play_open_animation", LOOTR_PLAY_OPEN_ANIMATION, true);
            ConfigScreenHelper.addBool(pickup, eb, "config.goldentweaks.pickup.lootr_show_flying_items", LOOTR_SHOW_FLYING_ITEMS, true);
            ConfigScreenHelper.addInt(pickup, eb, "config.goldentweaks.pickup.lootr_pickup_groups", LOOTR_PICKUP_GROUPS, 0, 0, 64);
            ConfigScreenHelper.addInt(pickup, eb, "config.goldentweaks.pickup.lootr_hold_pickup_interval", LOOTR_HOLD_PICKUP_INTERVAL, 5, 1, Integer.MAX_VALUE);

            // =========================================================
            // Feature
            // =========================================================
            Object feature = getOrCreateCategory.invoke(
                    builder,
                    Component.translatable("config.goldentweaks.category.feature")
            );

            ConfigScreenHelper.addBool(feature, eb,
                    "config.goldentweaks.feature.always_edible_food",
                    ALWAYS_EDIBLE_FOOD, true);

            ConfigScreenHelper.addBool(feature, eb,
                    "config.goldentweaks.feature.lost_maid_drop",
                    LOST_MAID_DROP, true);

            ConfigScreenHelper.addDouble(feature, eb,
                    "config.goldentweaks.feature.lost_maid_drop_chance",
                    LOST_MAID_DROP_CHANCE, 0.05D, 0.0D, 1.0D);


            // =========================================================
            // Misc
            // =========================================================
            Object misc = getOrCreateCategory.invoke(
                    builder,
                    Component.translatable("config.goldentweaks.category.misc")
            );

            ConfigScreenHelper.addBool(misc, eb,
                    "config.goldentweaks.disable_building_wands_block_preview",
                    DISABLE_BUILDING_WANDS_BLOCK_PREVIEW, true);

            ConfigScreenHelper.addInt(misc, eb,
                    "config.goldentweaks.search_start_delay",
                    SEARCH_START_DELAY_TICKS, 20, 0, Integer.MAX_VALUE);

            ConfigScreenHelper.addInt(misc, eb,
                    "config.goldentweaks.search_spread_duration",
                    SEARCH_SPREAD_DURATION_TICKS, 20, 0, Integer.MAX_VALUE);

            // =========================================================
            // Balance
            // =========================================================
            Object balance = getOrCreateCategory.invoke(
                    builder,
                    Component.translatable("config.goldentweaks.category.balance")
            );

            ConfigScreenHelper.addInt(balance, eb,
                    "config.goldentweaks.balance.evolved_mekanism_solar_multiplier",
                    EVOLVED_MEKANISM_SOLAR_MULTIPLIER,
                    1, 1, Integer.MAX_VALUE);

            ConfigScreenHelper.addBool(balance, eb,
                    "config.goldentweaks.balance.recycler_factory_stack_upgrades",
                    RECYCLER_FACTORY_STACK_UPGRADES, true);

            ConfigScreenHelper.addBool(balance, eb,
                    "config.goldentweaks.balance.cataclysm_balance",
                    CATACLYSM_BALANCE, true);

            ConfigScreenHelper.addBool(balance, eb,
                    "config.goldentweaks.balance.disable_advanced_infusion_items",
                    DISABLE_ADVANCED_INFUSION_ITEMS, true);

            ConfigScreenHelper.addBool(balance, eb,
                    "config.goldentweaks.balance.remove_thaumonomicon_research_highlight",
                    REMOVE_THAUMONOMICON_RESEARCH_HIGHLIGHT, true);

            ConfigScreenHelper.addInt(balance, eb,
                    "config.goldentweaks.balance.haggler_max_discount",
                    HAGGLER_MAX_DISCOUNT,
                    10, 0, 64);

            ConfigScreenHelper.addDouble(balance, eb,
                    "config.goldentweaks.balance.haggler_max_discount_percentage",
                    HAGGLER_MAX_DISCOUNT_PERCENTAGE,
                    0.5D, 0.0D, 1.0D);

            ConfigScreenHelper.addInt(balance, eb,
                    "config.goldentweaks.balance.tc_jei_aspect_max_page",
                    TC_JEI_ASPECT_MAX_PAGE,
                    10, 0, Integer.MAX_VALUE);

            ConfigScreenHelper.addInt(balance, eb,
                    "config.goldentweaks.balance.thaumcraft.arcane_crafting_cache_size",
                    ARCANE_CRAFTING_CACHE_SIZE,
                    64, 0, 256);

            ConfigScreenHelper.addInt(balance, eb,
                    "config.goldentweaks.balance.thaumcraft.thaumonomicon_aspect_source_items_per_frame",
                    THAUMONOMICON_ASPECT_SOURCE_ITEMS_PER_FRAME,
                    256, 1, Integer.MAX_VALUE);

            ConfigScreenHelper.addBool(balance, eb,
                    "config.goldentweaks.balance.thaumcraft.thaumonomicon_aspect_source_auto_page",
                    THAUMONOMICON_ASPECT_SOURCE_AUTO_PAGE, false);

            ConfigScreenHelper.addInt(balance, eb,
                    "config.goldentweaks.balance.thaumcraft.thaumonomicon_aspect_source_page_interval",
                    THAUMONOMICON_ASPECT_SOURCE_PAGE_INTERVAL,
                    2000, 250, 60000);

            ConfigScreenHelper.addBool(balance, eb,
                    "config.goldentweaks.balance.thaumcraft.arcane_workbench_vanilla_crafting",
                    ARCANE_WORKBENCH_VANILLA_CRAFTING, true);

            ConfigScreenHelper.addBool(balance, eb,
                    "config.goldentweaks.balance.thaumcraft.tc_object_tags_cache",
                    TC_OBJECT_TAGS_CACHE, true);

            ConfigScreenHelper.addBool(balance, eb,
                    "config.goldentweaks.balance.thaumcraft.tc_crucible_recipe_cache",
                    TC_CRUCIBLE_RECIPE_CACHE, true);

            ConfigScreenHelper.addBool(balance, eb,
                    "config.goldentweaks.balance.thaumcraft.tc_research_cache",
                    TC_RESEARCH_CACHE, true);

            ConfigScreenHelper.addEnum(balance, eb,
                    "config.goldentweaks.balance.thaumcraft.earth_shock_harm_mode",
                    EARTH_SHOCK_HARM_MODE,
                    EarthShockHarmMode.OnlyLiving, EarthShockHarmMode.values());

            ConfigScreenHelper.addBool(balance, eb,
                    "config.goldentweaks.balance.thaumcraft.hungry_node_breaks_blocks",
                    HUNGRY_NODE_BREAKS_BLOCKS, false);

            ConfigScreenHelper.addBool(balance, eb,
                    "config.goldentweaks.balance.thaumcraft.horizons_vortex_breaks_blocks",
                    HORIZONS_VORTEX_BREAKS_BLOCKS, false);

            // =========================================================
            // Debug
            // =========================================================
            Object debug = getOrCreateCategory.invoke(
                    builder,
                    Component.translatable("config.goldentweaks.category.debug")
            );

            ConfigScreenHelper.addBool(debug, eb,
                    "config.goldentweaks.debug.debug_gui_inspector",
                    DEBUG_GUI_INSPECTOR, false);

            ConfigScreenHelper.addBool(debug, eb,
                    "config.goldentweaks.debug.debug_gui_copy_to_clipboard",
                    DEBUG_GUI_COPY_TO_CLIPBOARD, false);

            // =========================================================
            // Save
            // =========================================================
            configBuilderClass
                    .getMethod("setSavingRunnable", Runnable.class)
                    .invoke(builder, (Runnable) () -> {
                        GoldenTweaksClientConfig.SPEC.save();
                        GoldenTweaksCommonConfig.SPEC.save();
                    });

            return (Screen) configBuilderClass
                    .getMethod("build")
                    .invoke(builder);

        } catch (Exception ignored) {
            GoldenTweaks.LOGGER.error("Cloth Config API is NOT installed!");
        }
        return parent;
    }
}
