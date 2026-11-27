package net.fodoth.skina.goldentweaks.config;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.EarthShockHarmMode;
import net.fodoth.skina.goldentweaks.util.GTState;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class GoldenTweaksCommonConfig {

    public static final ModConfigSpec SPEC;

    // =========================
    // feature 系统
    // =========================
    public static final ModConfigSpec.BooleanValue ALWAYS_EDIBLE_FOOD;
    public static final ModConfigSpec.BooleanValue GENERATE_AEROFREIGHT_LIST;

    public static final ModConfigSpec.BooleanValue REMOVE_THAUMONOMICON_RESEARCH_HIGHLIGHT;

    public static final ModConfigSpec.BooleanValue LOST_MAID_DROP;

    public static final ModConfigSpec.BooleanValue DISABLE_YUE_LINGLAN_STRUCTURE_TRAIL;

    public static final ModConfigSpec.DoubleValue LOST_MAID_DROP_CHANCE;

    /**
     * Shortest run a batched reaction may take, in ticks.
     *
     * <p>Batch mode scales a fast recipe up until it lasts at least this long, so the machine is not
     * torn down and restarted every fraction of a second. Anything at or above this length runs
     * unchanged. Six point four seconds is the default.</p>
     */
    public static final ModConfigSpec.IntValue CHEMICAL_REACTOR_BATCH_MIN_TICKS;


    // =========================
    // balance 系统
    // =========================
    public static final ModConfigSpec.DoubleValue ARGO_AQUATIC_SPAWN_CHANCE;

    public static final ModConfigSpec.DoubleValue VANILLA_FISH_SPAWN_CHANCE;

    public static final ModConfigSpec.IntValue EVOLVED_MEKANISM_SOLAR_MULTIPLIER;

    public static final ModConfigSpec.IntValue MEKANISM_COMPACT_SPACE_POWER_PERCENT;

    public static final ModConfigSpec.IntValue MEKANISM_TRANSPORTER_TRANSIT_CAP;

    public static final ModConfigSpec.IntValue MEKANISM_TRANSPORTER_FLOWING_CAP;

    public static final ModConfigSpec.IntValue MIO_ICIF_COMPACT_SPACE_SOLAR_PERCENT;

    public static final ModConfigSpec.IntValue MIO_ICIF_COMPACT_SPACE_WIND_PERCENT;

    public static final ModConfigSpec.BooleanValue MIO_ICIF_OIL_RIG_INFINITE;

    public static final ModConfigSpec.BooleanValue MIO_ICIF_TESLA_COIL_HOSTILE_ONLY;

    public static final ModConfigSpec.BooleanValue MIO_ICIF_TESLA_COIL_WEAR_ARMOR;

    public static final ModConfigSpec.BooleanValue MIO_ICIF_REACTOR_OUTPUT_ADJUSTMENT;

    public static final ModConfigSpec.IntValue MIO_ICIF_REACTOR_MULTIPLIER;

    public static final ModConfigSpec.LongValue MIO_ICIF_FUSION_MULTIPLIER;

    public static final ModConfigSpec.IntValue MIO_ICIF_STEAM_REACTOR_MULTIPLIER;

    public static final ModConfigSpec.IntValue MIO_ICIF_STEAM_TUNNEL_MULTIPLIER;

    public static final ModConfigSpec.BooleanValue MIO_ICIF_DISABLE_UU_SOLVER;

    /**
     * 核弹中心槽里，非奇点迭代自带的铀物品按多少辐射量计算。
     *
     * <p>辐射值原本挂在 {@code mio_icif_nuclear_material} 这个类的实例上，外来铀块没有这个类，
     * 取不到数值就会被当成 0 辐射——核弹于是只剩 IC_TNT 那部分威力。默认 0.3，与奇点迭代
     * 自己的普通铀（{@code item_uran}）持平。</p>
     */
    public static final ModConfigSpec.DoubleValue MIO_ICIF_NUKE_FOREIGN_FUEL_RADIOACTIVITY;

    public static final ModConfigSpec.BooleanValue RECYCLER_FACTORY_STACK_UPGRADES;

    public static final ModConfigSpec.IntValue ENTROPY_REACTION_MAX_SPEED_MULTIPLIER;

    public static final ModConfigSpec.BooleanValue CATACLYSM_BALANCE;

    public static final ModConfigSpec.BooleanValue DISABLE_ADVANCED_INFUSION_ITEMS;

    public static final ModConfigSpec.IntValue HAGGLER_MAX_DISCOUNT;

    public static final ModConfigSpec.DoubleValue HAGGLER_MAX_DISCOUNT_PERCENTAGE;

    public static final ModConfigSpec.IntValue ARCANE_CRAFTING_CACHE_SIZE;
    public static final ModConfigSpec.IntValue THAUMONOMICON_ASPECT_SOURCE_ITEMS_PER_FRAME;
    public static final ModConfigSpec.BooleanValue THAUMONOMICON_ASPECT_SOURCE_AUTO_PAGE;
    public static final ModConfigSpec.IntValue THAUMONOMICON_ASPECT_SOURCE_PAGE_INTERVAL;

    public static final ModConfigSpec.BooleanValue ARCANE_WORKBENCH_VANILLA_CRAFTING;

    public static final ModConfigSpec.BooleanValue TC_OBJECT_TAGS_CACHE;

    public static final ModConfigSpec.BooleanValue TC_CRUCIBLE_RECIPE_CACHE;

    public static final ModConfigSpec.BooleanValue TC_RESEARCH_CACHE;

    public static final ModConfigSpec.EnumValue<EarthShockHarmMode> EARTH_SHOCK_HARM_MODE;

    /**
     * Natural spawn weight for ordinary thaumaturges, overriding the mod's own setting.
     *
     * <p>Kept here rather than relying on the mod's config file so a pack can tune it in one place
     * with everything else. Zero disables natural spawns entirely, exactly as the mod's own option
     * does — eggs and spawners still work, since this only feeds the biome modifier.</p>
     */
    public static final ModConfigSpec.IntValue THAUMICCONCILIUM_THAUMATURGE_SPAWN_WEIGHT;

    public static final ModConfigSpec.BooleanValue HUNGRY_NODE_BREAKS_BLOCKS;

    public static final ModConfigSpec.BooleanValue HORIZONS_VORTEX_BREAKS_BLOCKS;

    public static final ModConfigSpec.BooleanValue SIMPLIFIED_GOLEMS;

    public static final ModConfigSpec.BooleanValue DISABLE_TAINT_SPREAD;

    public static final ModConfigSpec.BooleanValue DISABLE_PURE_NODE_BIOME_CONVERSION;

    public static final ModConfigSpec.BooleanValue DISABLE_SINISTER_NODE_BIOME_CONVERSION;

    public static final ModConfigSpec.BooleanValue DISABLE_TAINTED_NODE_BIOME_CONVERSION;

    public static final ModConfigSpec.BooleanValue FURNACE_FUEL_FIRST;

    // =========================
    // academy 兼容
    // =========================
    public static final ModConfigSpec.BooleanValue ACADEMY_MAGIC_ATTACK_DAMAGE;

    public static final ModConfigSpec.DoubleValue ACADEMY_MAGIC_ATTACK_PER_STEP;

    public static final ModConfigSpec.DoubleValue ACADEMY_MAGIC_ATTACK_BONUS_PER_STEP;

    public static final ModConfigSpec.BooleanValue ACADEMY_SKILL_BLOCK_CLAIM_PROTECTION;

    // =========================
    // astralsorcery 平衡
    // =========================
    public static final ModConfigSpec.IntValue ASTRAL_SORCERY_PERK_POINT_CAP;

    public static final ModConfigSpec.BooleanValue ASTRAL_SORCERY_SMOOTH_PERK_CURVE;

    // =========================
    // create ore excavation 平衡
    // =========================
    public static final ModConfigSpec.BooleanValue ORE_VEIN_SHARING;

    public static final ModConfigSpec.DoubleValue ORE_VEIN_SPEED_BONUS;

    public static final ModConfigSpec.DoubleValue ORE_VEIN_YIELD_BONUS;

    public static final ModConfigSpec.DoubleValue ORE_VEIN_SPEED_FLOOR;

    public static final ModConfigSpec.DoubleValue ORE_VEIN_DOUBLE_OUTPUT_CAP;

    public static final ModConfigSpec.IntValue ORE_VEIN_FINDER_RADIUS;

    // =========================
    // oei 兼容
    // =========================
    public static final ModConfigSpec.BooleanValue ORE_UNIFICATION_GUARD;

    public static final ModConfigSpec.BooleanValue ORE_UNIFICATION_SELF_HEAL;

    static {

        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // =========================
        // oei 兼容 group
        // =========================
        builder.translation(key("oei"));
        builder.push("oei");

        ORE_UNIFICATION_GUARD = builder
                .translation(key("oei.ore_unification_guard"))
                .comment(comment("oei.ore_unification_guard"))
                .define("oreUnificationGuard", true);

        ORE_UNIFICATION_SELF_HEAL = builder
                .translation(key("oei.ore_unification_self_heal"))
                .comment(comment("oei.ore_unification_self_heal"))
                .define("oreUnificationSelfHeal", true);

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

        GENERATE_AEROFREIGHT_LIST = builder
                .translation(key("feature.generate_aerofreight_list"))
                .comment("Regenerate AeroFreight Bountiful rewards and reset all pool weights to 1.0 on each world start.")
                .define("generateAeroFreightList", true);

        DISABLE_YUE_LINGLAN_STRUCTURE_TRAIL = builder
                .translation(key("feature.disable_yue_linglan_structure_trail"))
                .comment(comment("feature.disable_yue_linglan_structure_trail"))
                .define("disableYueLinglanStructureTrail", true);

        LOST_MAID_DROP = builder
                .translation(key("feature.lost_maid_drop"))
                .comment(comment("feature.lost_maid_drop"))
                .define("lostMaidDrop", true);

        LOST_MAID_DROP_CHANCE = builder
                .translation(key("feature.lost_maid_drop_chance"))
                .comment(comment("feature.lost_maid_drop_chance"))
                .defineInRange("lostMaidDropChance", 0.05D, 0.0D, 1.0D);

        // 128 ticks is 6.4 seconds at 20 ticks per second.
        // Shown under the multiblock category, but the stored path stays where it has always been:
        // moving it would silently reset every player's chosen value back to the default.
        CHEMICAL_REACTOR_BATCH_MIN_TICKS = builder
                .translation(key("multiblock.batch_min_ticks"))
                .comment(comment("multiblock.batch_min_ticks"))
                .defineInRange("chemicalReactorBatchMinTicks", 128, 1, 72000);


        builder.pop();

        // =========================
        // balance group
        // =========================
        builder.translation(key("balance"));
        builder.push("balance");

        ARGO_AQUATIC_SPAWN_CHANCE = builder
                .translation(key("balance.argo_aquatic_spawn_chance"))
                .comment(comment("balance.argo_aquatic_spawn_chance"))
                .defineInRange("argoAquaticSpawnChance", 0.1D, 0.0D, 1.0D);

        VANILLA_FISH_SPAWN_CHANCE = builder
                .translation(key("balance.vanilla_fish_spawn_chance"))
                .comment(comment("balance.vanilla_fish_spawn_chance"))
                .defineInRange("vanillaFishSpawnChance", 0.1D, 0.0D, 1.0D);

        REMOVE_THAUMONOMICON_RESEARCH_HIGHLIGHT = builder
                .translation(key("balance.remove_thaumonomicon_research_highlight"))
                .comment(comment("balance.remove_thaumonomicon_research_highlight"))
                .define("removeThaumonomiconResearchHighlight", true);

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
        // mekanism compact space
        // =========================
        builder.push("mekanism");

        MEKANISM_COMPACT_SPACE_POWER_PERCENT = builder
                .translation(key("balance.mekanism.compact_space_power_percent"))
                .comment(comment("balance.mekanism.compact_space_power_percent"))
                .defineInRange("compactSpacePowerPercent", 25, 0, Integer.MAX_VALUE);

        MEKANISM_TRANSPORTER_TRANSIT_CAP = builder
                .translation(key("balance.mekanism.transporter_transit_cap"))
                .comment(comment("balance.mekanism.transporter_transit_cap"))
                .defineInRange("transporterTransitCap", 256, 0, Integer.MAX_VALUE);

        MEKANISM_TRANSPORTER_FLOWING_CAP = builder
                .translation(key("balance.mekanism.transporter_flowing_cap"))
                .comment(comment("balance.mekanism.transporter_flowing_cap"))
                .defineInRange("transporterFlowingCap", 1024, 0, Integer.MAX_VALUE);

        builder.pop();

        // =========================
        // mio_icif compact space
        // =========================
        builder.push("mio_icif");

        MIO_ICIF_COMPACT_SPACE_SOLAR_PERCENT = builder
                .translation(key("balance.mio_icif.compact_space_solar_percent"))
                .comment(comment("balance.mio_icif.compact_space_solar_percent"))
                .defineInRange("compactSpaceSolarPercent", 100, 0, Integer.MAX_VALUE);

        MIO_ICIF_COMPACT_SPACE_WIND_PERCENT = builder
                .translation(key("balance.mio_icif.compact_space_wind_percent"))
                .comment(comment("balance.mio_icif.compact_space_wind_percent"))
                .defineInRange("compactSpaceWindPercent", 100, 0, Integer.MAX_VALUE);

        MIO_ICIF_OIL_RIG_INFINITE = builder
                .translation(key("balance.mio_icif.oil_rig_infinite"))
                .comment(comment("balance.mio_icif.oil_rig_infinite"))
                .define("oilRigInfinite", true);

        MIO_ICIF_TESLA_COIL_HOSTILE_ONLY = builder
                .translation(key("balance.mio_icif.tesla_coil_hostile_only"))
                .comment(comment("balance.mio_icif.tesla_coil_hostile_only"))
                .define("teslaCoilHostileOnly", true);

        MIO_ICIF_TESLA_COIL_WEAR_ARMOR = builder
                .translation(key("balance.mio_icif.tesla_coil_wear_armor"))
                .comment(comment("balance.mio_icif.tesla_coil_wear_armor"))
                .define("teslaCoilWearArmor", true);

        MIO_ICIF_REACTOR_OUTPUT_ADJUSTMENT = builder
                .translation(key("balance.mio_icif.reactor_output_adjustment"))
                .comment(comment("balance.mio_icif.reactor_output_adjustment"))
                .define("reactorOutputAdjustment", true);

        MIO_ICIF_REACTOR_MULTIPLIER = builder
                .translation(key("balance.mio_icif.reactor_multiplier"))
                .comment(comment("balance.mio_icif.reactor_multiplier"))
                .defineInRange("reactorMultiplier", 10, 1, 1000);

        MIO_ICIF_FUSION_MULTIPLIER = builder
                .translation(key("balance.mio_icif.fusion_multiplier"))
                .comment(comment("balance.mio_icif.fusion_multiplier"))
                .defineInRange("fusionMultiplier", 1000000L, 1L, 1_000_000_000L);

        MIO_ICIF_STEAM_REACTOR_MULTIPLIER = builder
                .translation(key("balance.mio_icif.steam_reactor_multiplier"))
                .comment(comment("balance.mio_icif.steam_reactor_multiplier"))
                .defineInRange("steamReactorMultiplier", 10, 1, 1000);

        MIO_ICIF_STEAM_TUNNEL_MULTIPLIER = builder
                .translation(key("balance.mio_icif.steam_tunnel_multiplier"))
                .comment(comment("balance.mio_icif.steam_tunnel_multiplier"))
                .defineInRange("steamTunnelMultiplier", 10, 1, 1000);

        MIO_ICIF_DISABLE_UU_SOLVER = builder
                .translation(key("balance.mio_icif.disable_uu_solver"))
                .comment(comment("balance.mio_icif.disable_uu_solver"))
                .define("disableUuSolver", true);

        MIO_ICIF_NUKE_FOREIGN_FUEL_RADIOACTIVITY = builder
                .translation(key("balance.mio_icif.nuke_foreign_fuel_radioactivity"))
                .comment(comment("balance.mio_icif.nuke_foreign_fuel_radioactivity"))
                .defineInRange("nukeForeignFuelRadioactivity", 0.3D, 0.0D, 1000.0D);

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

        // =========================
        // ae2cs
        // =========================
        builder.push("ae2cs");

        ENTROPY_REACTION_MAX_SPEED_MULTIPLIER = builder
                .translation(key("balance.ae2cs.max_speed_multiplier"))
                .comment(comment("balance.ae2cs.max_speed_multiplier"))
                .defineInRange("maxSpeedMultiplier", 2, 1, 16);

        builder.pop();

        builder.push("cataclysm");

        CATACLYSM_BALANCE = builder
                .translation(key("balance.cataclysm_balance"))
                .comment(comment("balance.cataclysm_balance"))
                .define("cataclysmBalance", true);


        builder.pop();

        DISABLE_ADVANCED_INFUSION_ITEMS = builder
                .translation(key("balance.disable_advanced_infusion_items"))
                .comment(comment("balance.disable_advanced_infusion_items"))
                .define("disableAdvancedInfusionItems", true);

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

        ARCANE_CRAFTING_CACHE_SIZE = builder
                .translation(key("balance.thaumcraft.arcane_crafting_cache_size"))
                .comment(comment("balance.thaumcraft.arcane_crafting_cache_size"))
                .defineInRange("arcaneCraftingCacheSize", 64, 0, 256);

        THAUMONOMICON_ASPECT_SOURCE_ITEMS_PER_FRAME = builder
                .translation(key("balance.thaumcraft.thaumonomicon_aspect_source_items_per_frame"))
                .comment(comment("balance.thaumcraft.thaumonomicon_aspect_source_items_per_frame"))
                .defineInRange("thaumonomiconAspectSourceItemsPerFrame", 256, 1, Integer.MAX_VALUE);

        THAUMONOMICON_ASPECT_SOURCE_AUTO_PAGE = builder
                .translation(key("balance.thaumcraft.thaumonomicon_aspect_source_auto_page"))
                .comment(comment("balance.thaumcraft.thaumonomicon_aspect_source_auto_page"))
                .define("thaumonomiconAspectSourceAutoPage", false);

        THAUMONOMICON_ASPECT_SOURCE_PAGE_INTERVAL = builder
                .translation(key("balance.thaumcraft.thaumonomicon_aspect_source_page_interval"))
                .comment(comment("balance.thaumcraft.thaumonomicon_aspect_source_page_interval"))
                .defineInRange("thaumonomiconAspectSourcePageInterval", 2000, 250, 60000);

        ARCANE_WORKBENCH_VANILLA_CRAFTING = builder
                .translation(key("balance.thaumcraft.arcane_workbench_vanilla_crafting"))
                .comment(comment("balance.thaumcraft.arcane_workbench_vanilla_crafting"))
                .define("arcaneWorkbenchVanillaCrafting", true);

        TC_OBJECT_TAGS_CACHE = builder
                .translation(key("balance.thaumcraft.tc_object_tags_cache"))
                .comment(comment("balance.thaumcraft.tc_object_tags_cache"))
                .define("tcObjectTagsCache", true);

        TC_CRUCIBLE_RECIPE_CACHE = builder
                .translation(key("balance.thaumcraft.tc_crucible_recipe_cache"))
                .comment(comment("balance.thaumcraft.tc_crucible_recipe_cache"))
                .define("tcCrucibleRecipeCache", true);

        TC_RESEARCH_CACHE = builder
                .translation(key("balance.thaumcraft.tc_research_cache"))
                .comment(comment("balance.thaumcraft.tc_research_cache"))
                .define("tcResearchCache", true);

        EARTH_SHOCK_HARM_MODE = builder
                .translation(key("balance.thaumcraft.earth_shock_harm_mode"))
                .comment(comment("balance.thaumcraft.earth_shock_harm_mode"))
                .defineEnum("earthShockHarmMode", EarthShockHarmMode.OnlyLiving);

        HUNGRY_NODE_BREAKS_BLOCKS = builder
                .translation(key("balance.thaumcraft.hungry_node_breaks_blocks"))
                .comment(comment("balance.thaumcraft.hungry_node_breaks_blocks"))
                .define("hungryNodeBreaksBlocks", false);

        HORIZONS_VORTEX_BREAKS_BLOCKS = builder
                .translation(key("balance.thaumcraft.horizons_vortex_breaks_blocks"))
                .comment(comment("balance.thaumcraft.horizons_vortex_breaks_blocks"))
                .define("horizonsVortexBreaksBlocks", false);

        SIMPLIFIED_GOLEMS = builder
                .translation(key("balance.thaumcraft.simplified_golems"))
                .comment(comment("balance.thaumcraft.simplified_golems"))
                .define("simplifiedGolems", true);

        DISABLE_TAINT_SPREAD = builder
                .translation(key("balance.thaumcraft.disable_taint_spread"))
                .comment(comment("balance.thaumcraft.disable_taint_spread"))
                .define("disableTaintSpread", true);

        DISABLE_PURE_NODE_BIOME_CONVERSION = builder
                .translation(key("balance.thaumcraft.disable_pure_node_biome_conversion"))
                .comment(comment("balance.thaumcraft.disable_pure_node_biome_conversion"))
                .define("disablePureNodeBiomeConversion", false);

        DISABLE_SINISTER_NODE_BIOME_CONVERSION = builder
                .translation(key("balance.thaumcraft.disable_sinister_node_biome_conversion"))
                .comment(comment("balance.thaumcraft.disable_sinister_node_biome_conversion"))
                .define("disableSinisterNodeBiomeConversion", true);

        DISABLE_TAINTED_NODE_BIOME_CONVERSION = builder
                .translation(key("balance.thaumcraft.disable_tainted_node_biome_conversion"))
                .comment(comment("balance.thaumcraft.disable_tainted_node_biome_conversion"))
                .define("disableTaintedNodeBiomeConversion", true);

        builder.pop();

        // =========================
        // thaumic concilium
        // =========================
        builder.push("thaumicconcilium");

        THAUMICCONCILIUM_THAUMATURGE_SPAWN_WEIGHT = builder
                .translation(key("balance.thaumicconcilium.thaumaturge_spawn_weight"))
                .comment(comment("balance.thaumicconcilium.thaumaturge_spawn_weight"))
                .defineInRange("thaumaturgeSpawnWeight", -1, -1, Integer.MAX_VALUE);

        builder.pop();

        // =========================
        // thaumic bases
        // =========================
        builder.push("thaumicbases");

        FURNACE_FUEL_FIRST = builder
                .translation(key("balance.thaumicbases.furnace_fuel_first"))
                .comment(comment("balance.thaumicbases.furnace_fuel_first"))
                .define("furnaceFuelFirst", true);

        builder.pop();

        // =========================
        // academy
        // =========================
        builder.push("academy");

        ACADEMY_MAGIC_ATTACK_DAMAGE = builder
                .translation(key("balance.academy.magic_attack_damage"))
                .comment(comment("balance.academy.magic_attack_damage"))
                .define("magicAttackDamage", true);

        ACADEMY_MAGIC_ATTACK_PER_STEP = builder
                .translation(key("balance.academy.magic_attack_per_step"))
                .comment(comment("balance.academy.magic_attack_per_step"))
                .defineInRange("magicAttackPerStep", 5.0D, 0.1D, 10000.0D);

        ACADEMY_MAGIC_ATTACK_BONUS_PER_STEP = builder
                .translation(key("balance.academy.magic_attack_bonus_per_step"))
                .comment(comment("balance.academy.magic_attack_bonus_per_step"))
                .defineInRange("magicAttackBonusPerStep", 0.10D, 0.0D, 100.0D);

        ACADEMY_SKILL_BLOCK_CLAIM_PROTECTION = builder
                .translation(key("balance.academy.skill_block_claim_protection"))
                .comment(comment("balance.academy.skill_block_claim_protection"))
                .define("skillBlockClaimProtection", true);

        builder.pop();

        // =========================
        // astralsorcery
        // =========================
        builder.push("astralsorcery");

        ASTRAL_SORCERY_PERK_POINT_CAP = builder
                .translation(key("balance.astralsorcery.perk_point_cap"))
                .comment(comment("balance.astralsorcery.perk_point_cap"))
                .defineInRange("perkPointCap", 364, 1, Integer.MAX_VALUE);

        ASTRAL_SORCERY_SMOOTH_PERK_CURVE = builder
                .translation(key("balance.astralsorcery.smooth_perk_curve"))
                .comment(comment("balance.astralsorcery.smooth_perk_curve"))
                .define("smoothPerkCurve", true);

        builder.pop();

        // =========================
        // create ore excavation
        // =========================
        builder.push("createoreexcavation");

        ORE_VEIN_SHARING = builder
                .translation(key("balance.createoreexcavation.encourage_vein_sharing"))
                .comment(comment("balance.createoreexcavation.encourage_vein_sharing"))
                .define("encourageVeinSharing", true);

        ORE_VEIN_SPEED_BONUS = builder
                .translation(key("balance.createoreexcavation.vein_speed_bonus"))
                .comment(comment("balance.createoreexcavation.vein_speed_bonus"))
                .defineInRange("veinSpeedBonus", 0.20D, 0.0D, 1.0D);

        ORE_VEIN_YIELD_BONUS = builder
                .translation(key("balance.createoreexcavation.vein_yield_bonus"))
                .comment(comment("balance.createoreexcavation.vein_yield_bonus"))
                .defineInRange("veinYieldBonus", 0.20D, 0.0D, 1.0D);

        ORE_VEIN_SPEED_FLOOR = builder
                .translation(key("balance.createoreexcavation.vein_speed_floor"))
                .comment(comment("balance.createoreexcavation.vein_speed_floor"))
                .defineInRange("veinSpeedFloor", 0.50D, 0.0D, 1.0D);

        ORE_VEIN_DOUBLE_OUTPUT_CAP = builder
                .translation(key("balance.createoreexcavation.vein_double_output_cap"))
                .comment(comment("balance.createoreexcavation.vein_double_output_cap"))
                .defineInRange("veinDoubleOutputCap", 1.00D, 0.0D, 1.0D);

        ORE_VEIN_FINDER_RADIUS = builder
                .translation(key("balance.createoreexcavation.vein_finder_radius"))
                .comment(comment("balance.createoreexcavation.vein_finder_radius"))
                .defineInRange("veinFinderRadius", 2500, 0, 10000);

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

    public static int getEntropyReactionMaxSpeedMultiplier() {

        if (!GTState.isReady()) {
            return 2;
        }

        return ENTROPY_REACTION_MAX_SPEED_MULTIPLIER.get();
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isCataclysmBalanced() {

        if (!GTState.isReady()) {
            return true;
        }

        return CATACLYSM_BALANCE.get();
    }

    public static boolean disableAdvancedInfusionItems() {
        return DISABLE_ADVANCED_INFUSION_ITEMS.get();
    }

    public static boolean isOreUnificationGuardEnabled() {
        return ORE_UNIFICATION_GUARD.get();
    }

    public static boolean isOreUnificationSelfHealEnabled() {
        return ORE_UNIFICATION_SELF_HEAL.get();
    }

    public static int getEmSolarMultiplier() {

        if (!GTState.isReady()) {
            return 1;
        }

        return EVOLVED_MEKANISM_SOLAR_MULTIPLIER.get();
    }

    public static int getMekanismCompactSpacePowerPercent() {

        if (!GTState.isReady()) {
            return 25;
        }

        return MEKANISM_COMPACT_SPACE_POWER_PERCENT.get();
    }

    public static float getMekanismCompactSpacePowerFraction() {
        return getMekanismCompactSpacePowerPercent() / 100.0F;
    }

    public static int getMekanismTransporterTransitCap() {

        if (!GTState.isReady()) {
            return 256;
        }

        return MEKANISM_TRANSPORTER_TRANSIT_CAP.get();
    }

    public static int getMekanismTransporterFlowingCap() {

        if (!GTState.isReady()) {
            return 1024;
        }

        return MEKANISM_TRANSPORTER_FLOWING_CAP.get();
    }

    public static int getMioIcifCompactSpaceSolarPercent() {

        if (!GTState.isReady()) {
            return 100;
        }

        return MIO_ICIF_COMPACT_SPACE_SOLAR_PERCENT.get();
    }

    public static float getMioIcifCompactSpaceSolarFraction() {
        return getMioIcifCompactSpaceSolarPercent() / 100.0F;
    }

    public static int getMioIcifCompactSpaceWindPercent() {

        if (!GTState.isReady()) {
            return 100;
        }

        return MIO_ICIF_COMPACT_SPACE_WIND_PERCENT.get();
    }

    public static float getMioIcifCompactSpaceWindFraction() {
        return getMioIcifCompactSpaceWindPercent() / 100.0F;
    }

    public static boolean isMioIcifOilRigInfinite() {
        return GTState.isReady() ? MIO_ICIF_OIL_RIG_INFINITE.get() : true;
    }

    public static boolean isMioIcifTeslaCoilHostileOnly() {
        return GTState.isReady() ? MIO_ICIF_TESLA_COIL_HOSTILE_ONLY.get() : true;
    }

    public static boolean isMioIcifTeslaCoilWearArmor() {
        return GTState.isReady() ? MIO_ICIF_TESLA_COIL_WEAR_ARMOR.get() : true;
    }

    /**
     * 核电产能调整总开关。关闭时核反应堆恢复原版数值，并重新显示方块物品的「能量等级」提示。
     */
    public static boolean isMioIcifReactorOutputAdjusted() {
        return GTState.isReady() ? MIO_ICIF_REACTOR_OUTPUT_ADJUSTMENT.get() : true;
    }

    public static int getMioIcifReactorMultiplier() {
        return GTState.isReady() ? MIO_ICIF_REACTOR_MULTIPLIER.get() : 10;
    }

    /** 关闭调整时返回 1，让倍率相乘的调用点自然退化为原版行为。 */
    public static int getEffectiveMioIcifReactorMultiplier() {
        return isMioIcifReactorOutputAdjusted() ? getMioIcifReactorMultiplier() : 1;
    }

    /**
     * 聚变堆产能倍率，默认 1,000,000。
     *
     * <p>原始输出被硬编码在 2,147,483,647（int 上限）处，这里同时放开该截断，
     * 否则倍率超过 1 就会被截回原值。</p>
     */
    public static long getMioIcifFusionMultiplier() {
        return GTState.isReady() ? MIO_ICIF_FUSION_MULTIPLIER.get() : 1000000L;
    }

    /**
     * 蒸汽反应堆流体倍率，默认 10。
     *
     * <p>统一放大内部水/蒸汽储量、每次循环的耗水量与产汽量。原版把水汽换算写死在
     * {@code 1 水 = 100 蒸汽} 与 {@code 蒸汽 = 热量 × 40}，放大储量而不放大循环预算
     * 只会让水箱更耐存、产出不变，所以这里同时放大循环预算。</p>
     */
    public static int getMioIcifSteamReactorMultiplier() {
        return GTState.isReady() ? MIO_ICIF_STEAM_REACTOR_MULTIPLIER.get() : 10;
    }

    /**
     * 蒸汽隧道（涡轮机）流体倍率，默认 10。
     *
     * <p>放大流体端口的蒸汽储量、每次做功消耗的蒸汽量，以及随之而来的发电量；
     * 电力端口的储量与输出也随之放大，否则产出会被端口容量卡回原值。</p>
     */
    public static int getMioIcifSteamTunnelMultiplier() {
        return GTState.isReady() ? MIO_ICIF_STEAM_TUNNEL_MULTIPLIER.get() : 10;
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

    public static int getArcaneCraftingCacheSize() {

        if (!GTState.isReady()) {
            return 64;
        }

        return ARCANE_CRAFTING_CACHE_SIZE.get();
    }

    public static int getThaumonomiconAspectSourceItemsPerFrame() {
        if (!GTState.isReady()) {
            return 256;
        }
        return THAUMONOMICON_ASPECT_SOURCE_ITEMS_PER_FRAME.get();
    }

    public static boolean isThaumonomiconAspectSourceAutoPage() {
        return GTState.isReady() && THAUMONOMICON_ASPECT_SOURCE_AUTO_PAGE.get();
    }

    public static int getThaumonomiconAspectSourcePageInterval() {
        if (!GTState.isReady()) {
            return 2000;
        }
        return THAUMONOMICON_ASPECT_SOURCE_PAGE_INTERVAL.get();
    }

    public static boolean isArcaneWorkbenchVanillaCrafting() {

        if (!GTState.isReady()) {
            return true;
        }

        return ARCANE_WORKBENCH_VANILLA_CRAFTING.get();
    }

    public static boolean isTcObjectTagsCache() {

        if (!GTState.isReady()) {
            return true;
        }

        return TC_OBJECT_TAGS_CACHE.get();
    }

    public static boolean isTcCrucibleRecipeCache() {

        if (!GTState.isReady()) {
            return true;
        }

        return TC_CRUCIBLE_RECIPE_CACHE.get();
    }

    public static boolean isTcResearchCache() {

        if (!GTState.isReady()) {
            return true;
        }

        return TC_RESEARCH_CACHE.get();
    }

    public static EarthShockHarmMode getEarthShockHarmMode() {

        if (!GTState.isReady()) {
            return EarthShockHarmMode.OnlyLiving;
        }

        return EARTH_SHOCK_HARM_MODE.get();
    }

    public static boolean doesHungryNodeBreakBlocks() {

        if (!GTState.isReady()) {
            return false;
        }

        return HUNGRY_NODE_BREAKS_BLOCKS.get();
    }

    public static boolean doesHorizonsVortexBreakBlocks() {

        if (!GTState.isReady()) {
            return false;
        }

        return HORIZONS_VORTEX_BREAKS_BLOCKS.get();
    }

    public static boolean isSimplifiedGolems() {

        if (!GTState.isReady()) {
            return true;
        }

        return SIMPLIFIED_GOLEMS.get();
    }

    public static boolean isTaintSpreadDisabled() {

        if (!GTState.isReady()) {
            return true;
        }

        return DISABLE_TAINT_SPREAD.get();
    }

    public static boolean isPureNodeBiomeConversionDisabled() {

        if (!GTState.isReady()) {
            return false;
        }

        return DISABLE_PURE_NODE_BIOME_CONVERSION.get();
    }

    public static boolean isSinisterNodeBiomeConversionDisabled() {

        if (!GTState.isReady()) {
            return true;
        }

        return DISABLE_SINISTER_NODE_BIOME_CONVERSION.get();
    }

    public static boolean isTaintedNodeBiomeConversionDisabled() {

        if (!GTState.isReady()) {
            return true;
        }

        return DISABLE_TAINTED_NODE_BIOME_CONVERSION.get();
    }

    public static boolean isFurnaceFuelFirst() {

        if (!GTState.isReady()) {
            return true;
        }

        return FURNACE_FUEL_FIRST.get();
    }

    public static boolean isAcademyMagicAttackDamageEnabled() {

        if (!GTState.isReady()) {
            return true;
        }

        return ACADEMY_MAGIC_ATTACK_DAMAGE.get();
    }

    public static double getAcademyMagicAttackPerStep() {

        if (!GTState.isReady()) {
            return 5.0D;
        }

        return ACADEMY_MAGIC_ATTACK_PER_STEP.get();
    }

    public static double getAcademyMagicAttackBonusPerStep() {

        if (!GTState.isReady()) {
            return 0.10D;
        }

        return ACADEMY_MAGIC_ATTACK_BONUS_PER_STEP.get();
    }

    public static boolean isAcademySkillBlockClaimProtectionEnabled() {

        if (!GTState.isReady()) {
            return true;
        }

        return ACADEMY_SKILL_BLOCK_CLAIM_PROTECTION.get();
    }

    public static int getAstralSorceryPerkPointCap() {

        if (!GTState.isReady()) {
            return 364;
        }

        return ASTRAL_SORCERY_PERK_POINT_CAP.get();
    }

    public static boolean isAstralSorcerySmoothPerkCurve() {

        if (!GTState.isReady()) {
            return true;
        }

        return ASTRAL_SORCERY_SMOOTH_PERK_CURVE.get();
    }

    public static boolean isOreVeinSharingEnabled() {

        if (!GTState.isReady()) {
            return true;
        }

        return ORE_VEIN_SHARING.get();
    }

    public static double getOreVeinSpeedBonus() {

        if (!GTState.isReady()) {
            return 0.20D;
        }

        return ORE_VEIN_SPEED_BONUS.get();
    }

    public static double getOreVeinYieldBonus() {

        if (!GTState.isReady()) {
            return 0.20D;
        }

        return ORE_VEIN_YIELD_BONUS.get();
    }

    public static double getOreVeinSpeedFloor() {

        if (!GTState.isReady()) {
            return 0.50D;
        }

        return ORE_VEIN_SPEED_FLOOR.get();
    }

    public static double getOreVeinDoubleOutputCap() {

        if (!GTState.isReady()) {
            return 1.00D;
        }

        return ORE_VEIN_DOUBLE_OUTPUT_CAP.get();
    }

    public static int getVeinFinderRadius() {

        if (!GTState.isReady()) {
            return 2500;
        }

        return ORE_VEIN_FINDER_RADIUS.get();
    }

    public static double getArgoAquaticSpawnChance() {
        return GTState.isReady() ? ARGO_AQUATIC_SPAWN_CHANCE.get() : 0.1D;
    }

    public static double getVanillaFishSpawnChance() {
        return GTState.isReady() ? VANILLA_FISH_SPAWN_CHANCE.get() : 0.1D;
    }

    /**
     * The spawn weight to use instead of the mod's own, or {@code -1} to leave it alone.
     *
     * <p>Negative means "do not override": the mod may ship a weight a pack wants to keep, and
     * silently replacing it would make its own config file lie. A pack opts in by setting a value.</p>
     */
    public static int getThaumaturgeSpawnWeightOverride() {
        if (!GTState.isReady()) {
            return -1;
        }

        return THAUMICCONCILIUM_THAUMATURGE_SPAWN_WEIGHT.get();
    }

    /** 核弹中心槽里外来铀物品按多少辐射量计；配置未就绪时用默认值。 */
    public static float getNukeForeignFuelRadioactivity() {
        if (!GTState.isReady()) {
            return 0.3F;
        }

        return MIO_ICIF_NUKE_FOREIGN_FUEL_RADIOACTIVITY.get().floatValue();
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
