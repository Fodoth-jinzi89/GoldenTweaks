import json
import re
from pathlib import Path


INPUT_DIR = Path(__file__).parent / "output"
ASPECT_DIR = Path(__file__).parents[2] / "src/main/resources/data/goldentweaks/recipe/thaumcraft/aspects"
ENTITY_ASPECT_DIR = ASPECT_DIR.parent / "entity_aspects"
BATCH_START = 225
BATCH_SIZE = 25
MAX_ASPECTS = 6
VALID_PATH = re.compile(r"^[a-z0-9_/-]+$")
COLORS = {
    "black", "blue", "brown", "cyan", "gray", "green", "light_blue", "light_gray",
    "lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow",
}
THAUMCRAFT_EXACT_ASPECTS = {
    "cinnabar_ore": {"terra": 1, "metallum": 2, "permutatio": 2, "venenum": 1},
    "raw_cinnabar": {"terra": 1, "metallum": 2, "permutatio": 2, "venenum": 1},
    "amber_bearing_stone": {"terra": 1, "vinculum": 3, "vitreus": 2},
    "taint_block": {"arbor": 1, "vitium": 3},
    "tainted_soil": {"terra": 1, "vitium": 3},
    "flesh_block": {"corpus": 9, "exanimis": 9},
    "taint_fibres": {"victus": 1, "vitium": 2},
    "greatwood_log": {"arbor": 3, "praecantatio": 1},
    "silverwood_log": {"arbor": 3, "praecantatio": 1, "ordo": 1},
    "greatwood_leaves": {"herba": 1},
    "silverwood_leaves": {"herba": 1},
    "greatwood_sapling": {"herba": 2, "arbor": 1, "praecantatio": 1},
    "silverwood_sapling": {"herba": 2, "arbor": 1, "praecantatio": 1},
    "shimmerleaf": {"herba": 2, "permutatio": 2, "praecantatio": 2},
    "cinderpearl": {"herba": 2, "ignis": 2, "praecantatio": 2},
    "ethereal_bloom": {"herba": 2, "venenum": 1, "praecantatio": 2, "auram": 2},
    "arcane_stone": {"terra": 1, "praecantatio": 1},
    "arcane_stone_bricks": {"terra": 1, "praecantatio": 1},
    "quicksilver": {"metallum": 3, "venenum": 1, "permutatio": 2},
    "zombie_brain": {"corpus": 2, "cognitio": 4, "exanimis": 2},
    "amber": {"vinculum": 2, "vitreus": 2},
    "knowledge_fragment": {"cognitio": 8},
    "tainted_goo": {"vitium": 3, "limus": 1},
    "taint_tendril": {"vitium": 2, "lucrum": 1, "fames": 1},
    "gold_coin": {"lucrum": 1},
    "loot_bag_common": {"lucrum": 8},
    "loot_bag_uncommon": {"lucrum": 16},
    "loot_bag_rare": {"lucrum": 32},
    "beef_nugget": {"fames": 1},
    "chicken_nugget": {"fames": 1},
    "pork_nugget": {"fames": 1},
    "fish_nugget": {"fames": 1},
    "iron_nugget": {"metallum": 1},
    "copper_nugget": {"metallum": 1},
    "tin_nugget": {"metallum": 1},
    "silver_nugget": {"metallum": 1},
    "lead_nugget": {"metallum": 1},
    "quicksilver_drop": {"metallum": 1},
    "thaumium_nugget": {"metallum": 1},
    "native_iron_cluster": {"ordo": 1, "metallum": 6, "terra": 1},
    "native_copper_cluster": {"ordo": 1, "metallum": 5, "terra": 1, "permutatio": 2},
    "native_tin_cluster": {"ordo": 1, "metallum": 5, "terra": 1, "vitreus": 2},
    "native_silver_cluster": {"ordo": 1, "metallum": 5, "terra": 1, "lucrum": 2},
    "native_lead_cluster": {"ordo": 3, "metallum": 5, "terra": 1},
    "native_cinnabar_cluster": {"ordo": 1, "metallum": 4, "terra": 1, "permutatio": 4, "venenum": 2},
    "native_gold_cluster": {"ordo": 1, "metallum": 4, "terra": 1, "lucrum": 2},
    "thaumonomicon": {"cognitio": 8, "arbor": 2, "praecantatio": 2},
    "thaumonomicon_cheat": {"cognitio": 8, "arbor": 2, "praecantatio": 2},
    "alchemical_furnace": {"praecantatio": 8, "aqua": 8, "fabrico": 8},
    "focus_pech": {"praecantatio": 5, "venenum": 5, "perditio": 5, "alienis": 5, "telum": 5},
    "cultist_plate_chestplate": {"metallum": 5, "alienis": 1},
    "cultist_plate_helm": {"metallum": 5, "alienis": 1},
    "cultist_plate_leggings": {"metallum": 5, "alienis": 1},
    "cultist_leader_plate_chestplate": {"metallum": 5, "alienis": 2},
    "cultist_leader_plate_helm": {"metallum": 5, "alienis": 2},
    "cultist_leader_plate_leggings": {"metallum": 5, "alienis": 2},
    "cultist_robe_chestplate": {"metallum": 3, "pannus": 2, "alienis": 1},
    "cultist_robe_hood": {"metallum": 3, "pannus": 2, "alienis": 1},
    "cultist_robe_leggings": {"metallum": 3, "pannus": 2, "alienis": 1},
    "cultist_boots": {"metallum": 4, "alienis": 1},
    "hatred_wand": {"alienis": 1, "arbor": 2, "pannus": 3},
    "eldritch_eye": {"alienis": 5, "auram": 3, "praecantatio": 3, "sensus": 3, "spiritus": 3},
    "crimson_rites": {"cognitio": 5, "praecantatio": 3, "alienis": 3, "spiritus": 3},
    "runed_tablet": {"vinculum": 4, "cognitio": 4, "machina": 4},
    "primordial_pearl": {"aer": 16, "terra": 16, "ignis": 16, "aqua": 16, "ordo": 16, "perditio": 16},
    "ancient_stone": {"terra": 1, "alienis": 1},
    "ancient_rock": {"terra": 1, "alienis": 1},
    "crusted_stone": {"lux": 1, "terra": 1, "alienis": 1},
}
THAUMCRAFT_PRIMALS = {
    "air": "aer", "earth": "terra", "fire": "ignis",
    "water": "aqua", "order": "ordo", "entropy": "perditio",
}


