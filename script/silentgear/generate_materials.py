import hashlib
import json
import math
import re
import zipfile
from io import BytesIO
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[2]
INPUT = ROOT / "script" / "materials" / "output" / "materials.json"
EXTRA_INPUT = ROOT / "游戏内的新材料.txt"
OUTPUT = ROOT / "src" / "main" / "resources" / "data" / "goldentweaks" / "silentgear_materials" / "compat"
OVERRIDE_OUTPUT = ROOT / "src" / "main" / "resources" / "data" / "goldentweaks" / "silentgear_materials"
LANG_DIR = ROOT / "src" / "main" / "resources" / "assets" / "goldentweaks" / "lang"
PRODUCTION_MODS = Path(r"E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods")
JAR_GLOB = "silent-gear-*.jar"

CATEGORY_ORDER = {"金属": 0, "宝石": 1, "木头": 2, "岩石": 3, "纤维": 4, "有机物": 5}
CATEGORY_BASE = {"木头": 0.22, "岩石": 0.30, "纤维": 0.12, "宝石": 0.52, "金属": 0.55, "有机物": 0.05}
CATEGORY_COLOR = {
    "木头": 0x8B6538,
    "岩石": 0x777777,
    "纤维": 0xDDD2B5,
    "宝石": 0x49C9D3,
    "金属": 0xB7BEC8,
    "有机物": 0x72A84A,
}

INVALID = {
    "armor", "amulet", "bath", "boots", "bucket", "cannon", "chest", "controller",
    "counter", "desc", "door", "engine", "fence", "generator", "helmet", "leggings",
    "machine", "millstone", "paxel", "projectile", "spawn_egg", "tank", "tray", "wheel",
}
PROCESSED = {"brick", "bricks", "button", "paver", "pavers", "pillar", "plate", "plates", "slab", "stairs", "tile", "tiles", "wall"}
POWER_WORDS = {
    "ancient": 0.12, "arcane": 0.16, "attuned": 0.12, "celestial": 0.30,
    "creation": 0.22, "diamond": 0.18, "dragon": 0.18, "eldritch": 0.34,
    "ender": 0.20, "energized": 0.18, "entropy": 0.20, "flawless": 0.18,
    "gravity": 0.24, "hallowed": 0.22, "iridium": 0.24, "knowledge": 0.20,
    "luminous": 0.18, "magic": 0.14, "mithril": 0.20, "netherite": 0.24,
    "overload": 0.26, "paltaeria": 0.30, "prismatic": 0.26, "purified": 0.12,
    "quantum": 0.34, "resonance": 0.18, "silverwood": 0.16, "solar": 0.18,
    "spectral": 0.24, "stellar": 0.34, "taint": 0.14, "tungsten": 0.18,
    "tyrian": 0.24, "void": 0.40, "warp": 0.20,
}
WEAK_WORDS = {"dead": -0.12, "dry": -0.08, "flawed": -0.08, "rotten": -0.10, "tiny": -0.08}
EXCEPTIONAL_WORDS = {
    "antimatter", "celestial", "creation", "creative", "deus", "eldritch", "exoversal", "flawless",
    "infinity", "singular", "ultimate", "prismatic", "quantum", "spectral", "stellar",
}
EXCLUDED_MATERIALS = {
    "focus_vis", "gravity", "purified_overload", "purified_quantum", "woodbasin",
    "yellow_chiseled_preservation_stone",
}
STRENGTH_OVERRIDES = {
    "paltaeria": 0.76,
    "void": 0.80,
    "void_stone": 0.78,
    "warped_unbalanced": 0.84,
    "warpwood": 0.58,
    "abyssal_spellweave_ingot": 0.82,
    "aether_vestiges": 0.75,
    "alternative_chromatic_compound": 0.75,
    "bedrock_dust": 0.75,
    "bismuth_crystal": 0.75,
    "crystal_matrix_ingot": 0.75,
    "endest_pearl": 0.75,
    "ichor": 0.75,
    "ichorium_ingot": 0.75,
    "quark_ingot": 1.0,
}
PROFILE_TARGET_ARMOR = {
    "abyssal_spellweave_ingot": 120.0,
    "pyro_spellweave_ingot": 88.0,
    "verdant_spellweave_ingot": 80.0,
    "void_spellweave_ingot": 105.0,
}


def metal_design(family, levels):
    return {
        resource_id: {"family": family, "level": level, "material": material}
        for level, entries in enumerate(levels, 1)
        for resource_id, material in entries
    }


METAL_DESIGN = {}
METAL_DESIGN.update(metal_design("hard_metal", [
    [("minecraft:copper_ingot", "copper"), ("createcardboardthings:cardboard_ingot", "cardboard_ingot"),
     ("mekanism:ingot_tin", "tin"), ("alltheores:zinc_ingot", "zinc_ingot"), ("mekanism:ingot_lead", "lead")],
    [("minecraft:iron_ingot", "iron"), ("extendedcrafting:black_iron_ingot", "black_iron_ingot"),
     ("alltheores:nickel_ingot", "nickel"), ("extendedcrafting:redstone_ingot", "redstone_ingot"),
     ("extendedcrafting:ender_ingot", "ender_ingot"), ("alltheores:aluminum_ingot", "aluminum"),
     ("neoecoae:tungsten_ingot", "tungsten")],
    [("alltheores:constantan_ingot", "constantan"), ("createbigcannons:cast_iron_ingot", "cast_iron"),
     ("alltheores:bronze_ingot", "bronze"), ("create:brass_ingot", "brass"),
     ("alltheores:invar_ingot", "invar"), ("thaumcraft:thaumium_ingot", "thaumium_ingot"),
     ("extendedae:entro_ingot", "entro_ingot")],
    [("mekanism:ingot_refined_glowstone", "refined_glowstone"), ("silentgear:crimson_iron_ingot", "crimson_iron"),
     ("mekanism:ingot_steel", "steel"), ("mekanism:ingot_osmium", "osmium"),
     ("irons_spellbooks:arcane_ingot", "arcane_ingot"), ("evolvedmekanism:ingot_refined_redstone", "ingot_refined_redstone"),
     ("taintedmagic:shadow_metal_ingot", "shadow_metal_ingot"), ("mekanism:ingot_uranium", "uranium"),
     ("alltheores:signalum_ingot", "signalum"), ("alltheores:lumium_ingot", "lumium"),
     ("neoecoae:aluminum_alloy_ingot", "aluminum_alloy_ingot"), ("neoecoae:black_tungsten_alloy_ingot", "black_tungsten_alloy_ingot")],
    [("hazennstuff:dreadsteel_ingot", "dreadsteel_ingot"), ("hazennstuff:chlorophyte_ingot", "chlorophyte_ingot"),
     ("mekanism_extras:ingot_naquadah", "ingot_naquadah"), ("irons_spellbooks:mithril_ingot", "mithril_ingot"),
     ("hazennstuff:demonite_ingot", "demonite_ingot"), ("iss_magicfromtheeast:refined_jade_ingot", "refined_jade_ingot"),
     ("alltheores:iridium_ingot", "iridium"), ("mekanism:ingot_refined_obsidian", "refined_obsidian"),
     ("hazennstuff:hallowed_ingot", "hallowed_ingot"), ("northstar:titanium_ingot", "titanium"),
     ("neoecoae:energized_superconductive_ingot", "energized_superconductive_ingot")],
    [("silentgear:crimson_steel_ingot", "crimson_steel"), ("northstar:martian_steel_ingot", "martian_steel_ingot"),
     ("cataclysm:cursium_ingot", "cursium_ingot"), ("cataclysm:black_steel_ingot", "black_steel_ingot"),
     ("cataclysm:ancient_metal_ingot", "ancient_metal_ingot"), ("evolvedmekanism:ingot_better_gold", "ingot_better_gold")],
    [("extendedcrafting:enhanced_ender_ingot", "enhanced_ender_ingot"), ("extendedcrafting:enhanced_redstone_ingot", "enhanced_redstone_ingot"),
     ("alltheores:enderium_ingot", "enderium"), ("hazennstuff:zenalite_ingot", "zenalite_ingot"),
     ("extendedcrafting:crystaltine_ingot", "crystaltine_ingot"), ("cataclysm:ignitium_ingot", "ignitium_ingot"),
     ("irons_spellbooks:pyrium_ingot", "pyrium_ingot"), ("evolvedmekanism:ingot_plaslitherite", "ingot_plaslitherite")],
    [("silentgear:tyrian_steel_ingot", "tyrian_steel"), ("hazennstuff:cosmic_gold_ingot", "cosmic_gold_ingot"),
     ("cataclysm:witherite_ingot", "witherite_ingot"), ("avaritia:crystal_matrix_ingot", "crystal_matrix_ingot")],
]))
METAL_DESIGN.update(metal_design("super_metal", [
    [("traveloptics:pyro_spellweave_ingot", "pyro_spellweave_ingot"), ("traveloptics:void_spellweave_ingot", "void_spellweave_ingot"),
     ("traveloptics:verdant_spellweave_ingot", "verdant_spellweave_ingot"), ("traveloptics:abyssal_spellweave_ingot", "abyssal_spellweave_ingot")],
    [("traveloptics:crimson_spellweave_ingot", "crimson_spellweave_ingot"), ("traveloptics:lightning_spellweave_ingot", "lightning_spellweave_ingot"),
     ("traveloptics:cryo_spellweave_ingot", "cryo_spellweave_ingot")],
    [("traveloptics:eldritch_spellweave_ingot", "eldritch_spellweave_ingot"), ("traveloptics:evokated_spellweave_ingot", "evokated_spellweave_ingot")],
    [("traveloptics:celestial_spellweave_ingot", "celestial_spellweave_ingot")],
    [("extendedcrafting:the_ultimate_ingot", "the_ultimate_ingot")],
]))
METAL_DESIGN.update(metal_design("god_metal", [
    [("avaritia:neutron_ingot", "neutron_ingot")], [("avaritia:infinity_ingot", "infinity_ingot")],
    [("avaritia_more_items:quark_ingot", "quark_ingot")], [("avaritia_more_items:cosmic_ingot", "cosmic_ingot")],
]))
METAL_DESIGN.update(metal_design("soft_metal", [
    [("minecraft:gold_ingot", "gold"), ("alltheores:silver_ingot", "silver"),
     ("alltheores:electrum_ingot", "electrum"), ("silentgear:blaze_gold_ingot", "blaze_gold")],
    [("silentgear:azure_silver_ingot", "azure_silver"), ("hazennstuff:rose_gold_ingot", "rose_gold_ingot"),
     ("alltheores:platinum_ingot", "platinum")],
    [("silentgear:azure_electrum_ingot", "azure_electrum"), ("thaumcraft:void_metal_ingot", "void_metal_ingot")],
    [("forbiddenmagic:hexite_ingot", "hexite_ingot")],
]))
COATING_DESIGN = metal_design("metal_coating", [
    [("minecraft:gold_ingot", "gold"), ("silentgear:blaze_gold_ingot", "blaze_gold")],
    [("minecraft:netherite_ingot", "netherite"), ("createbigcannons:nethersteel_ingot", "nethersteel_ingot"),
     ("createvoidway:void_steel_ingot", "void_steel_ingot")],
    [("igleelib:modium_ingot", "modium_ingot"), ("igleelib:lavium_ingot", "lavium_ingot"),
     ("igleelib:blazum_ingot", "blazum_ingot"), ("igleelib:derium_ingot", "derium_ingot")],
])
for resource_id, design in COATING_DESIGN.items():
    METAL_DESIGN.setdefault(resource_id, design)["coating_level"] = design["level"]

