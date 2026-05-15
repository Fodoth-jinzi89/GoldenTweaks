package net.fodoth.skina.goldentweaks.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fodoth.skina.goldentweaks.util.DSAMode;
import net.fodoth.skina.goldentweaks.util.DSAVariant;
import net.fodoth.skina.goldentweaks.util.SmartCullingType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static net.fodoth.skina.goldentweaks.config.ConfigScreenHelper.*;
import static net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig.*;
import static net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig.*;

public class GoldenTweaksConfigScreen {

    public static Screen create(Screen parent) {

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.goldentweaks.title"));

        ConfigEntryBuilder eb = builder.entryBuilder();

        // =========================
        // Pickup
        // =========================
        ConfigCategory pickup = builder.getOrCreateCategory(
                Component.translatable("config.goldentweaks.pickup")
        );

        addDouble(pickup, eb, "config.goldentweaks.pickup.base_pickup_reach",
                BASE_PICKUP_REACH, 5.0, 1.0, 128.0);

        addDouble(pickup, eb, "config.goldentweaks.pickup.extended_reach_bonus",
                EXTENDED_REACH_BONUS, 17.0, 0.0, 128.0);

        addDouble(pickup, eb, "config.goldentweaks.pickup.search_box_inflate",
                SEARCH_BOX_INFLATE, 1.2, 0.0, 16.0);

        addDouble(pickup, eb, "config.goldentweaks.pickup.trace_bias",
                TRACE_BIAS, 1.0, 0.0, 64.0);

        addInt(pickup, eb, "config.goldentweaks.pickup.max_targets",
                MAX_TARGETS, 64, 1, 1024);

        addBool(pickup, eb, "config.goldentweaks.pickup.allow_through_walls",
                ALLOW_THROUGH_WALLS, false);

        addBool(pickup, eb, "config.goldentweaks.pickup.allow_sneak_pickup",
                ALLOW_SNEAK_PICKUP, false);

        addBool(pickup, eb, "config.goldentweaks.pickup.block_use",
                BLOCK_USE, true);

        addBool(pickup, eb, "config.goldentweaks.pickup.allow_continuous_pickup",
                ALLOW_CONTINUOUS_PICKUP, true);

        addInt(pickup, eb, "config.goldentweaks.pickup.continuous_pickup_interval",
                CONTINUOUS_PICKUP_INTERVAL, 2, 1, 20);

        // =========================
        // Building / Rendering
        // =========================
        ConfigCategory building = builder.getOrCreateCategory(
                Component.translatable("config.goldentweaks.category.building")
        );

        addEnum(building, eb, "config.goldentweaks.dsa",
                DSA_MODE, DSAMode.ALL, DSAMode.values());

        addEnum(building, eb, "config.goldentweaks.dsa_variant",
                DSA_VARIANT, DSAVariant.CORE, DSAVariant.values());

        addBool(building, eb, "config.goldentweaks.vertex_format_cache",
                VERTEX_FORMAT_CACHE, false);

        addInt(building, eb, "config.goldentweaks.render_cycle_pool_size",
                RENDER_CYCLE_POOL_SIZE, 256, 128, 432);

        // =========================
        // Misc
        // =========================
        ConfigCategory misc = builder.getOrCreateCategory(
                Component.translatable("config.goldentweaks.category.misc")
        );

        addBool(misc, eb, "config.goldentweaks.renderbuffer_depth",
                RENDERBUFFER_DEPTH, true);

        addBool(misc, eb, "config.goldentweaks.fast_math",
                FAST_MATH, true);

        addBool(misc, eb, "config.goldentweaks.tex_barrier",
                TEX_BARRIER, true);

        addBool(misc, eb, "config.goldentweaks.batch_text_rendering",
                BATCH_TEXT_RENDERING, true);

        addEnum(misc, eb, "config.goldentweaks.smart_culling",
                SMART_CULLING, SmartCullingType.BASE, SmartCullingType.values());

        // =========================
        // Balance
        // =========================
        ConfigCategory balance = builder.getOrCreateCategory(
                Component.translatable("config.goldentweaks.category.balance")
        );

        addInt(balance, eb,
                "config.goldentweaks.balance.evolved_mekanism_solar_multiplier",
                EVOLVED_MEKANISM_SOLAR_MULTIPLIER,
                2700,
                1,
                Integer.MAX_VALUE);

        builder.setSavingRunnable(() -> {
            GoldenTweaksClientConfig.SPEC.save();
            GoldenTweaksCommonConfig.SPEC.save();
        });

        return builder.build();
    }
}