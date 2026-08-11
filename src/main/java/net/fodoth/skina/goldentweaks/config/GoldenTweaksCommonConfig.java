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

    public static final ModConfigSpec.IntValue PICKUP_DELAY_THRESHOLD;
    public static final ModConfigSpec.BooleanValue ALLOW_INFINITE_DELAY;


    // =========================
    // feature 系统
    // =========================
    public static final ModConfigSpec.BooleanValue ALWAYS_EDIBLE_FOOD;

    public static final ModConfigSpec.BooleanValue LOST_MAID_DROP;

    public static final ModConfigSpec.DoubleValue LOST_MAID_DROP_CHANCE;


    // =========================
    // balance 系统
    // =========================
    public static final ModConfigSpec.IntValue EVOLVED_MEKANISM_SOLAR_MULTIPLIER;

    public static final ModConfigSpec.BooleanValue RECYCLER_FACTORY_STACK_UPGRADES;

    public static final ModConfigSpec.BooleanValue CATACLYSM_BALANCE;

    public static final ModConfigSpec.IntValue HAGGLER_MAX_DISCOUNT;

    public static final ModConfigSpec.DoubleValue HAGGLER_MAX_DISCOUNT_PERCENTAGE;

    public static final ModConfigSpec.IntValue TC_JEI_ASPECT_MAX_PAGE;

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

        PICKUP_DELAY_THRESHOLD = builder
                .translation(key("pickup.pickup_delay_threshold"))
                .comment(comment("pickup.pickup_delay_threshold"))
                .defineInRange("pickupDelayThreshold", 32766, 0, 32766);

        ALLOW_INFINITE_DELAY = builder
                .translation(key("pickup.allow_infinite_delay"))
                .comment(comment("pickup.allow_infinite_delay"))
                .define("allowInfiniteDelay", false);

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

        LOST_MAID_DROP = builder
                .translation(key("feature.lost_maid_drop"))
                .comment(comment("feature.lost_maid_drop"))
                .define("lostMaidDrop", true);

        LOST_MAID_DROP_CHANCE = builder
                .translation(key("feature.lost_maid_drop_chance"))
                .comment(comment("feature.lost_maid_drop_chance"))
                .defineInRange("lostMaidDropChance", 0.05D, 0.0D, 1.0D);


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
                .defineInRange("solarMultiplier", 1, 1, Integer.MAX_VALUE);

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

        builder.push("cataclysm");

        CATACLYSM_BALANCE = builder
                .translation(key("balance.cataclysm_balance"))
                .comment(comment("balance.cataclysm_balance"))
                .define("cataclysmBalance", true);

        builder.pop();

        builder.push("irons_jewelry");

        HAGGLER_MAX_DISCOUNT = builder
                .translation(key("balance.haggler_max_discount"))
                .comment(comment("balance.haggler_max_discount"))
                .defineInRange("hagglerMaxDiscount", 10, 0, 64);

        HAGGLER_MAX_DISCOUNT_PERCENTAGE = builder
                .translation(key("balance.haggler_max_discount_percentage"))
                .comment(comment("balance.haggler_max_discount_percentage"))
                .defineInRange("hagglerMaxDiscountPercentage", 0.5D, 0.0D, 1.0D);

        builder.pop();

        builder.push("thaumcraft");

        TC_JEI_ASPECT_MAX_PAGE = builder
                .translation(key("balance.tc_jei_aspect_max_page"))
                .comment(comment("balance.tc_jei_aspect_max_page"))
                .defineInRange("TCJEIAspectMaxPage", 10, 0, Integer.MAX_VALUE);

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


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isCataclysmBalanced() {

        if (!GTState.isReady()) {
            return true;
        }

        return CATACLYSM_BALANCE.get();
    }

    public static int getEmSolarMultiplier() {

        if (!GTState.isReady()) {
            return 1;
        }

        return EVOLVED_MEKANISM_SOLAR_MULTIPLIER.get();
    }

    public static int getIJHagglerMaxDiscount() {

        if (!GTState.isReady()) {
            return 10;
        }

        return HAGGLER_MAX_DISCOUNT.get();
    }

    public static double getIJHagglerMaxDiscountPercentage() {

        if (!GTState.isReady()) {
            return 0.5D;
        }

        return HAGGLER_MAX_DISCOUNT_PERCENTAGE.get();
    }

    public static int getTCJEIAspectMaxPage() {

        if (!GTState.isReady()) {
            return 10;
        }

        return TC_JEI_ASPECT_MAX_PAGE.get();
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