# Semantic position inside a level. -1 leans toward the previous level's center,
# +1 leans toward the next level's center. The generated value never crosses either center.
METAL_SEMANTIC_BIAS = {
    "minecraft:copper_ingot": -0.10, "createcardboardthings:cardboard_ingot": -0.75,
    "mekanism:ingot_tin": -0.35, "alltheores:zinc_ingot": 0.10, "mekanism:ingot_lead": 0.25,
    "minecraft:iron_ingot": 0.00, "extendedcrafting:black_iron_ingot": 0.35,
    "alltheores:nickel_ingot": 0.15, "extendedcrafting:redstone_ingot": -0.35,
    "extendedcrafting:ender_ingot": 0.30, "alltheores:aluminum_ingot": -0.45,
    "neoecoae:tungsten_ingot": 0.70, "alltheores:constantan_ingot": 0.05,
    "createbigcannons:cast_iron_ingot": -0.20, "alltheores:bronze_ingot": 0.00,
    "create:brass_ingot": -0.35, "alltheores:invar_ingot": 0.30,
    "thaumcraft:thaumium_ingot": 0.55, "extendedae:entro_ingot": 0.65,
    "mekanism:ingot_refined_glowstone": -0.20, "silentgear:crimson_iron_ingot": 0.15,
    "mekanism:ingot_steel": 0.05, "mekanism:ingot_osmium": 0.30,
    "irons_spellbooks:arcane_ingot": 0.45, "evolvedmekanism:ingot_refined_redstone": -0.10,
    "taintedmagic:shadow_metal_ingot": 0.35, "mekanism:ingot_uranium": 0.40,
    "alltheores:signalum_ingot": 0.15, "alltheores:lumium_ingot": 0.10,
    "neoecoae:aluminum_alloy_ingot": -0.05, "neoecoae:black_tungsten_alloy_ingot": 0.70,
    "hazennstuff:dreadsteel_ingot": 0.20, "hazennstuff:chlorophyte_ingot": -0.15,
    "mekanism_extras:ingot_naquadah": 0.45, "irons_spellbooks:mithril_ingot": 0.35,
    "hazennstuff:demonite_ingot": 0.20, "iss_magicfromtheeast:refined_jade_ingot": -0.10,
    "alltheores:iridium_ingot": 0.60, "mekanism:ingot_refined_obsidian": 0.55,
    "hazennstuff:hallowed_ingot": 0.40, "northstar:titanium_ingot": 0.25,
    "neoecoae:energized_superconductive_ingot": 0.70,
    "silentgear:crimson_steel_ingot": -0.05, "northstar:martian_steel_ingot": 0.05,
    "cataclysm:cursium_ingot": 0.35, "cataclysm:black_steel_ingot": 0.20,
    "cataclysm:ancient_metal_ingot": 0.50, "evolvedmekanism:ingot_better_gold": -0.20,
    "extendedcrafting:enhanced_ender_ingot": 0.25, "extendedcrafting:enhanced_redstone_ingot": -0.20,
    "alltheores:enderium_ingot": 0.40, "hazennstuff:zenalite_ingot": 0.30,
    "extendedcrafting:crystaltine_ingot": 0.45, "cataclysm:ignitium_ingot": 0.65,
    "irons_spellbooks:pyrium_ingot": 0.50, "evolvedmekanism:ingot_plaslitherite": 0.70,
    "silentgear:tyrian_steel_ingot": -0.10, "hazennstuff:cosmic_gold_ingot": 0.45,
    "cataclysm:witherite_ingot": 0.30, "avaritia:crystal_matrix_ingot": 0.70,
    "traveloptics:pyro_spellweave_ingot": 0.10, "traveloptics:void_spellweave_ingot": 0.35,
    "traveloptics:verdant_spellweave_ingot": -0.25, "traveloptics:abyssal_spellweave_ingot": 0.55,
    "traveloptics:crimson_spellweave_ingot": 0.10, "traveloptics:lightning_spellweave_ingot": 0.45,
    "traveloptics:cryo_spellweave_ingot": -0.20, "traveloptics:eldritch_spellweave_ingot": 0.55,
    "traveloptics:evokated_spellweave_ingot": 0.15, "traveloptics:celestial_spellweave_ingot": 0.60,
    "silentgear:azure_silver_ingot": -0.30, "hazennstuff:rose_gold_ingot": -0.10,
    "alltheores:platinum_ingot": 0.55, "silentgear:azure_electrum_ingot": -0.20,
    "thaumcraft:void_metal_ingot": 0.60,
}
COATING_SEMANTIC_BIAS = {
    "minecraft:netherite_ingot": 0.15, "createbigcannons:nethersteel_ingot": -0.25,
    "createvoidway:void_steel_ingot": 0.55, "igleelib:modium_ingot": -0.35,
    "igleelib:lavium_ingot": 0.10, "igleelib:blazum_ingot": 0.35, "igleelib:derium_ingot": 0.65,
}