def add(aspects, aspect, amount):
    if amount > 0:
        aspects[aspect] = max(aspects.get(aspect, 0), amount)


def material_aspects(name, amount=3):
    aspects = {}
    metals = {
        "aluminum", "brass", "bronze", "constantan", "copper", "electrum", "gold", "invar",
        "iridium", "iron", "lead", "nickel", "osmium", "platinum", "silver", "steel", "tin",
        "uranium", "zinc", "allthemodium", "unobtainium", "vibranium", "enderium", "lumium",
        "netherite", "signalum", "cursium", "neutron", "quark", "ancient_metal",
    }
    crystals = {
        "amethyst", "certus", "diamond", "emerald", "fluix", "fluorite", "lapis", "peridot",
        "quartz", "ruby", "sapphire", "crystal", "gem",
    }
    wood = {"acacia", "bamboo", "birch", "cherry", "crimson", "dark_oak", "jungle", "mangrove", "oak", "spruce", "warped"}
    if any(word in name for word in metals):
        add(aspects, "metallum", amount)
    if any(word in name for word in crystals):
        add(aspects, "vitreus", amount)
    if any(word in name for word in wood) or any(word in name for word in ("log", "planks", "wood")):
        add(aspects, "arbor", amount)
    if any(word in name for word in ("stone", "granite", "diorite", "andesite", "deepslate", "basalt", "calcite", "slate", "cobble")):
        add(aspects, "saxum", amount)
    if any(word in name for word in ("sand", "gravel", "dirt", "clay", "earth")):
        add(aspects, "terra", amount)
    if "glass" in name:
        add(aspects, "vitreus", amount)
    if any(word in name for word in ("ice", "snow", "frost")):
        add(aspects, "gelum", amount)
    if any(word in name for word in ("mushroom", "fungus")):
        add(aspects, "fungus", amount)
    if any(word in name for word in ("leaf", "leaves", "flower", "sapling", "grass", "moss", "cactus", "berry")):
        add(aspects, "herba", amount)
    return aspects


def compressed_aspects(path):
    match = re.match(r"(.+)_([1-9])x$", path)
    if not match:
        return None
    base, level_text = match.groups()
    level = int(level_text)
    aspects = material_aspects(base, min(3 + level, 8))
    if not aspects:
        add(aspects, "substance", min(3 + level, 8))
    add(aspects, "dense", level * 2)
    if "block" in base:
        for aspect in list(aspects):
            if aspect != "dense":
                aspects[aspect] += 2
    return aspects


def ore_aspects(path):
    aspects = material_aspects(path, 3)
    if "ore" in path:
        add(aspects, "terra", 3 if "deepslate" not in path else 4)
        add(aspects, "perfodio", 2)
    if "block" in path:
        aspects = material_aspects(path, 12) or {"substance": 8}
    elif "nugget" in path:
        aspects = material_aspects(path, 1)
    elif "ingot" in path:
        aspects = material_aspects(path, 4)
    elif any(word in path for word in ("plate", "rod", "dust", "shard", "clump", "crystal")):
        aspects = material_aspects(path, 2) or {"terra": 2}
    if "gear" in path:
        add(aspects, "machina", 2)
    if "hammer" in path:
        add(aspects, "instrumentum", 3)
    if "cinnabar" in path:
        add(aspects, "vitreus", 2)
        add(aspects, "venenum", 2)
    if "salt" in path:
        add(aspects, "terra", 2)
        add(aspects, "aqua", 2)
    if "sulfur" in path:
        add(aspects, "ignis", 3)
        add(aspects, "perditio", 2)
    if "molten" in path:
        add(aspects, "ignis", 3)
        add(aspects, "metallum", 2)
    if not aspects:
        add(aspects, "terra", 2)
    return dict(sorted(aspects.items(), key=lambda entry: (-entry[1], entry[0]))[:MAX_ASPECTS])


