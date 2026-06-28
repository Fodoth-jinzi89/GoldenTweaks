package net.fodoth.skina.goldentweaks.config;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.DSAMode;
import net.fodoth.skina.goldentweaks.util.DSAVariant;
import net.fodoth.skina.goldentweaks.util.SmartCullingType;
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
                    CONTINUOUS_PICKUP_INTERVAL, 2, 1, 20);

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
            // Building
            // =========================================================
            Object building = getOrCreateCategory.invoke(
                    builder,
                    Component.translatable("config.goldentweaks.category.building")
            );

            ConfigScreenHelper.addEnum(building, eb,
                    "config.goldentweaks.dsa",
                    DSA_MODE, DSAMode.ALL, DSAMode.values());

            ConfigScreenHelper.addEnum(building, eb,
                    "config.goldentweaks.dsa_variant",
                    DSA_VARIANT, DSAVariant.CORE, DSAVariant.values());

            ConfigScreenHelper.addBool(building, eb,
                    "config.goldentweaks.vertex_format_cache",
                    VERTEX_FORMAT_CACHE, false);

            ConfigScreenHelper.addInt(building, eb,
                    "config.goldentweaks.render_cycle_pool_size",
                    RENDER_CYCLE_POOL_SIZE, 256, 128, 432);

            // =========================================================
            // Misc
            // =========================================================
            Object misc = getOrCreateCategory.invoke(
                    builder,
                    Component.translatable("config.goldentweaks.category.misc")
            );

            ConfigScreenHelper.addBool(misc, eb,
                    "config.goldentweaks.renderbuffer_depth",
                    RENDERBUFFER_DEPTH, true);

            ConfigScreenHelper.addBool(misc, eb,
                    "config.goldentweaks.fast_math",
                    FAST_MATH, true);

            ConfigScreenHelper.addBool(misc, eb,
                    "config.goldentweaks.tex_barrier",
                    TEX_BARRIER, true);

            ConfigScreenHelper.addBool(misc, eb,
                    "config.goldentweaks.batch_text_rendering",
                    BATCH_TEXT_RENDERING, true);

            ConfigScreenHelper.addEnum(misc, eb,
                    "config.goldentweaks.smart_culling",
                    SMART_CULLING, SmartCullingType.BASE, SmartCullingType.values());

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