BUILTIN_PATHS = {
    "minecraft:copper_ingot": "copper", "mekanism:ingot_tin": "tin", "mekanism:ingot_lead": "lead",
    "minecraft:iron_ingot": "iron", "alltheores:nickel_ingot": "nickel", "alltheores:aluminum_ingot": "aluminum",
    "alltheores:bronze_ingot": "bronze", "create:brass_ingot": "brass", "alltheores:invar_ingot": "invar",
    "mekanism:ingot_refined_glowstone": "refined_glowstone", "silentgear:crimson_iron_ingot": "crimson_iron",
    "mekanism:ingot_steel": "steel", "mekanism:ingot_osmium": "osmium", "mekanism:ingot_uranium": "uranium",
    "alltheores:signalum_ingot": "signalum", "alltheores:lumium_ingot": "lumium",
    "mekanism:ingot_refined_obsidian": "refined_obsidian", "alltheores:iridium_ingot": "iridium",
    "northstar:titanium_ingot": "titanium", "silentgear:crimson_steel_ingot": "crimson_steel",
    "alltheores:enderium_ingot": "enderium", "silentgear:tyrian_steel_ingot": "tyrian_steel",
    "minecraft:gold_ingot": "gold", "alltheores:silver_ingot": "silver", "alltheores:electrum_ingot": "electrum",
    "silentgear:blaze_gold_ingot": "blaze_gold", "silentgear:azure_silver_ingot": "azure_silver",
    "alltheores:platinum_ingot": "platinum", "silentgear:azure_electrum_ingot": "azure_electrum",
    "minecraft:netherite_ingot": "netherite",
}
PRIMAL_SHARDS = {"air", "earth", "entropy", "fire", "order", "water"}
SINS = {"envy", "gluttony", "greed", "lust", "pride", "sloth", "wrath"}
ATTRIBUTE_MULTIPLIERS = {
    "air": 0.75,
    "balanced": 1.25,
    "blaze": 1.25,
    "blaze_cube": 10.0,
    "constantan": 1.25,
    "creation": 0.85,
    "aluminum_alloy_ingot": 3.0,
    "black_iron_ingot": 1.5,
    "black_steel_ingot": 2.0,
    "black_tungsten_alloy_ingot": 2.0,
    "blazum_ingot": 2.0,
    "chlorophyte_ingot": 2.2,
    "compressed_solidified_flux_experience": 2.0,
    "cryotheum": 1.5,
    "crystal_ingot": 4.0,
    "crystal_matrix_ingot": 10.0,
    "crystaltine_ingot": 4.0,
    "cursium_ingot": 6.0,
    "demonite_ingot": 2.0,
    "derium_ingot": 2.0,
    "deus_essence": 0.5,
    "downstone_fragments": 1.5,
    "dreadsteel_ingot": 2.0,
    "echo_shard": 3.0,
    "ender_ingot": 0.35,
    "ender_pearl": 0.35,
    "endest_pearl": 5.0,
    "energized_fluix": 1.25,
    "energized_superconductive_ingot": 2.0,
    "entro": 1.25,
    "entro_ingot": 2.0,
    "glowing_ancient_stone": 2.0,
    "malachite": 4.0,
    "ignitium_ingot": 10.0,
    "infinity_ingot": 2.0,
    "ingot_better_gold": 4.0,
    "ingot_naquadah": 2.0,
    "ingot_plaslitherite": 8.0,
    "ichor": 10.0,
    "ichorcloth": 10.0,
    "ichorium_ingot": 10.0,
    "ingot_refined_redstone": 0.5,
    "lavium_ingot": 2.0,
    "meteorite_fragment": 1.0,
    "midnight_chip": 2.0,
    "modium_ingot": 2.0,
    "moonstone": 2.0,
    "neolith": 6.0,
    "nether_star": 3.0,
    "neutron_ingot": 25.0,
    "onyx": 4.0,
    "pellet_antimatter": 0.8,
    "pellet_plutonium": 4.0,
    "pellet_polonium": 4.0,
    "permafrost": 3.0,
    "polished_rose_quartz": 2.0,
    "prismarine_shard": 2.0,
    "pure_azurite": 6.0,
    "pure_bloodstone": 6.0,
    "pure_certus_quartz": 3.0,
    "pure_coal": 3.0,
    "pure_copper": 3.0,
    "pure_diamond": 3.0,
    "pure_echo": 3.0,
    "pure_emerald": 3.0,
    "pure_fluix": 3.0,
    "pure_glowstone": 3.0,
    "pure_gold": 3.0,
    "pure_iron": 3.0,
    "pure_lapis": 3.0,
    "pure_malachite": 6.0,
    "pure_netherite_scrap": 3.0,
    "pure_prismarine": 3.0,
    "pure_quartz": 3.0,
    "pure_redstone": 3.0,
    "pure_zinc": 3.0,
    "pyrium_ingot": 10.0,
    "purified_entro": 2.0,
    "purified_irradiated": 2.0,
    "purified_resonating": 3.0,
    "shard": 1.5,
    "shimmerstone": 1.5,
    "red_shaft": 1.0,
    "red_string": 0.5,
    "shadow_metal_ingot": 0.75,
    "spectral": 2.0,
    "storm_stone": 4.0,
    "stratine": 2.3,
    "taint": 0.5,
    "tainted_unbalanced": 0.5,
    "the_ultimate_ingot": 2.5,
    "solar_core": 0.7,
    "star_fuel": 10.0,
    "substrate": 0.25,
    "tungsten": 0.7,
    "uu_matter": 5.0,
    "empty_crystal": 0.7,
    "scrap_box": 1.5,
    "vegetal": 0.8,
    "void": 0.7,
    "void_stone": 0.5,
    "warped_unbalanced": 0.35,
    "cosmic_gold_ingot": 6.0,
    "witherite_ingot": 10.0,
    "zenalite_ingot": 4.5,
    "canvas": 0.25,
    "bedrock_dust": 1.5,
    "bismuth_crystal": 2.0,
    "lacrima": 2.0,
}
PROFILE_OVERRIDES = {
    "cast_iron": ("iron", 1.1),
    "coiler": ("crimson_steel", 1.0),
    "fluix": ("iron", 1.1),
    "weeping_gala": ("crimson_steel", 1.0),
}
MATERIAL_ALIASES = {}
ANNOTATION_EXCLUDES = {
    "air_infused_stone", "amber_bearing_stone", "attuned", "boil_stone", "butter_flour_sand",
    "casting_sand", "celestial", "clay", "crumbling_attuned", "dead_king_phylactery", "dormant",
    "dream_cat", "eldritch", "flowing_liquid", "garnet", "glyph_stone", "knowledge", "liquid",
    "mold", "mortar_stone", "paving_stone_travel", "paving_stone_warding", "peridot", "prismatic",
    "red_sand", "redstone_sand", "rock", "ruby", "sand_paper", "sapphire", "seatwood",
    "shriving_stone", "sky", "stone_lion", "stone_shimmerstone_light", "stone_shingles",
    "stuck_storm_stone", "suppression_stone", "suspicious_sand", "tuff", "unprocessed_echo",
    "venus_stone_bull_spawn_egg", "warded_stone", "warp_stone", "zenalite_stone",
    "bean", "carrot", "chip_wood", "coral", "honey", "lapis", "malachite", "polished_amethyst",
    "rope", "sag", "titanium_ingot",
}
EXTRA_CATEGORY_OVERRIDES = {
    "aether_vestiges": "宝石",
    "alternative_chromatic_compound": "岩石",
    "bedrock_dust": "岩石",
    "bismuth_crystal": "宝石",
    "crystalline": "宝石",
    "crystallized_soul": "宝石",
    "deus_essence": "宝石",
    "downstone_fragments": "宝石",
    "echo_shard": "宝石",
    "endest_pearl": "宝石",
    "ichor": "宝石",
    "midnight_chip": "宝石",
    "nether_star": "宝石",
    "uu_matter": "宝石",
    "compressed_solidified_flux_experience": "宝石",
    "lacrima": "宝石",
    "null_block": "岩石",
    "null_end_stone": "岩石",
    "null_leaves": "有机物",
    "pellet_antimatter": "宝石",
    "solar_core": "宝石",
    "sodium_catalyst": "宝石",
    "warden_tendril": "有机物",
    "bramble": "有机物",
    "cattail": "有机物",
    "fireblossom": "有机物",
    "mars_palm": "有机物",
    "mars_sprout": "有机物",
    "mars_tulip": "有机物",
    "resonance": "有机物",
    "vegetal": "有机物",
    "jingxu_youlan": "有机物",
    "scarlet_zhuhua": "有机物",
    "yue_linglan": "有机物",
    "abyssal_spellweave_ingot": "金属",
    "pyro_spellweave_ingot": "金属",
    "verdant_spellweave_ingot": "金属",
    "void_spellweave_ingot": "金属",
}
COATING_MATERIALS = {
    "honey_butter", "super_butter", "blazum_ingot", "derium_ingot", "lavium_ingot",
    "modium_ingot", "pure_netherite_scrap", "ichor",
}
ROD_MATERIALS = {"red_shaft", "wither_rib", "hdpe_stick"}
CORD_MATERIALS = {"red_string"}
GEM_OVERRIDES = {
    "blaze_cube", "star_fuel", "paltaeria", "pure_azurite", "pure_bloodstone", "pure_certus_quartz", "pure_coal",
    "pure_diamond", "pure_echo", "pure_emerald", "pure_fluix", "pure_glowstone", "pure_lapis",
    "pure_malachite", "pure_prismarine", "pure_quartz", "pure_redstone", "shimmerstone",
    "stratine", "air", "earth", "entropy", "fire", "order", "water", "meteorite_fragment",
    "smoky_quartz", "crystalline",
}
MEKANISM_ALLOY_ORDER = [
    "alloy_infused", "alloy_reinforced", "alloy_atomic", "alloy_hypercharged", "alloy_radiance",
    "alloy_subatomic", "alloy_thermonuclear", "alloy_singular", "alloy_shining", "alloy_exoversal",
    "alloy_spectrum",
]
FOOD_WORDS = {"apple", "banana", "barley", "bean", "beef", "berry", "cabbage", "carrot", "chicken", "corn", "egg", "fish", "fruit", "melon", "mutton", "onion", "pork", "potato", "pumpkin", "rice", "tomato", "wheat"}
EXTRA_FIBER_WORDS = {"cloth", "cobweb", "hair", "hide", "reed", "straw", "tendons", "vellum", "vine", "weave", "webbing"}
EXTRA_ORGANIC_WORDS = {"bark", "bone", "bones", "butter", "coral", "fang", "feather", "flower", "mushroom", "petals", "scute", "shell", "skin", "slime", "spine"}
EXTRA_ROCK_WORDS = {"brick", "dust", "neolith", "prismarine", "rock"}