def semantic_aspects(namespace, path, display, kind):
    name = f"{path}_{display.lower().replace(' ', '_')}"
    for color in COLORS:
        name = name.replace(color + "_", "")

    if namespace == "allthecompressed":
        return compressed_aspects(path) or {"dense": 2, "substance": 2}
    if namespace == "alltheores":
        return ore_aspects(path)
    if path.startswith("phial_of_essentia_"):
        phial_aspects = {path.removeprefix("phial_of_essentia_"): 8}
        add(phial_aspects, "vas", 2)
        add(phial_aspects, "vitreus", 1)
        return phial_aspects
    if path.startswith("wisp_essence_"):
        return {path.removeprefix("wisp_essence_"): 2}
    if namespace == "thaumcraft":
        if path in THAUMCRAFT_EXACT_ASPECTS:
            return THAUMCRAFT_EXACT_ASPECTS[path]
        primal = next((aspect for prefix, aspect in THAUMCRAFT_PRIMALS.items() if path.startswith(prefix + "_")), None)
        if primal and path.endswith("_infused_stone"):
            return {"terra": 1, primal: 3, "vitreus": 2}
        if primal and path.endswith("_shard"):
            return {"praecantatio": 1, primal: 2, "vitreus": 1}
        if path.startswith("candle_"):
            return {"lux": 2, "corpus": 1, "praecantatio": 1}

    aspects = material_aspects(name)

    # Living things and magic are decided before generic equipment terms.
    if kind == "entity":
        if "rocket_contraption" in path:
            return {"machina": 4, "volatus": 3, "iter": 3, "potentia": 2}
        if namespace == "tunes_n_tomes" and any(word in name for word in ("note", "aria", "melody", "harp", "music", "segno", "strings", "healing_circle", "hope_shield")):
            return {"sonus": 4, "praecantatio": 3, "aer": 2}
        if any(word in name for word in ("projectile", "bullet", "arrow", "spear", "blade", "beam", "laser", "fireball", "bomb", "blast", "orb", "mine", "strike", "shot", "rocket", "shell", "tnt", "bolt", "missile", "volley", "shuriken")):
            add(aspects, "telum", 4)
            if any(word in name for word in ("bomb", "blast", "fireball", "rocket", "shell", "tnt", "mine")):
                add(aspects, "explosion", 3)
            if any(word in name for word in ("fire", "flame", "lava", "ignis")):
                add(aspects, "ignis", 3)
            if any(word in name for word in ("lightning", "bolt")):
                add(aspects, "fulmen", 3)
            if any(word in name for word in ("zap", "electro", "tesla")):
                add(aspects, "fulmen", 3)
                add(aspects, "potentia", 2)
            return dict(sorted(aspects.items(), key=lambda entry: (-entry[1], entry[0]))[:MAX_ASPECTS])
        if any(word in name for word in ("aoe", "field", "pool", "swarm", "shackle", "earthquake", "electrocute", "chain_lightning", "gust", "breath", "slash")) or "black_hole" in path:
            add(aspects, "praecantatio", 4)
            if "black_hole" in path:
                add(aspects, "vacuos", 4)
                add(aspects, "gravitas", 3)
            if any(word in name for word in ("fire", "flame", "magma", "blaze")):
                add(aspects, "ignis", 3)
            if any(word in name for word in ("ice", "frost", "cold", "blizzard")):
                add(aspects, "gelum", 3)
            if any(word in name for word in ("lightning", "electrocute")):
                add(aspects, "fulmen", 3)
            if any(word in name for word in ("poison", "acid")):
                add(aspects, "venenum", 3)
            return dict(sorted(aspects.items(), key=lambda entry: (-entry[1], entry[0]))[:MAX_ASPECTS])
        if any(word in name for word in ("portal", "rift")):
            return {"alienis": 4, "iter": 3, "vacuos": 2}
        if "falling_block" in name:
            return {"terra": 4, "motus": 2}
        if "corpse" in name:
            return {"mortuus": 5, "corpus": 4, "spiritus": 2}
        if any(word in name for word in ("photograph", "camera_stand", "photo_frame")):
            return {"sensus": 4, "fabrico": 3, "lux": 2, "cognitio": 2}
        add(aspects, "bestia", 4)
        add(aspects, "victus", 3)
        if any(word in name for word in ("familiar", "mage", "cleric", "druid", "summon")):
            add(aspects, "praecantatio", 3)
        if any(word in name for word in ("undead", "necromancer", "soul", "shadow")):
            add(aspects, "mortuus", 3)
        if any(word in name for word in ("dragon", "dragoon")):
            add(aspects, "dragon", 4)
        if any(word in name for word in ("fire", "scorcher")):
            add(aspects, "ignis", 3)
        if any(word in name for word in ("frost", "frozen", "ice")):
            add(aspects, "gelum", 3)
        if any(word in name for word in ("portal", "ender", "end_")):
            add(aspects, "alienis", 3)
        if any(word in name for word in ("draugr", "revenant", "wither", "skeleton", "zombie", "undead")):
            add(aspects, "exanimis", 4)
        if any(word in name for word in ("abyss", "deep", "leviathan")):
            add(aspects, "profundum", 3)
        if any(word in name for word in ("golem", "monstrosity", "contraption", "minecart")):
            add(aspects, "machina", 3)
        if any(word in name for word in ("drone", "robo", "mechanical_wolf", "custom_wolf", "module")):
            add(aspects, "machina", 4)
            add(aspects, "cognitio", 2)
        if any(word in name for word in ("photograph", "item_frame")):
            return {"sensus": 4, "fabrico": 3, "cognitio": 2}
        if "dummy" in name:
            return {"humanus": 3, "fabrico": 2}
        if any(word in name for word in ("wire", "cord", "track")):
            return {"machina": 3, "potentia": 2, "motus": 2}
        if any(word in name for word in ("note", "aria", "melody", "harp", "music", "segno", "strings", "healing_circle", "hope_shield")):
            return {"sonus": 4, "praecantatio": 3, "aer": 2}
        if any(word in name for word in ("irradiated", "radiation", "mutant")):
            add(aspects, "vitium", 4)
            add(aspects, "potentia", 3)
        if any(word in name for word in ("card", "flesh_chunk", "flesh_piece", "flesh_mound")):
            if "card" in name:
                return {"cognitio": 3, "praecantatio": 2}
            return {"corpus": 4, "victus": 2}
        if any(word in name for word in ("spell", "magic", "arcane", "wizard", "mage", "sorcer", "summon", "rune")):
            add(aspects, "praecantatio", 4)
        if any(word in name for word in ("fire", "flame", "magma", "blaze", "cinder")):
            add(aspects, "ignis", 3)
        if any(word in name for word in ("ice", "frost", "cold", "blizzard", "icicle", "cryo")):
            add(aspects, "gelum", 3)
        if any(word in name for word in ("lightning", "electro", "thunder")):
            add(aspects, "fulmen", 3)
        if any(word in name for word in ("poison", "acid", "venom")):
            add(aspects, "venenum", 3)
        if any(word in name for word in ("blood", "flesh")):
            add(aspects, "corpus", 3)
        if any(word in name for word in ("soul", "spirit", "phantom")):
            add(aspects, "spiritus", 3)
        if any(word in name for word in ("crab", "fish", "coral", "sea", "scylla", "hippoc")):
            add(aspects, "aqua", 3)
        if "package" in name:
            add(aspects, "vas", 3)
        if "glue" in name:
            add(aspects, "adhaesio", 3)
        return dict(sorted(aspects.items(), key=lambda entry: (-entry[1], entry[0]))[:MAX_ASPECTS])

    if any(word in name for word in ("sword", "gun", "rifle", "warhead", "weapon", "bow", "arrow")):
        add(aspects, "telum", 4)
    if any(word in name for word in ("axe", "pickaxe", "shovel", "hoe", "hammer", "knife", "wrench", "staff", "tool")):
        add(aspects, "instrumentum", 3)
    if any(word in name for word in ("helmet", "chestplate", "leggings", "boots", "armor", "shield", "plate_tier")):
        add(aspects, "tutamen", 4)
    if any(word in name for word in ("armorplate", "armor_plate", "protection", "defensive", "lining", "force_field")):
        add(aspects, "tutamen", 4)
    if any(word in name for word in ("curio", "trinket", "pendant", "charm", "ring", "crown", "necklace")):
        add(aspects, "accessories", 3)
    if any(word in name for word in ("jewelry", "jewelcraft", "signet", "amulet")):
        add(aspects, "accessories", 3)
        add(aspects, "lucrum", 2)
    if any(word in name for word in ("spell", "magic", "arcane", "rune", "ritual", "enchant", "infused", "imbue", "sorcer", "wizard", "occult")):
        add(aspects, "praecantatio", 4)
    if any(word in name for word in ("wand", "focus", "thaum", "essentia", "vis_", "salis", "alchemy", "alchemical", "infusion")):
        add(aspects, "praecantatio", 4)
    if any(word in name for word in ("enchant", "tome")):
        add(aspects, "incantatio", 4)
    if any(word in name for word in ("book", "tome", "pattern", "processor", "calculation", "logic", "engineering", "memory", "terminal", "monitor", "interface")):
        add(aspects, "cognitio", 3)
    if any(word in name for word in ("machine", "mechanical", "mechanism", "engine", "controller", "provider", "drive", "workbench", "table", "anvil", "servo", "bearing", "joint", "crank", "lever", "gyroscope", "keypad", "joystick", "tracker", "configurator", "cannon", "barrel", "breech", "computer", "circuit", "calculator", "router")):
        add(aspects, "machina", 3)
    if any(word in name for word in ("energy", "charged", "charger", "generator", "power", "electron", "electric", "plasma", "lumen")):
        add(aspects, "potentia", 3)
    if any(word in name for word in ("storage", "cell", "chest", "case", "bag", "cache", "tank")):
        add(aspects, "vacuos", 4)
        add(aspects, "vas", 2)
    if any(word in name for word in ("cable", "wire", "circuit", "card", "upgrade", "component", "core", "press", "plane", "bus")):
        add(aspects, "machina", 2)
    if any(word in name for word in ("wireless", "antenna", "broadcaster")):
        add(aspects, "aer", 2)
        add(aspects, "iter", 2)
    if any(word in name for word in ("redstone", "relay", "transmitter", "modem", "network", "link_bridge", "stock_ticker")):
        add(aspects, "potentia", 2)
        add(aspects, "cognitio", 2)
    if any(word in name for word in ("ender", "quantum", "spatial", "ethereal", "portal", "singularity")):
        add(aspects, "alienis", 3)
    if any(word in name for word in ("warp", "waystone", "sharestone", "portstone", "teleport", "bound_scroll", "return_scroll")):
        add(aspects, "iter", 4)
        add(aspects, "alienis", 2)
    if "singularity" in name:
        add(aspects, "singularity", 5)
    if "black_hole" in path:
        add(aspects, "vacuos", 4)
        add(aspects, "gravitas", 3)
    if any(word in name for word in ("gravity", "levitite", "loadstone")):
        add(aspects, "gravitas", 4)
    if any(word in name for word in ("thruster", "propeller", "wing", "flight", "aero", "tire", "wheel")):
        add(aspects, "motus", 3)
        add(aspects, "volatus", 2)
    if any(word in name for word in ("airfoil", "airflow", "wind_tunnel", "windtunnel")):
        add(aspects, "aer", 4)
        add(aspects, "volatus", 3)
        add(aspects, "machina", 2)
    if any(word in name for word in ("gearbox", "cogwheel", "shaft", "bearing", "belt", "pulley", "gearshift", "transmission", "rotator", "clutch", "flywheel")):
        add(aspects, "machina", 4)
        add(aspects, "motus", 3)
    if any(word in name for word in ("mechanical_arm", "deployer", "piston", "gantry", "contraption_control")):
        add(aspects, "machina", 4)
        add(aspects, "motus", 3)
        add(aspects, "instrumentum", 2)
    if any(word in name for word in ("fan", "nozzle", "windmill", "sail")):
        add(aspects, "aer", 3)
        add(aspects, "motus", 3)
        add(aspects, "machina", 2)
    if any(word in name for word in ("drill", "saw", "millstone", "crushing_wheel")):
        add(aspects, "instrumentum", 4)
        add(aspects, "perditio", 3)
        add(aspects, "machina", 2)
    if any(word in name for word in ("press", "mixer", "basin", "crafter", "crafting_table")):
        add(aspects, "fabrico", 3)
        add(aspects, "machina", 2)
    if any(word in name for word in ("harvester", "plough", "tree_cutter")):
        add(aspects, "meto", 4)
        add(aspects, "instrumentum", 3)
    if any(word in name for word in ("roost", "fishing_net", "farmer", "farming", "crop")):
        add(aspects, "messis", 3)
        add(aspects, "machina", 2)
    if any(word in name for word in ("drill", "excavation", "ore_vein", "miner")):
        add(aspects, "perfodio", 4)
        add(aspects, "instrumentum", 3)
    if any(word in name for word in ("motor", "alternator", "accumulator", "capacitor", "electric", "energy", "generator", "tesla")):
        add(aspects, "potentia", 4)
        add(aspects, "machina", 3)
    if any(word in name for word in ("circuit", "diode", "transistor", "relay", "resistor", "transformer", "voltage", "current", "electrical", "battery", "wire", "cord", "coil")):
        add(aspects, "potentia", 3)
        add(aspects, "machina", 3)
    if any(word in name for word in ("solar", "photonic", "radiance", "shining", "spectrum")):
        add(aspects, "lux", 3)
        add(aspects, "potentia", 3)
    if any(word in name for word in ("fission", "fusion", "thermonuclear", "naquadah", "polonium", "deuterium", "tritium")):
        add(aspects, "potentia", 4)
        add(aspects, "vitium", 2)
    if any(word in name for word in ("reactor", "uranium", "yellowcake", "radioactive", "radiation", "autunite", "nuclear", "graphite")):
        add(aspects, "potentia", 4)
        add(aspects, "vitium", 3)
        if "reactor" in name:
            add(aspects, "machina", 3)
    if any(word in name for word in ("submarine", "ballast", "oxygen", "pressurizer", "rudder", "underwater", "water_thruster", "barometer")):
        add(aspects, "aqua", 3)
        add(aspects, "machina", 2)
    if any(word in name for word in ("rocket", "space_suit", "lander", "interplanetary", "lunar", "martian", "mars", "venus", "mercury", "moon")):
        add(aspects, "space", 3)
        if any(word in name for word in ("rocket", "lander", "navigator")):
            add(aspects, "iter", 3)
    if any(word in name for word in ("jetpack", "propeler", "propeller", "drone")):
        add(aspects, "volatus", 3)
        add(aspects, "machina", 2)
    if any(word in name for word in ("compass", "navigation", "travel", "linker", "lead")):
        add(aspects, "iter", 3)
    if any(word in name for word in ("forge", "furnace", "burning", "thermal", "fire", "flame", "immolation")):
        add(aspects, "ignis", 3)
    if any(word in name for word in ("diesel", "gasoline", "ethanol", "biodiesel", "biofuel", "fuel", "oil", "burner")):
        add(aspects, "potentia", 3)
        add(aspects, "ignis", 2)
    if any(word in name for word in ("explosive", "tnt", "warhead")):
        add(aspects, "explosion", 4)
    if any(word in name for word in ("shell", "torpedo", "rocket", "bomb", "shot", "round", "ammo", "ammunition")):
        add(aspects, "telum", 4)
        if any(word in name for word in ("he_", "explosive", "bomb", "rocket", "torpedo", "depth_charge")):
            add(aspects, "explosion", 3)
    if any(word in name for word in ("autocannon", "big_cannon", "cannon_mount", "cannon_carriage", "mortar", "grapeshot", "propellant", "fuze")):
        add(aspects, "telum", 4)
        add(aspects, "machina", 3)
    if any(word in name for word in ("railgun", "minigun", "plasma_rifle", "gauntlet")):
        add(aspects, "telum", 4)
        add(aspects, "potentia", 3)
    if any(word in name for word in ("sensor", "detector", "agency", "camera")):
        add(aspects, "sensus", 3)
    if any(word in name for word in ("filter", "attribute", "scanner", "rangefinder")):
        add(aspects, "sensus", 3)
    if any(word in name for word in ("optical", "mirror", "lens", "hologram", "radar", "sonar", "binocular", "receptor", "focuser", "beam_reader")):
        add(aspects, "sensus", 4)
        add(aspects, "lux", 2)
    if any(word in name for word in ("camera", "photograph", "film", "projector", "lightroom", "polaroid", "slide", "photo_album")):
        add(aspects, "sensus", 4)
        add(aspects, "lux", 2)
        add(aspects, "cognitio", 2)
    if any(word in name for word in ("quest", "guide", "atlas", "tome", "data_model", "deep_learner", "prediction", "simulation")):
        add(aspects, "cognitio", 4)
    if any(word in name for word in ("research", "notes", "skilltree", "skill_tree", "food_book", "syncbook")):
        add(aspects, "cognitio", 4)
    if any(word in name for word in ("astronomy", "astronomical", "space_atlas", "navigator", "targeting_computer")):
        add(aspects, "cognitio", 3)
        add(aspects, "space", 3)
    if any(word in name for word in ("detective", "magnifier", "evidence", "clue")):
        add(aspects, "sensus", 3)
        add(aspects, "cognitio", 3)
    if any(word in name for word in ("radio", "siren", "alarm", "audio", "speaker")):
        add(aspects, "sonus", 3)
    if any(word in name for word in ("cassette", "television", "tape", "viewfinder")):
        add(aspects, "sensus", 3)
        add(aspects, "machina", 2)
    if any(word in name for word in ("organ", "horn", "flute", "whistle", "trompette", "posaune", "viola", "diapason", "music_roll", "windchest")):
        add(aspects, "sonus", 4)
        add(aspects, "aer", 2)
    if any(word in name for word in ("music_disc", "record")):
        add(aspects, "sonus", 4)
    if any(word in name for word in ("clock", "hourglass")):
        add(aspects, "tempus", 3)
    if any(word in name for word in ("dye", "paint", "color")):
        add(aspects, "sensus", 2)
    if any(word in name for word in ("ink", "chromatic", "palette")):
        add(aspects, "sensus", 3)
    if any(word in name for word in ("skull", "bone", "death", "necrom")):
        add(aspects, "mortuus", 3)
    if any(word in name for word in ("taint", "tainted", "corrupt", "shadow", "dark_matter")):
        add(aspects, "vitium", 3)
        add(aspects, "tenebrae", 2)
    if any(word in name for word in ("celestial", "astrolog", "constellation", "zodiac")):
        add(aspects, "cosmic", 3)
        add(aspects, "praecantatio", 2)
    if any(word in name for word in ("infinity", "mythic", "ancient_material")):
        add(aspects, "terminus", 6)
    if "ultimate" in name:
        add(aspects, "terminus", 5)
    if "cosmic" in name:
        add(aspects, "cosmic", 6)
    if "singularity" in name:
        add(aspects, "singularity", 6)
    if "neutron" in name:
        add(aspects, "dense", 6)
        add(aspects, "gravitas", 4)
    if "crystal_matrix" in name:
        add(aspects, "vitreus", 5)
        add(aspects, "terminus", 3)
    if "blaze" in name:
        add(aspects, "ardor", 4)
        add(aspects, "ignis", 3)
    if any(word in name for word in ("gem", "jewel", "treasure", "loot", "rarity", "sigil")):
        add(aspects, "lucrum", 3)
    if any(word in name for word in ("flux", "emc", "transmutation")):
        add(aspects, "permutatio", 3)
        add(aspects, "potentia", 2)
    if any(word in name for word in ("salvag", "annihilation", "destroy", "pulverizer")):
        add(aspects, "perditio", 3)
    if any(word in name for word in ("repair", "reforg", "augment", "smithing")):
        add(aspects, "fabrico", 3)
    if any(word in name for word in ("copycat", "framed", "tiled", "casing", "scaffolding", "girder", "panel", "catwalk")):
        add(aspects, "fabrico", 3)
    if any(word in name for word in ("chair", "table", "desk", "cabinet", "shelf", "sofa", "couch", "bed", "bench", "stool", "counter", "drawer", "wardrobe", "cupboard", "lamp", "lantern", "decor", "plating", "floor", "velvet")):
        add(aspects, "fabrico", 3)
        if any(word in name for word in ("sofa", "couch", "bed", "velvet")):
            add(aspects, "pannus", 2)
    if any(word in name for word in ("cushion", "rug", "tatami")):
        add(aspects, "pannus", 3)
        add(aspects, "fabrico", 2)
    if any(word in name for word in ("package", "postbox", "crate", "toolbox", "container")):
        add(aspects, "vas", 3)
    if any(word in name for word in ("packager", "unpackager", "package_filler", "packaging_provider", "distributor", "requester")):
        add(aspects, "machina", 3)
        add(aspects, "permutatio", 3)
    if any(word in name for word in ("backpack", "storage", "barrel", "chest", "shulker_box", "inventory")):
        add(aspects, "vacuos", 4)
        add(aspects, "vas", 3)
    if any(word in name for word in ("funnel", "chute", "depot", "transporter", "tunnel", "pipe", "conduit")):
        add(aspects, "permutatio", 3)
        add(aspects, "machina", 2)
    if any(word in name for word in ("glue", "adhesive")):
        add(aspects, "adhaesio", 3)
    if any(word in name for word in ("printer", "blueprint", "printed_page")):
        add(aspects, "charta", 3)
        add(aspects, "cognitio", 2)
    if any(word in name for word in ("speaker", "nixie", "display", "monitor")):
        add(aspects, "sensus", 2)
    if any(word in name for word in ("bed", "carpet", "cloth", "banner", "tire")):
        add(aspects, "pannus", 2)
    if any(word in name for word in ("food", "cake", "fruit", "berry", "flour", "bread", "bagel", "baguette", "toast", "dough", "croissant", "pastry", "cheese", "cream", "milk", "butter", "coffee", "latte", "sandwich", "stew", "meat", "yolk")):
        add(aspects, "victus", 3)
        add(aspects, "fames", 2)
    if any(word in name for word in ("juice", "smoothie", "milkshake", "pizza", "pasta", "rice", "salad", "soup", "jam", "pie", "bacon", "seafood", "vegetable", "egg", "bean", "burger", "taco", "sushi")):
        add(aspects, "victus", 3)
        add(aspects, "fames", 2)
    if any(word in name for word in ("tofu", "noodle", "chicken", "fish", "pork", "beef", "mutton", "dumpling", "broth", "icecream", "popsicle", "tea", "fruit")):
        add(aspects, "victus", 3)
        add(aspects, "fames", 2)
    if any(word in name for word in ("wine", "brandy", "champagne", "brew", "cocktail", "tavern")):
        add(aspects, "aqua", 2)
        add(aspects, "fames", 2)
    if any(word in name for word in ("seed", "crop", "sapling", "almond", "apricot", "avocado", "banana", "barley", "basil", "broccoli", "cabbage", "corn", "cucumber", "garlic", "grape", "pepper", "tomato")):
        add(aspects, "herba", 3)
        add(aspects, "messis", 2)
    if any(word in name for word in ("fluid", "liquid", "faucet", "pump", "basin", "hatch", "multi_fluid")):
        add(aspects, "aqua", 3)
        add(aspects, "machina", 2)
    if any(word in name for word in ("shelf", "bookshelf", "library")):
        add(aspects, "arbor", 3)
        add(aspects, "cognitio", 3)
    if any(word in name for word in ("spawner", "spawn_egg")):
        add(aspects, "bestia", 3)
        add(aspects, "vinculum", 3)
    if "trident" in name:
        add(aspects, "telum", 4)
        add(aspects, "aqua", 2)
    if "web" in name:
        add(aspects, "vinculum", 3)
    if "tendril" in name:
        add(aspects, "herba", 3)
    if "experience" in name:
        add(aspects, "cognitio", 3)
        add(aspects, "potentia", 2)
    if "p2p_tunnel" in name:
        add(aspects, "machina", 3)
        add(aspects, "permutatio", 2)
    if any(word in name for word in ("boat", "minecart")):
        add(aspects, "iter", 3)
        add(aspects, "motus", 2)
    if any(word in name for word in ("blood", "flesh", "meat")):
        add(aspects, "corpus", 3)
    if any(word in name for word in ("abyss", "deep", "sculk")):
        add(aspects, "profundum", 3)
    if any(word in name for word in ("mushroom", "fungus")):
        add(aspects, "fungus", 3)

    if namespace == "biomesoplenty" and not aspects:
        if any(word in name for word in ("leaves", "sapling", "grass", "flower", "bush", "bramble", "barley", "clover", "cattail", "reed", "sprout", "blossom", "fern", "ivy", "vine", "root")):
            add(aspects, "herba", 3)
        elif any(word in name for word in ("button", "door", "fence", "gate", "sign", "slab", "stairs", "trapdoor", "branch", "boat")):
            add(aspects, "arbor", 3)
        else:
            add(aspects, "terra", 3)
    elif namespace == "cataclysm" and not aspects:
        if any(word in name for word in ("spear", "athame", "astrape", "brontes", "ceraunus", "bardiche", "halberd", "claw", "grip")):
            add(aspects, "telum", 4)
        elif any(word in name for word in ("altar", "eye", "soul", "curse", "tomb", "seal", "sacrifice")):
            add(aspects, "praecantatio", 3)
            add(aspects, "tenebrae", 2)
        else:
            add(aspects, "reliquiae", 3)
    elif namespace == "bits_n_bobs" and not aspects:
        if any(word in name for word in ("chair", "seat")):
            add(aspects, "pannus", 2)
        elif any(word in name for word in ("tile", "girder", "grating", "strut")):
            add(aspects, "saxum", 3)
        else:
            add(aspects, "machina", 3)
    elif namespace == "bakeries" and not aspects:
        add(aspects, "victus", 3)
        add(aspects, "fames", 2)
    elif namespace == "backpack_pixel" and not aspects:
        if any(word in name for word in ("container", "box", "pack")):
            add(aspects, "vas", 3)
        else:
            add(aspects, "machina", 3)
    elif namespace.startswith("avaritia") and not aspects:
        add(aspects, "terminus", 3)
    elif namespace in {"cbc_ballistics", "cbcmoreshells"} and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "telum", 2)
    elif namespace in {"cc_better_recipes", "ccbr", "cccbridge"} and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "cognitio", 2)
    elif namespace == "azimuth" and not aspects:
        add(aspects, "cognitio", 3)
        add(aspects, "ordo", 2)
    elif namespace == "bountiful" and not aspects:
        add(aspects, "lucrum", 3)
        add(aspects, "cognitio", 2)
    elif namespace == "capacity" and not aspects:
        add(aspects, "vacuos", 3)
    elif namespace in {"computercraft", "ccredstonelinkbridge", "ccterminals", "create_cc", "create_computercraft_integration"} and not aspects:
        add(aspects, "cognitio", 3)
        add(aspects, "machina", 3)
    elif namespace.startswith("create") and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "motus", 2)
    elif namespace in {"compactflap", "compactgearbox", "cmparallelpipes", "cmverticaladditions"} and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "motus", 2)
    elif namespace == "compactmachines" and not aspects:
        add(aspects, "space", 3)
        add(aspects, "machina", 2)
    elif namespace == "cmpackagecouriers" and not aspects:
        add(aspects, "iter", 3)
        add(aspects, "vas", 2)
    elif namespace == "copycats" and not aspects:
        add(aspects, "fabrico", 3)
        add(aspects, "permutatio", 2)
    elif namespace == "corpse" and not aspects:
        add(aspects, "mortuus", 5)
        add(aspects, "corpus", 4)
    elif namespace in {"createbigcannons", "create_radar"} and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "telum", 3)
    elif namespace in {"create_optical", "createmoderntech"} and not aspects:
        add(aspects, "sensus", 3)
        add(aspects, "machina", 2)
    elif namespace in {"createdieselgenerators", "createaddition", "create_vehiclework"} and not aspects:
        add(aspects, "potentia", 3)
        add(aspects, "machina", 3)
    elif namespace in {"create_integrated_farming"} and not aspects:
        add(aspects, "messis", 3)
        add(aspects, "machina", 2)
    elif namespace in {"create_submarine"} and not aspects:
        add(aspects, "aqua", 3)
        add(aspects, "machina", 2)
    elif namespace in {"createmechanicalcompanion", "create_sa"} and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "cognitio", 2)
    elif namespace in {"createframed"} and not aspects:
        add(aspects, "fabrico", 3)
        add(aspects, "vitreus", 2)
    elif namespace in {"createadditionallogistics", "createmetalogistics", "create_mobile_packages"} and not aspects:
        add(aspects, "permutatio", 3)
        add(aspects, "machina", 2)
    elif namespace == "createnuclear" and not aspects:
        add(aspects, "potentia", 3)
        add(aspects, "vitium", 2)
    elif namespace in {"croptopia", "cuisinedelight"} and not aspects:
        add(aspects, "victus", 3)
        add(aspects, "fames", 2)
    elif namespace in {"dndecor", "dndesires"} and not aspects:
        add(aspects, "fabrico", 3)
    elif namespace in {"densemekanism", "evolvedmekanism"} and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "potentia", 2)
    elif namespace in {"exposure", "exposure_expanded", "exposure_polaroid", "exposuredetective"} and not aspects:
        add(aspects, "sensus", 3)
        add(aspects, "cognitio", 2)
    elif namespace == "efield" and not aspects:
        add(aspects, "potentia", 4)
        add(aspects, "tutamen", 3)
    elif namespace == "discovery" and not aspects:
        add(aspects, "cognitio", 3)
    elif namespace == "drivebywiretypewriter" and not aspects:
        add(aspects, "cognitio", 3)
        add(aspects, "machina", 2)
    elif namespace in {"farmersdelight", "flavor_immersed_daily", "flavor_immersed_daily_food"} and not aspects:
        add(aspects, "victus", 3)
        add(aspects, "fames", 2)
    elif namespace == "fluidlogistics" and not aspects:
        add(aspects, "aqua", 3)
        add(aspects, "machina", 2)
    elif namespace == "forbiddenmagic" and not aspects:
        add(aspects, "praecantatio", 4)
        add(aspects, "vitium", 2)
    elif namespace in {"ftbfiltersystem", "ftblibrary", "ftbquests", "guideme"} and not aspects:
        add(aspects, "cognitio", 3)
    elif namespace == "ftbultimine" and not aspects:
        add(aspects, "perfodio", 4)
        add(aspects, "instrumentum", 2)
    elif namespace == "hostilenetworks" and not aspects:
        add(aspects, "cognitio", 3)
        add(aspects, "machina", 2)
    elif namespace in {"hazennstuff", "hazentouvelib"} and not aspects:
        add(aspects, "praecantatio", 3)
    elif namespace == "goldentweaks" and not aspects:
        add(aspects, "praecantatio", 3)
    elif namespace == "exspectriments" and not aspects:
        if "lab_coat" in name:
            add(aspects, "pannus", 3)
            add(aspects, "cognitio", 2)
        elif "pigment" in name:
            add(aspects, "sensus", 3)
            add(aspects, "machina", 2)
        else:
            add(aspects, "cognitio", 3)
    elif namespace == "extendedae" and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "cognitio", 2)
    elif namespace == "extendedcrafting" and not aspects:
        add(aspects, "fabrico", 3)
        add(aspects, "ordo", 2)
    elif namespace == "held" and not aspects:
        add(aspects, "humanus", 2)
        add(aspects, "instrumentum", 2)
    elif namespace == "igleelib" and not aspects:
        add(aspects, "metallum", 3)
        if "blazum" in name:
            add(aspects, "ardor", 3)
        elif "lavium" in name:
            add(aspects, "lava", 3)
    elif namespace in {"irons_spellbooks", "iss_magicfromtheeast", "maidspell"} and not aspects:
        add(aspects, "praecantatio", 4)
    elif namespace in {"irons_jewelry"} and not aspects:
        add(aspects, "accessories", 3)
        add(aspects, "lucrum", 2)
    elif namespace in {"interiors", "irons_lib", "irons_patreon_lib"} and not aspects:
        add(aspects, "fabrico", 3)
    elif namespace in {"kaleidoscope_cookery", "kaleidoscope_nether", "kaleidoscope_tavern", "maid_restaurant", "maidsoulkitchen"} and not aspects:
        add(aspects, "victus", 3)
        add(aspects, "fames", 2)
    elif namespace in {"kaleidoscope_doll", "kaleidoscopedoll"} and not aspects:
        add(aspects, "childish", 3)
        add(aspects, "fabrico", 2)
    elif namespace in {"maid_storage_manager", "maidbeacon"} and not aspects:
        add(aspects, "cognitio", 3)
    elif namespace in {"mekanism", "mekanicalcreate"} and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "potentia", 2)
    elif namespace in {"mekanism_extras", "mekmm", "mekanismadvancedgenerators", "mekanismgenerators"} and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "potentia", 3)
    elif namespace == "mekanism_weaponry" and not aspects:
        add(aspects, "telum", 3)
        add(aspects, "machina", 2)
    elif namespace == "mekanismtools" and not aspects:
        add(aspects, "instrumentum", 3)
    elif namespace == "merequester" and not aspects:
        add(aspects, "cognitio", 3)
        add(aspects, "machina", 2)
    elif namespace == "neoecoae" and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "cognitio", 2)
    elif namespace == "neoguanniao" and not aspects:
        add(aspects, "bestia", 3)
    elif namespace == "netmusic" and not aspects:
        add(aspects, "sonus", 4)
        add(aspects, "machina", 2)
    elif namespace == "northstar" and not aspects:
        add(aspects, "space", 3)
        add(aspects, "machina", 2)
    elif namespace in {"packagedauto", "packagedavaritia", "packagedexcrafting", "packagedmekemicals"} and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "fabrico", 2)
    elif namespace == "minecraft" and not aspects:
        if "potion" in name:
            add(aspects, "aqua", 3)
            add(aspects, "praecantatio", 2)
        else:
            add(aspects, "substance", 2)
    elif namespace == "modonomicon" and not aspects:
        add(aspects, "cognitio", 4)
        add(aspects, "praecantatio", 2)
    elif namespace == "moonlight" and not aspects:
        add(aspects, "bestia", 3)
        add(aspects, "vinculum", 2)
    elif namespace == "nodalmechanics" and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "potentia", 2)
    elif namespace in {"patchouli", "researchnotes", "skilltree", "solcarrot", "spectral-sync"} and not aspects:
        add(aspects, "cognitio", 4)
    elif namespace == "pipeorgans" and not aspects:
        add(aspects, "sonus", 4)
        add(aspects, "machina", 2)
    elif namespace == "powergrid" and not aspects:
        add(aspects, "potentia", 4)
        add(aspects, "machina", 3)
    elif namespace == "protection_pixel" and not aspects:
        add(aspects, "tutamen", 4)
    elif namespace == "ratatouille" and not aspects:
        add(aspects, "victus", 3)
        add(aspects, "fames", 2)
    elif namespace in {"silentgear", "silentgems"} and not aspects:
        add(aspects, "fabrico", 3)
    elif namespace in {"sophisticatedbackpacks", "sophisticatedstorage", "sophisticatedcore"} and not aspects:
        add(aspects, "vacuos", 4)
        add(aspects, "vas", 3)
    elif namespace in {"simulated_addition", "simulatedrubies", "sg_tracks", "sable_tournament"} and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "motus", 2)
    elif namespace == "powerful_dummy" and not aspects:
        add(aspects, "humanus", 3)
        add(aspects, "fabrico", 2)
    elif namespace == "questshop" and not aspects:
        add(aspects, "lucrum", 4)
        add(aspects, "metallum", 2)
    elif namespace in {"spectrum", "taintedmagic", "thaumcraft", "thaumcraftcelestial", "thaumic_tinkerer", "touhou_little_maid_spell", "traveloptics", "tunes_n_tomes"} and not aspects:
        add(aspects, "praecantatio", 4)
    elif namespace in {"spectral_decorations", "stoneworks", "supplementaries"} and not aspects:
        add(aspects, "fabrico", 3)
    elif namespace in {"steampowered", "stabilized", "synaxis", "tracks"} and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "motus", 2)
    elif namespace in {"touhou_little_maid", "touhou_lost_maid"} and not aspects:
        add(aspects, "humanus", 3)
    elif namespace in {"vista", "vista_plus"} and not aspects:
        add(aspects, "sensus", 3)
        add(aspects, "machina", 2)
    elif namespace == "waystones" and not aspects:
        add(aspects, "iter", 4)
        add(aspects, "alienis", 2)
    elif namespace == "windtunnel" and not aspects:
        add(aspects, "aer", 4)
        add(aspects, "machina", 2)
    elif namespace == "torchmaster" and not aspects:
        if "frozen" in name:
            add(aspects, "gelum", 3)
            add(aspects, "iter", 2)
        else:
            add(aspects, "lux", 4)
            add(aspects, "ignis", 2)
    elif namespace == "jaopca" and not aspects:
        if any(word in name for word in ("molten", "solution", "sulfate")):
            add(aspects, "aqua", 3)
            add(aspects, "metallum", 2)
        else:
            add(aspects, "terra", 2)
            add(aspects, "metallum", 2)
    elif namespace == "linearbearing" and not aspects:
        if "magnetic" in name:
            add(aspects, "magnetis", 3)
            add(aspects, "machina", 2)
        elif "bubble_gum" in name:
            add(aspects, "adhaesio", 3)
        else:
            add(aspects, "machina", 3)
            add(aspects, "motus", 2)
    elif namespace == "lootr" and not aspects:
        add(aspects, "treasure", 3)
        add(aspects, "lucrum", 2)
    elif namespace == "lzxnonefate" and not aspects:
        if "key" in name:
            add(aspects, "vinculum", 2)
            add(aspects, "iter", 2)
        else:
            add(aspects, "cognitio", 2)
    elif namespace == "mek_x_star" and not aspects:
        if "wool" in name or "insulation" in name:
            add(aspects, "pannus", 3)
            add(aspects, "tutamen", 2)
        else:
            add(aspects, "metallum", 3)

    if namespace.startswith("ae2") and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "ordo", 2)
    elif namespace.startswith("aero") and not aspects:
        add(aspects, "machina", 3)
        add(aspects, "motus", 2)
    elif namespace in {"apotheosis", "apotheosis_create", "apotheosis_things", "ancientreforging"}:
        add(aspects, "praecantatio", 2)
    elif namespace == "alshanex_familiars":
        add(aspects, "praecantatio", 2)
    elif not aspects:
        add(aspects, "substance", 2)

    return dict(sorted(aspects.items(), key=lambda entry: (-entry[1], entry[0]))[:MAX_ASPECTS])


