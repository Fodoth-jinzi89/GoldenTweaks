package net.fodoth.skina.goldentweaks.config;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class GoldenTweaksCommonConfig {

    public static final ModConfigSpec SPEC;

    // =========================
    // pickup 系统
    // =========================
    public static final ModConfigSpec.DoubleValue BASE_PICKUP_REACH;
    public static final ModConfigSpec.DoubleValue EXTENDED_REACH_BONUS;
    public static final ModConfigSpec.DoubleValue SEARCH_BOX_INFLATE;
    public static final ModConfigSpec.DoubleValue TRACE_BIAS;

    public static final ModConfigSpec.IntValue MAX_TARGETS;
    public static final ModConfigSpec.IntValue CONTINUOUS_PICKUP_INTERVAL;

    public static final ModConfigSpec.BooleanValue ALLOW_THROUGH_WALLS;
    public static final ModConfigSpec.BooleanValue ALLOW_SNEAK_PICKUP;
    public static final ModConfigSpec.BooleanValue BLOCK_USE;
    public static final ModConfigSpec.BooleanValue ALLOW_CONTINUOUS_PICKUP;

    // =========================
    // balance 系统
    // =========================
    public static final ModConfigSpec.IntValue EVOLVED_MEKANISM_SOLAR_MULTIPLIER;

    static {

        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // =========================
        // pickup group
        // =========================
        builder.translation(key("pickup"));
        builder.push("pickup");

        BASE_PICKUP_REACH = builder
                .translation(key("pickup.base_pickup_reach"))
                .defineInRange("basePickupReach", 5.0D, 1.0D, 128.0D);

        EXTENDED_REACH_BONUS = builder
                .translation(key("pickup.extended_reach_bonus"))
                .defineInRange("extendedReachBonus", 17.0D, 0.0D, 128.0D);

        SEARCH_BOX_INFLATE = builder
                .translation(key("pickup.search_box_inflate"))
                .defineInRange("searchBoxInflate", 1.2D, 0.0D, 16.0D);

        TRACE_BIAS = builder
                .translation(key("pickup.trace_bias"))
                .defineInRange("traceBias", 1.0D, 0.0D, 64.0D);

        MAX_TARGETS = builder
                .translation(key("pickup.max_targets"))
                .defineInRange("maxTargets", 64, 1, 1024);

        ALLOW_THROUGH_WALLS = builder
                .translation(key("pickup.allow_through_walls"))
                .define("allowThroughWalls", false);

        ALLOW_SNEAK_PICKUP = builder
                .translation(key("pickup.allow_sneak_pickup"))
                .define("allowSneakPickup", false);

        BLOCK_USE = builder
                .translation(key("pickup.block_use"))
                .define("blockUse", true);

        ALLOW_CONTINUOUS_PICKUP = builder
                .translation(key("pickup.allow_continuous_pickup"))
                .define("allowContinuousPickup", true);

        CONTINUOUS_PICKUP_INTERVAL = builder
                .translation(key("pickup.continuous_pickup_interval"))
                .defineInRange("continuousPickupInterval", 2, 1, 20);

        builder.pop();

        // =========================
        // balance group（你新增的核心）
        // =========================
        builder.push("balance");
        builder.translation(key("balance"));

        builder.push("evolved_mekanism");

        EVOLVED_MEKANISM_SOLAR_MULTIPLIER = builder
                .translation(key("balance.evolved_mekanism.solar_multiplier"))
                .comment("Multiplier for Evolved Mekanism solar generators (affects all tiers)")
                .defineInRange("solarMultiplier", 2700, 1, Integer.MAX_VALUE);

        builder.pop(); // evolved_mekanism
        builder.pop(); // balance

        SPEC = builder.build();
    }

    private static String key(String path) {
        return "config." + GoldenTweaks.MODID + "." + path;
    }

    private GoldenTweaksCommonConfig() {}
}