SPECIAL_TRAITS = {
    "aquatic": "aquatic", "blaze": "fiery", "brimstone": "fiery", "celestial": "stellar",
    "chill": "chilled", "crimson": "fiery", "eldritch": "void_ward", "ender": "terminus",
    "fire": "fiery", "frost": "chilled", "glow": "brilliant", "gravity": "floatstoner",
    "hell": "fireproof", "honey": "yummy", "ice": "chilled", "lightning": "crackler",
    "luminous": "brilliant", "magic": "lustrous", "moon": "moonwalker", "nether": "fireproof",
    "obsidian": "fireproof", "permafrost": "chilled", "prismatic": "refractive",
    "quartz": "refractive", "silverwood": "renew", "solar": "fiery", "spectral": "stellar",
    "taint": "venom", "venom": "venom", "void": "void_ward", "warp": "cursed",
}


def tokens(value):
    return set(re.findall(r"[a-z0-9]+", value.lower()))


class AssetResolver:
    def __init__(self):
        self.archives = []
        self.color_hits = 0
        self.color_fallbacks = 0
        jar_paths = sorted(PRODUCTION_MODS.glob("*.jar"))
        jar_paths += sorted((ROOT / "libs" / "implementation").glob("*.jar"))
        jar_paths += sorted((ROOT / "libs" / "compileOnly").glob("*.jar"))
        for path in jar_paths:
            try:
                self.archives.append(zipfile.ZipFile(path))
            except zipfile.BadZipFile:
                pass
        self.cache = {}
        self.lang_cache = {}

    def close(self):
        for archive in self.archives:
            archive.close()

    def read(self, path):
        if path in self.cache:
            return self.cache[path]
        local = ROOT / "src" / "main" / "resources" / path
        if local.is_file():
            result = local.read_bytes()
        else:
            result = None
            for archive in self.archives:
                try:
                    result = archive.read(path)
                    break
                except KeyError:
                    continue
        self.cache[path] = result
        return result

    @staticmethod
    def location(value, default_namespace):
        if ":" in value:
            return value.split(":", 1)
        return default_namespace, value

    def model_textures(self, namespace, model_path, seen=None):
        seen = set() if seen is None else seen
        key = f"{namespace}:{model_path}"
        if key in seen:
            return {}
        seen.add(key)
        raw = self.read(f"assets/{namespace}/models/{model_path}.json")
        if raw is None:
            return {}
        try:
            model = json.loads(raw)
        except (UnicodeDecodeError, json.JSONDecodeError):
            return {}
        textures = {}
        parent = model.get("parent")
        if isinstance(parent, str) and not parent.startswith("builtin/"):
            parent_namespace, parent_path = self.location(parent, namespace)
            textures.update(self.model_textures(parent_namespace, parent_path, seen))
        textures.update({key: value for key, value in model.get("textures", {}).items() if isinstance(value, str)})
        return textures

    def item_texture_bytes(self, resource_id):
        namespace, path = resource_id.split(":", 1)
        textures = self.model_textures(namespace, f"item/{path}")
        resolved = []
        for key, value in textures.items():
            if key in {"halo", "particle"}:
                continue
            visited = set()
            while value.startswith("#") and value[1:] not in visited:
                visited.add(value[1:])
                value = textures.get(value[1:], "")
            if not value:
                continue
            texture_namespace, texture_path = self.location(value, namespace)
            raw = self.read(f"assets/{texture_namespace}/textures/{texture_path}.png")
            if raw is not None:
                resolved.append(raw)
        return resolved

    def lang(self, namespace, locale):
        key = (namespace, locale)
        if key not in self.lang_cache:
            raw = self.read(f"assets/{namespace}/lang/{locale}.json")
            try:
                self.lang_cache[key] = json.loads(raw) if raw is not None else {}
            except (UnicodeDecodeError, json.JSONDecodeError):
                self.lang_cache[key] = {}
        return self.lang_cache[key]

    def item_name(self, resource_id, locale):
        namespace, path = resource_id.split(":", 1)
        language = self.lang(namespace, locale)
        for key in (f"item.{namespace}.{path}", f"block.{namespace}.{path}"):
            value = language.get(key)
            if isinstance(value, str) and value:
                return re.sub(r"§.", "", value)
        return None


def find_jar():
    jars = sorted((ROOT / "libs" / "compileOnly").glob(JAR_GLOB))
    if len(jars) != 1:
        raise RuntimeError(f"Expected one {JAR_GLOB}, found {len(jars)}")
    return jars[0]


def builtin_materials_and_bounds(jar):
    material_names = set()
    values = {}
    profiles = {}
    material_data = {}
    with zipfile.ZipFile(jar) as archive:
        paths = [name for name in archive.namelist() if name.startswith("data/silentgear/silentgear_materials/") and name.endswith(".json")]
        for path in paths:
            relative = path.removeprefix("data/silentgear/silentgear_materials/").removesuffix(".json")
            material_names.update((relative, relative.rsplit("/", 1)[-1]))
            data = json.loads(archive.read(path))
            material_data[relative] = data
            properties = data.get("properties", {}).get("silentgear:main", {})
            profiles[relative.rsplit("/", 1)[-1]] = properties
            for key, value in properties.items():
                if key not in {"traits", "harvest_tier", "additive"} and isinstance(value, (int, float)) and value > 0:
                    values.setdefault(key, []).append(value)
    bounds = {key: (min(nums) * 0.5, max(nums) * 10.0) for key, nums in values.items() if nums}
    return material_names, bounds, profiles, material_data