def valid_entry(resource_location):
    _, path = resource_location.split(":", 1)
    return bool(VALID_PATH.fullmatch(path))


def write_entry(root, resource_location, aspects, entity=False):
    namespace, path = resource_location.split(":", 1)
    output = root / namespace / f"{path}.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    target_key = "entity" if entity else "item"
    data = {
        "type": f"goldentweaks:{'entity_aspect' if entity else 'item_aspect'}",
        target_key: {"id": resource_location},
        "aspects": aspects,
    }
    output.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    files = sorted(INPUT_DIR.glob("*.json"))[BATCH_START:BATCH_START + BATCH_SIZE]
    namespaces = [path.stem for path in files]
    for namespace in namespaces:
        for root in (ASPECT_DIR, ENTITY_ASPECT_DIR):
            directory = root / namespace
            if directory.exists():
                for old_file in directory.rglob("*.json"):
                    old_file.unlink()

    item_count = entity_count = skipped = 0
    for source in files:
        data = json.loads(source.read_text(encoding="utf-8"))
        resources = {}
        for kind in ("items", "blocks"):
            for resource_location, display in data[kind].items():
                if valid_entry(resource_location):
                    resources[resource_location] = (display, kind[:-1])
                else:
                    skipped += 1
        for resource_location, (display, kind) in sorted(resources.items()):
            _, path = resource_location.split(":", 1)
            write_entry(ASPECT_DIR, resource_location, semantic_aspects(source.stem, path, display, kind))
            item_count += 1
        for resource_location, display in sorted(data["entities"].items()):
            if not valid_entry(resource_location):
                skipped += 1
                continue
            _, path = resource_location.split(":", 1)
            write_entry(ENTITY_ASPECT_DIR, resource_location, semantic_aspects(source.stem, path, display, "entity"), True)
            entity_count += 1

    print(f"批次 {BATCH_START + 1}-{BATCH_START + len(files)}: {', '.join(namespaces)}")
    print(f"生成 {item_count} 个物品/方块要素、{entity_count} 个实体要素；跳过 {skipped} 个说明文本键。")


if __name__ == "__main__":
    main()
