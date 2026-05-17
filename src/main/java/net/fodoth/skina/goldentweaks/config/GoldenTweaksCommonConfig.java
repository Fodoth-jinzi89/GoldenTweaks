package net.fodoth.skina.goldentweaks.config;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.GTState;
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
    // feature 系统
    // =========================
    public static final ModConfigSpec.BooleanValue ALWAYS_EDIBLE_FOOD;

    // =========================
    // balance 系统
    // =========================
    public static final ModConfigSpec.IntValue EVOLVED_MEKANISM_SOLAR_MULTIPLIER;

    public static final ModConfigSpec.BooleanValue RECYCLER_FACTORY_STACK_UPGRADES;

    static {

        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // =========================
        // pickup group
        // =========================
        builder.translation(key("pickup"));
        builder.push("pickup");

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

        ALLOW_SNEAK_PICKUP = builder
                .translation(key("pickup.allow_sneak_pickup"))
                .comment(comment("pickup.allow_sneak_pickup"))
                .define("allowSneakPickup", false);

        BLOCK_USE = builder
                .translation(key("pickup.block_use"))
                .comment(comment("pickup.block_use"))
                .define("blockUse", true);

        ALLOW_CONTINUOUS_PICKUP = builder
                .translation(key("pickup.allow_continuous_pickup"))
                .comment(comment("pickup.allow_continuous_pickup"))
                .define("allowContinuousPickup", true);

        CONTINUOUS_PICKUP_INTERVAL = builder
                .translation(key("pickup.continuous_pickup_interval"))
                .comment(comment("pickup.continuous_pickup_interval"))
                .defineInRange("continuousPickupInterval", 2, 1, 20);

        builder.pop();

        // =========================
        // feature group
        // =========================
        builder.translation(key("feature"));
        builder.push("feature");

        ALWAYS_EDIBLE_FOOD = builder
                .translation(key("feature.always_edible_food"))
                .comment(comment("feature.always_edible_food"))
                .define("alwaysEdibleFood", true);

        builder.pop();

        // =========================
        // balance group
        // =========================
        builder.translation(key("balance"));
        builder.push("balance");

        // =========================
        // evolved mekanism
        // =========================
        builder.push("evolved_mekanism");

        EVOLVED_MEKANISM_SOLAR_MULTIPLIER = builder
                .translation(key("balance.evolved_mekanism.solar_multiplier"))
                .comment(comment("balance.evolved_mekanism.solar_multiplier"))
                .defineInRange("solarMultiplier", 2700, 1, Integer.MAX_VALUE);

        builder.pop();

        // =========================
        // mekanism more machines
        // =========================
        builder.push("mekmm");

        RECYCLER_FACTORY_STACK_UPGRADES = builder
                .translation(key("balance.recycler_factory_stack_upgrades"))
                .comment(comment("balance.recycler_factory_stack_upgrades"))
                .define("recyclerFactoryStackUpgrades", true);

        builder.pop();

        builder.pop();

        SPEC = builder.build();
    }

    public static boolean isAlwaysEdible() {

        if (!GTState.isReady()) {
            return false;
        }

        return ALWAYS_EDIBLE_FOOD.get();
    }

    public static boolean isMekmmBalanced() {

        if (!GTState.isReady()) {
            return true;
        }

        return RECYCLER_FACTORY_STACK_UPGRADES.get();
    }

    private static String key(String path) {
        return "config." + GoldenTweaks.MODID + "." + path;
    }

    private static String comment(String path) {
        return key(path) + ".comment";
    }

    private GoldenTweaksCommonConfig() {
    }
}