def existing_materials():
    result = set()
    data_root = ROOT / "src" / "main" / "resources" / "data"
    for path in data_root.glob("*/silentgear_materials/**/*.json"):
        if OUTPUT in path.parents:
            continue
        namespace = path.relative_to(data_root).parts[0]
        relative = path.relative_to(data_root / namespace / "silentgear_materials").with_suffix("").as_posix()
        result.add(f"{namespace}:{relative}")
        result.add(relative.rsplit("/", 1)[-1])
    return result


def extra_entries():
    if not EXTRA_INPUT.exists():
        return []
    text = EXTRA_INPUT.read_text(encoding="utf-8")
    resource_ids = dict.fromkeys(re.findall(r'\bid: "([a-z0-9_.-]+:[a-z0-9_./-]+)"', text))
    resource_ids.pop("sophisticatedstorage:barrel", None)
    entries = []
    for resource_id in resource_ids:
        path = resource_id.split(":", 1)[1]
        word_set = tokens(path)
        if path in GEM_OVERRIDES:
            category = "宝石"
        elif path in EXTRA_CATEGORY_OVERRIDES:
            category = EXTRA_CATEGORY_OVERRIDES[path]
        elif word_set & EXTRA_FIBER_WORDS or any(word in path for word in EXTRA_FIBER_WORDS):
            category = "纤维"
        elif word_set & EXTRA_ORGANIC_WORDS or any(word in path for word in EXTRA_ORGANIC_WORDS):
            category = "有机物"
        elif word_set & EXTRA_ROCK_WORDS or any(word in path for word in EXTRA_ROCK_WORDS):
            category = "岩石"
        elif any(word in path for word in ("crystal", "gem", "pearl", "quartz", "shard")):
            category = "宝石"
        else:
            category = "金属"
        entries.append((category, {"id": resource_id, "material": path, "name": path.replace("_", " ").title(), "extra": True}))
    return entries


def valid(category, entry):
    namespace = entry["id"].split(":", 1)[0]
    path = entry["id"].split(":", 1)[1]
    word_set = tokens(path)
    material = entry["material"]
    if "familiar" in namespace or material in EXCLUDED_MATERIALS or material in ANNOTATION_EXCLUDES:
        return False
    if entry.get("extra"):
        return True
    if namespace == "apotheosis" or "preservation_stone" in material or "infused_stone" in material or "golem" in material:
        return False
    if category == "有机物" and any(word in path for word in ("crop", "seed", "sapling")):
        return False
    if word_set & INVALID or "." in path:
        return False
    if category == "木头" and word_set & {"basin", "beam", "block", "counter", "knot", "nailed", "tray"}:
        return False
    if category == "岩石" and word_set & PROCESSED:
        return False
    if category == "宝石" and word_set & {"budding", "dye", "bucket", "empty", "mob"}:
        return False
    if category == "有机物" and word_set & {"potted", "sequenced", "stew", "salad", "wrap", "roll", "milk"}:
        return False
    return True


def strength(category, material, resource_id):
    if material in PRIMAL_SHARDS or material in SINS:
        return 0.55
    if material in STRENGTH_OVERRIDES:
        return STRENGTH_OVERRIDES[material]
    text = f"{material}_{resource_id}".lower()
    score = CATEGORY_BASE[category]
    score += sum(value for word, value in POWER_WORDS.items() if word in text)
    score += sum(value for word, value in WEAK_WORDS.items() if word in text)
    if category == "有机物" and tokens(text) & FOOD_WORDS:
        score -= 0.03
    random_value = int(hashlib.sha256(text.encode()).hexdigest()[:4], 16) / 65535
    score += random_value * 0.06 - 0.03
    if any(word in text for word in EXCEPTIONAL_WORDS):
        return max(0.91, min(1.0, max(score, 0.91 + random_value * 0.09)))
    return max(0.0, min(0.9, score))


def scale(bounds, key, tier, digits=3):
    low, high = bounds[key]
    normal_high = high / 10.0 * 1.5
    if tier <= 0.9:
        normal_tier = tier / 0.9
        value = low * math.pow(normal_high / low, normal_tier)
    else:
        exceptional_tier = (tier - 0.9) / 0.1
        value = normal_high * math.pow(high / normal_high, exceptional_tier)
    return round(value, digits)


def harvest_tier(tier):
    if tier < 0.18:
        return "wood", "0", "minecraft:incorrect_for_wooden_tool"
    if tier < 0.38:
        return "stone", "1", "minecraft:incorrect_for_stone_tool"
    if tier < 0.62:
        return "iron", "2", "minecraft:incorrect_for_iron_tool"
    if tier < 0.86:
        return "diamond", "3", "minecraft:incorrect_for_diamond_tool"
    return "netherite", "4", "minecraft:incorrect_for_netherite_tool"


def trait(name, level):
    return {"conditions": [], "level": level, "trait": f"silentgear:{name}"}


def traits_for(category, material, resource_id, tier):
    level = max(1, min(5, 1 + round(tier * 4)))
    defaults = {
        "木头": ["organic", "flexible", "flammable"],
        "岩石": ["hard", "heavy"],
        "纤维": ["flexible", "soft"],
        "宝石": ["brittle", "lustrous"],
        "金属": ["malleable", "hard"],
        "有机物": ["organic", "soft"],
    }[category]
    text = f"{material}_{resource_id}".lower()
    selected = []
    if material.endswith("spellweave_ingot"):
        selected.append("soft")
    for word, special in SPECIAL_TRAITS.items():
        if word in text and special not in selected:
            selected.append(special)
    if category == "纤维" and any(word in text for word in ("silk", "fabric", "cloth")):
        selected.insert(0, "silky")
    if category == "有机物" and tokens(text) & FOOD_WORDS:
        selected.insert(0, "yummy")
    for default in defaults:
        if default not in selected:
            selected.append(default)
    return [trait(name, 1 if name in {"fiery", "fireproof", "floatstoner", "magmatic", "silky", "yummy"} else level) for name in selected[:3]]


def texture_color(resolver, resource_id):
    weighted = [0.0, 0.0, 0.0]
    total = 0.0
    for raw in resolver.item_texture_bytes(resource_id):
        try:
            image = Image.open(BytesIO(raw)).convert("RGBA")
        except (OSError, ValueError):
            continue
        for red, green, blue, alpha in image.get_flattened_data():
            if alpha < 24:
                continue
            brightness = (red + green + blue) / 3
            if brightness < 12 or brightness > 248:
                continue
            saturation = max(red, green, blue) - min(red, green, blue)
            weight = alpha / 255 * (1.0 + saturation / 255)
            for index, channel in enumerate((red, green, blue)):
                linear = (channel / 255) ** 2.2
                weighted[index] += linear * weight
            total += weight
    if total == 0:
        return None
    channels = [round((value / total) ** (1 / 2.2) * 255) for value in weighted]
    return f"#FF{channels[0]:02X}{channels[1]:02X}{channels[2]:02X}"


def color_for(category, material, resource_id, resolver):
    extracted = texture_color(resolver, resource_id)
    if extracted is not None:
        resolver.color_hits += 1
        return extracted
    resolver.color_fallbacks += 1
    base = CATEGORY_COLOR[category]
    digest = hashlib.sha256(material.encode()).digest()
    channels = [(base >> shift) & 0xFF for shift in (16, 8, 0)]
    channels = [max(24, min(240, channel + digest[index] % 41 - 20)) for index, channel in enumerate(channels)]
    return f"#FF{channels[0]:02X}{channels[1]:02X}{channels[2]:02X}"


ZH_TERMS = {
    "alloy": "合金", "ancient": "远古", "arcane": "奥术", "armadillo": "犰狳", "astral": "星界", "atomic": "原子",
    "azure": "蔚蓝", "black": "黑色", "blaze": "烈焰", "bloodstone": "血石", "bone": "骨",
    "ball": "球", "block": "块", "bramble": "荆棘", "cactus": "仙人掌", "celestial": "天界", "charged": "充能", "cloth": "布料", "coal": "煤",
    "compressed": "压缩", "copper": "铜", "coral": "珊瑚", "cosmic": "宇宙", "creative": "创造",
    "crimson": "绯红", "crystal": "水晶", "crystalline": "晶化物", "cypress": "柏树", "dark": "暗色", "diamond": "钻石",
    "divine": "神圣", "dragon": "龙", "dust": "粉末", "echo": "回响", "elder": "远古", "empyreal": "天穹",
    "emerald": "绿宝石", "ender": "末影", "energized": "充能", "essence": "精华", "exoversal": "超界",
    "fabric": "织物", "feather": "羽毛", "fire": "火焰", "flamebearer": "炎之承载者", "fluix": "福鲁伊克斯",
    "frosted": "霜冻", "glowstone": "荧石", "gold": "金", "hallowed": "神圣", "hide": "皮革",
    "hypercharged": "超频", "ichor": "灵液", "ichorium": "灵液金属", "ignitium": "炽炎铁",
    "infused": "灌注", "ingot": "锭", "iron": "铁", "jadeite": "硬玉", "lapis": "青金石",
    "leaves": "树叶", "lunar": "月球", "magic": "魔法", "malachite": "孔雀石", "maple": "枫木", "matrix": "矩阵", "meteorite": "陨石",
    "midnight": "午夜", "mossy": "覆苔", "nether": "下界", "neutron": "中子", "null": "虚无", "orange": "橙色", "overgrown": "繁茂", "pearl": "珍珠", "pine": "松木",
    "pellet": "颗粒", "permafrost": "永冻", "plutonium": "钚", "polonium": "钋", "prismarine": "海晶",
    "pure": "纯净", "pyrium": "炽焰金属", "quark": "夸克", "quartz": "石英", "rabbit": "兔子", "red": "红色", "redstone": "红石",
    "reinforced": "强化", "resonance": "共振", "sapphire": "蓝宝石", "scale": "鳞片", "shadow": "暗影",
    "sand": "沙", "scute": "鳞甲", "shard": "碎片", "shell": "外壳", "shining": "闪耀", "shulker": "潜影贝", "silver": "银", "singular": "奇点", "slime": "史莱姆", "snowblossom": "雪花木",
    "solar": "太阳", "soul": "灵魂", "spectrum": "光谱", "spellweave": "法术织物", "star": "星辰", "steel": "钢",
    "stone": "石", "storm": "风暴", "string": "线", "subatomic": "亚原子", "superconductive": "超导",
    "tendril": "触须", "thermonuclear": "热核", "titanium": "钛", "tungsten": "钨", "ultimate": "终极",
    "vegetal": "植物质", "verdant": "翠绿", "void": "虚空", "warden": "监守者", "weave": "织物", "wither": "凋灵", "yellow": "黄色",
    "witherite": "凋灵合金", "zinc": "锌",
}
ZH_OVERRIDES = {
    "biomesoplenty:null_end_stone": "虚无末地石",
    "minecraft:brick": "红砖",
    "minecraft:nether_brick": "下界砖",
    "minecraft:nether_star": "下界之星",
    "mekmm:uu_matter": "UU物质",
}


def generated_translation_key(entry):
    namespace, _ = entry["id"].split(":", 1)
    return f"material.goldentweaks.compat.{namespace}.{entry['material']}"


def fallback_zh(name):
    words = name.lower().replace("-", "_").split("_")
    translated = [ZH_TERMS.get(word, word.title()) for word in words]
    return "".join(translated)


def write_generated_lang(entries, resolver):
    generated = {"en_us": {}, "zh_cn": {}}
    for entry in entries:
        key = generated_translation_key(entry)
        generated["en_us"][key] = resolver.item_name(entry["id"], "en_us") or entry["name"]
        generated["zh_cn"][key] = ZH_OVERRIDES.get(entry["id"]) or resolver.item_name(entry["id"], "zh_cn") or fallback_zh(entry["material"])
    LANG_DIR.mkdir(parents=True, exist_ok=True)
    for locale, values in generated.items():
        path = LANG_DIR / f"{locale}.json"
        current = json.loads(path.read_text(encoding="utf-8")) if path.exists() else {}
        current = {key: value for key, value in current.items() if not key.startswith("material.goldentweaks.compat.")}
        current.update(values)
        path.write_text(json.dumps(dict(sorted(current.items())), ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return generated


def main_properties(bounds, tier, traits):
    tier_name, level, incorrect = harvest_tier(tier)
    armor = scale(bounds, "armor", tier)
    return {
        "armor": armor,
        "armor/boots": round(armor * 0.15, 3),
        "armor/chestplate": round(armor * 0.4, 3),
        "armor/helmet": round(armor * 0.15, 3),
        "armor/leggings": round(armor * 0.3, 3),
        "armor_durability": scale(bounds, "armor_durability", tier),
        "armor_toughness": scale(bounds, "armor_toughness", tier),
        "attack_damage": scale(bounds, "attack_damage", tier),
        "charging_value": round(0.45 + tier * 1.05, 3),
        "durability": scale(bounds, "durability", tier),
        "enchantment_value": scale(bounds, "enchantment_value", tier),
        "harvest_speed": scale(bounds, "harvest_speed", tier),
        "harvest_tier": {"incorrect_blocks_for_tool": incorrect, "level_hint": level, "name": tier_name},
        "magic_armor": scale(bounds, "magic_armor", tier),
        "magic_damage": scale(bounds, "magic_damage", tier),
        "projectile_accuracy": round(0.75 + tier * 0.75, 3),
        "projectile_speed": round(0.7 + tier * 1.3, 3),
        "ranged_damage": scale(bounds, "ranged_damage", tier),
        "rarity": scale(bounds, "rarity", tier),
        "traits": traits,
    }


def multiply_numbers(value, factor):
    if isinstance(value, bool):
        return value
    if isinstance(value, (int, float)):
        return round(value * factor, 3)
    if isinstance(value, list):
        return [multiply_numbers(item, factor) for item in value]
    if isinstance(value, dict):
        return {key: item if key in {"level", "level_hint"} else multiply_numbers(item, factor) for key, item in value.items()}
    return value


def apply_annotation_overrides(data, material, profiles):
    main = data["properties"].get("silentgear:main")
    if main and material in PROFILE_OVERRIDES:
        profile_name, factor = PROFILE_OVERRIDES[material]
        profile = profiles[profile_name]
        for key, value in profile.items():
            if key not in {"traits"}:
                main[key] = multiply_numbers(value, factor)
    factor = ATTRIBUTE_MULTIPLIERS.get(material)
    if material in SINS:
        factor = 3.0 if material == "pride" else 2.5
    if factor is not None:
        for properties in data["properties"].values():
            for key, value in list(properties.items()):
                if key not in {"traits", "harvest_tier"}:
                    properties[key] = multiply_numbers(value, factor)


def coating_properties(tier, traits):
    return {
        "armor_durability": {"operation": "MULTIPLY_TOTAL", "value": round(0.04 + tier * 0.28, 3)},
        "armor_toughness": {"operation": "ADD", "value": round(0.5 + tier * 5.5, 3)},
        "attack_damage": {"operation": "MULTIPLY_TOTAL", "value": round(0.05 + tier * 0.35, 3)},
        "durability": {"operation": "MULTIPLY_TOTAL", "value": round(0.08 + tier * 0.32, 3)},
        "harvest_speed": {"operation": "MULTIPLY_TOTAL", "value": round(0.03 + tier * 0.17, 3)},
        "magic_damage": {"operation": "MULTIPLY_TOTAL", "value": round(0.05 + tier * 0.35, 3)},
        "ranged_damage": {"operation": "MULTIPLY_TOTAL", "value": round(0.05 + tier * 0.35, 3)},
        "traits": traits,
    }


def metal_level_centers(family):
    return {
        "hard_metal": [12, 16, 20, 25, 32, 41, 53, 68],
        "super_metal": [82, 105, 134, 171, 218],
        "god_metal": [300, 460, 700, 1000],
        "soft_metal": [8, 11, 15, 20],
    }[family]


def semantic_level_value(centers, level, bias):
    center = centers[level - 1]
    if bias < 0 and level > 1:
        return round(center + (center - centers[level - 2]) * bias * 0.8, 3)
    if bias > 0 and level < len(centers):
        return round(center + (centers[level] - center) * bias * 0.8, 3)
    return center


def designed_main_properties(family, level, traits, semantic_bias=0.0):
    armor = semantic_level_value(metal_level_centers(family), level, semantic_bias)
    if family == "hard_metal":
        durability, melee, tools, ranged = armor * 28, armor * 0.16, armor * 0.38, armor * 0.07
    elif family == "super_metal":
        durability, melee, tools, ranged = armor * 34, armor * 0.19, armor * 0.48, armor * 0.13
    elif family == "god_metal":
        durability, melee, tools, ranged = armor * 42, armor * 0.23, armor * 0.60, armor * 0.18
    else:
        durability, melee, tools, ranged = armor * 8, armor * 0.10, armor * 0.85, armor * 0.16
    harvest = min(4, max(1, level if family == "hard_metal" else level + 3 if family != "soft_metal" else level))
    tier_name = ("stone", "iron", "diamond", "netherite")[harvest - 1]
    return {
        "armor": armor, "armor/boots": round(armor * 0.15, 3), "armor/chestplate": round(armor * 0.4, 3),
        "armor/helmet": round(armor * 0.15, 3), "armor/leggings": round(armor * 0.3, 3),
        "armor_durability": round(durability / 18, 3), "armor_toughness": round(armor * 0.18, 3),
        "attack_damage": round(melee, 3), "charging_value": round(0.6 + armor / 120, 3),
        "draw_speed": round(0.05 + ranged / 12, 3), "durability": round(durability, 3),
        "enchantment_value": round(10 + armor * 0.32, 3), "harvest_speed": round(tools, 3),
        "harvest_tier": {"incorrect_blocks_for_tool": f"minecraft:incorrect_for_{tier_name}_tool", "level_hint": str(harvest), "name": tier_name},
        "magic_armor": round(armor * 0.38, 3), "magic_damage": round(melee * 0.85, 3),
        "projectile_accuracy": round(1.0 + ranged / 18, 3), "projectile_speed": round(1.0 + ranged / 15, 3),
        "ranged_damage": round(ranged, 3), "rarity": round(12 + armor * 0.75, 3), "traits": traits,
    }


def designed_traits(family, material, resource_id, level):
    selected = []
    text = f"{material}_{resource_id}".lower()
    for word, special in SPECIAL_TRAITS.items():
        if word in text and special not in selected:
            selected.append(special)
    if family == "soft_metal":
        selected.extend(name for name in ("soft", "malleable") if name not in selected)
    elif family == "super_metal" and material.endswith("spellweave_ingot"):
        selected.extend(name for name in ("soft", "malleable") if name not in selected)
    else:
        selected.append("malleable")
    return [trait(name, max(1, min(5, level))) for name in selected[:2]]


def apply_metal_design(data, entry):
    design = METAL_DESIGN.get(entry["id"])
    if design is None:
        return
    family, level = design["family"], design["level"]
    if family == "metal_coating":
        data["crafting"]["categories"] = ["metal_coating", "advanced"]
    traits = designed_traits(family, entry["material"], entry["id"], level)
    if family != "metal_coating":
        data["crafting"]["categories"] = [family, "endgame" if family in {"super_metal", "god_metal"} else "advanced"]
        semantic_bias = METAL_SEMANTIC_BIAS.get(entry["id"], 0.0)
        data["properties"] = {"silentgear:main": designed_main_properties(family, level, traits, semantic_bias)}
        rod_level = max(1, min(5, level))
        if family == "soft_metal":
            data["properties"]["silentgear:rod"] = {
                "draw_speed": {"operation": "ADD", "value": round(0.15 + level * 0.08, 3)},
                "harvest_speed": {"operation": "ADD", "value": round(1.5 + level * 0.75, 3)},
                "projectile_speed": {"operation": "ADD", "value": round(0.1 + level * 0.08, 3)},
                "traits": traits,
            }
        else:
            data["properties"]["silentgear:rod"] = {"traits": [trait("malleable", rod_level)]}
        if family == "super_metal" and entry["material"].endswith("spellweave_ingot"):
            data["properties"].update({
                "silentgear:binding": {"repair_efficiency": {"operation": "MULTIPLY_BASE", "value": round(0.2 + level * 0.06, 3)}, "traits": traits},
                "silentgear:cord": {"draw_speed": {"operation": "MULTIPLY_BASE", "value": round(0.18 + level * 0.05, 3)}, "traits": traits},
                "silentgear:fletching": {"projectile_accuracy": {"operation": "MULTIPLY_BASE", "value": round(0.18 + level * 0.05, 3)}, "traits": traits},
                "silentgear:lining": {"armor_durability": {"operation": "MULTIPLY_BASE", "value": round(0.2 + level * 0.06, 3)}, "traits": traits},
            })
    coating_level = design.get("coating_level", level if family == "metal_coating" else None)
    if coating_level is not None:
        coating_traits = designed_traits("soft_metal" if family == "soft_metal" else "hard_metal", entry["material"], entry["id"], coating_level)
        coating_centers = [0.36, 0.54, 0.72, 0.90]
        coating_bias = COATING_SEMANTIC_BIAS.get(entry["id"], 0.0) if coating_level >= 2 else 0.0
        coating_tier = semantic_level_value(coating_centers, coating_level, coating_bias)
        data["properties"]["silentgear:coating"] = coating_properties(coating_tier, coating_traits)


def apply_part_override(data, material, tier, traits):
    if material in COATING_MATERIALS:
        data["crafting"]["categories"] = ["coating", data["crafting"]["categories"][-1]]
        data["properties"] = {"silentgear:coating": coating_properties(tier, traits)}
    elif material in ROD_MATERIALS:
        data["crafting"]["categories"] = ["rod", data["crafting"]["categories"][-1]]
        data["properties"] = {
            "silentgear:rod": {
                "durability": {"operation": "MULTIPLY_TOTAL", "value": round(0.05 + tier * 0.25, 3)},
                "harvest_speed": {"operation": "MULTIPLY_TOTAL", "value": round(0.03 + tier * 0.17, 3)},
                "traits": [trait("flexible", max(1, min(5, 1 + round(tier * 4))))],
            }
        }
    elif material in CORD_MATERIALS:
        data["crafting"]["categories"] = ["fiber", data["crafting"]["categories"][-1]]
        data["properties"] = {
            "silentgear:cord": {
                "draw_speed": {"operation": "MULTIPLY_BASE", "value": round(0.05 + tier * 0.35, 3)},
                "traits": [trait("flexible", max(1, min(5, 1 + round(tier * 4))))],
            }
        }


def apply_mekanism_alloy_profile(data, material, profiles):
    if material not in MEKANISM_ALLOY_ORDER:
        return
    index = MEKANISM_ALLOY_ORDER.index(material)
    target_armor = 15.0 + (200.0 - 15.0) * index / (len(MEKANISM_ALLOY_ORDER) - 1)
    factor = target_armor / profiles["iron"]["armor"]
    main = data["properties"].get("silentgear:main")
    if main:
        for key, value in profiles["iron"].items():
            if key != "traits":
                main[key] = multiply_numbers(value, factor)


def apply_target_armor(data, material, profiles):
    target = PROFILE_TARGET_ARMOR.get(material)
    main = data["properties"].get("silentgear:main")
    if target is None or main is None:
        return
    factor = target / main["armor"]
    for key, value in list(main.items()):
        if key not in {"traits", "harvest_tier"}:
            main[key] = multiply_numbers(value, factor)


def make_material(category, entry, bounds, profiles, resolver):
    material = entry["material"]
    tier = strength(category, material, entry["id"])
    traits = traits_for(category, material, entry["id"], tier)
    texture = "HIGH_CONTRAST" if category in {"岩石", "宝石", "金属"} else "LOW_CONTRAST"
    tier_category = "basic" if tier < 0.25 else "intermediate" if tier < 0.5 else "advanced" if tier < 0.8 else "endgame"
    category_name = {"木头": "wood", "岩石": "stone", "纤维": "fiber", "宝石": "gem", "金属": "metal", "有机物": "organic"}[category]
    data = {
        "type": "silentgear:simple",
        "parent": "silentgear:empty",
        "crafting": {
            "can_salvage": True,
            "categories": [category_name, tier_category],
            "gear_type_blacklist": [],
            "ingredient": {"item": entry["id"]},
            "part_substitutes": {},
        },
        "display": {
            "color": color_for(category, material, entry["id"], resolver),
            "main_texture_type": texture,
            "name": {"translate": generated_translation_key(entry)},
            "name_prefix": "",
        },
        "properties": {},
    }
    if category in {"木头", "岩石", "宝石", "金属"}:
        data["properties"]["silentgear:main"] = main_properties(bounds, tier, traits)
        if category == "宝石":
            data["properties"]["silentgear:tip"] = {
                "attack_damage": {"operation": "ADD", "value": round(scale(bounds, "attack_damage", tier) * 0.35, 3)},
                "durability": {"operation": "ADD", "value": round(scale(bounds, "durability", tier) * 0.2, 3)},
                "harvest_speed": {"operation": "ADD", "value": round(scale(bounds, "harvest_speed", tier) * 0.2, 3)},
                "traits": traits[:2],
            }
        if category in {"木头", "金属"}:
            data["properties"]["silentgear:rod"] = {"traits": [trait("flexible" if category == "木头" else "malleable", max(1, min(5, 1 + round(tier * 4))))]}
    else:
        level = max(1, min(5, 1 + round(tier * 4)))
        data["properties"]["silentgear:binding"] = {
            "repair_efficiency": {"operation": "MULTIPLY_BASE", "value": round(0.025 + tier * 0.475, 3)},
            "traits": traits,
        }
        data["properties"]["silentgear:cord"] = {
            "draw_speed": {"operation": "MULTIPLY_BASE", "value": round(0.05 + tier * 0.35, 3)},
            "traits": [trait("flexible", level)],
        }
        data["properties"]["silentgear:fletching"] = {
            "projectile_accuracy": {"operation": "MULTIPLY_BASE", "value": round(0.05 + tier * 0.35, 3)},
            "projectile_speed": {"operation": "MULTIPLY_BASE", "value": round(0.025 + tier * 0.275, 3)},
            "traits": traits[:2],
        }
        data["properties"]["silentgear:lining"] = {
            "armor_durability": {"operation": "MULTIPLY_BASE", "value": round(0.025 + tier * 0.375, 3)},
            "magic_armor": {"operation": "ADD", "value": round(scale(bounds, "magic_armor", tier) * 0.1, 3)},
            "traits": traits[:2],
        }
    apply_mekanism_alloy_profile(data, material, profiles)
    apply_part_override(data, material, tier, traits)
    apply_annotation_overrides(data, material, profiles)
    apply_target_armor(data, material, profiles)
    if material == "quark_ingot":
        factor = 1000.0 / data["properties"]["silentgear:main"]["armor"]
        for properties in data["properties"].values():
            for key, value in list(properties.items()):
                if key not in {"traits", "harvest_tier"}:
                    properties[key] = multiply_numbers(value, factor)
    apply_metal_design(data, entry)
    return data, tier


def main():
    jar = find_jar()
    builtins, bounds, profiles, builtin_data = builtin_materials_and_bounds(jar)
    resolver = AssetResolver()
    existing = existing_materials()
    source = json.loads(INPUT.read_text(encoding="utf-8"))
    wood_materials = {entry["material"] for entry in source.get("木头", [])}
    candidates = []
    for category, entries in source.items():
        if category not in CATEGORY_ORDER:
            continue
        for entry in entries:
            entry = dict(entry)
            material = MATERIAL_ALIASES.get(entry["material"], entry["material"])
            entry["material"] = material
            if material in GEM_OVERRIDES:
                category = "宝石"
            elif material in EXTRA_CATEGORY_OVERRIDES:
                category = EXTRA_CATEGORY_OVERRIDES[material]
            if entry["material"] in builtins or entry["material"] in existing or not valid(category, entry):
                continue
            candidates.append((category, entry))
    for category, entry in extra_entries():
        design = METAL_DESIGN.get(entry["id"])
        if design is not None:
            entry["material"] = design["material"]
            category = "金属"
        if design is not None or entry["material"] not in builtins and entry["material"] not in existing and valid(category, entry):
            candidates.append((category, entry))
    candidate_ids = {entry["id"] for _, entry in candidates}
    for resource_id, design in METAL_DESIGN.items():
        if resource_id not in candidate_ids:
            candidates.append(("金属", {
                "id": resource_id,
                "material": design["material"],
                "name": design["material"].replace("_", " ").title(),
                "extra": True,
            }))

    candidates.sort(key=lambda item: (not item[1].get("extra", False), CATEGORY_ORDER[item[0]], item[1]["material"], item[1]["id"]))
    chosen = []
    seen_materials = set()
    seen_ingredients = set()
    for category, entry in candidates:
        if entry["material"] in seen_materials or entry["id"] in seen_ingredients:
            continue
        seen_materials.add(entry["material"])
        seen_ingredients.add(entry["id"])
        chosen.append((category, entry))

    OUTPUT.mkdir(parents=True, exist_ok=True)
    expected = set()
    tiers = []
    designed_ids = set()
    designed_values = {}
    for category, entry in chosen:
        namespace = entry["id"].split(":", 1)[0]
        builtin_path = BUILTIN_PATHS.get(entry["id"])
        if builtin_path not in builtin_data:
            builtin_path = None
        path = OVERRIDE_OUTPUT / f"{builtin_path}.json" if builtin_path else OUTPUT / namespace / f"{entry['material']}.json"
        expected.add(path.resolve())
        if builtin_path:
            data = json.loads(json.dumps(builtin_data[builtin_path]))
            apply_metal_design(data, entry)
            tier = entry["id"] in METAL_DESIGN and METAL_DESIGN[entry["id"]]["level"] / 8 or 0.5
        else:
            data, tier = make_material(category, entry, bounds, profiles, resolver)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        tiers.append((tier, category, entry["material"], entry["id"]))
        design = METAL_DESIGN.get(entry["id"])
        if design is not None:
            designed_ids.add(entry["id"])
            main = data["properties"].get("silentgear:main")
            if main is not None and design["family"] != "metal_coating":
                centers = metal_level_centers(design["family"])
                lower = centers[design["level"] - 2] if design["level"] > 1 else centers[0]
                upper = centers[design["level"]] if design["level"] < len(centers) else centers[-1]
                designed_values[entry["id"]] = (lower, main["armor"], upper)

    for path in OUTPUT.rglob("*.json"):
        if path.resolve() not in expected:
            path.unlink()

    assert len(expected) == len(chosen)
    assert all(json.loads(path.read_text(encoding="utf-8"))["type"] == "silentgear:simple" for path in expected)
    assert designed_ids == set(METAL_DESIGN)
    assert all(lower <= value <= upper for lower, value, upper in designed_values.values())
    generated_lang = write_generated_lang([entry for _, entry in chosen], resolver)
    resolver.close()
    print(f"Silent Gear JAR: {jar.name}")
    print(f"Built-in material names skipped: {len(builtins)}")
    print(f"Generated materials: {len(chosen)}")
    print(f"Texture-derived colors: {resolver.color_hits}; fallback colors: {resolver.color_fallbacks}")
    print(f"Generated language keys: {len(generated_lang['zh_cn'])}")
    for tier, category, material, resource_id in sorted(tiers)[:3]:
        print(f"Weak: {tier:.3f} {category} {material} <- {resource_id}")
    for tier, category, material, resource_id in sorted(tiers)[-3:]:
        print(f"Strong: {tier:.3f} {category} {material} <- {resource_id}")


if __name__ == "__main__":
    main()
