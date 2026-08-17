import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
INPUT_DIR = ROOT / "resource_locations" / "output"
OUTPUT_FILE = Path(__file__).with_name("output") / "materials.json"
EXCLUDED_NAMESPACES = {"allthecompressed", "jaopca", "silentgear", "silentgems"}

METALS = {
    "aluminum", "aluminium", "antimony", "brass", "bronze", "cast_iron", "chromium", "cobalt",
    "constantan", "copper", "electrum", "enderium", "gold", "invar", "iridium",
    "iron", "lead", "lumium", "magnesium", "manasteel", "mithril", "nickel",
    "netherite", "osmium", "palladium", "platinum", "refined_glowstone",
    "refined_obsidian", "rose_gold", "signalum", "silver", "steel", "tin",
    "titanium", "tungsten", "uranium", "zinc",
}
GEMS = {
    "agate", "alexandrite", "amethyst", "apatite", "aquamarine", "beryl", "carnelian",
    "citrine", "diamond", "emerald", "fluorite", "garnet", "jade", "jasper", "lapis",
    "malachite", "moonstone", "morganite", "onyx", "opal", "peridot", "quartz",
    "ruby", "sapphire", "spinel", "sunstone", "tanzanite", "topaz", "tourmaline",
    "turquoise", "zircon",
}
ROCK_WORDS = {
    "andesite", "basalt", "blackstone", "brimstone", "calcite", "chalk", "clay",
    "cobblestone", "deepslate", "diorite", "dolomite", "dripstone", "end_stone",
    "flint", "gabbro", "granite", "limestone", "marble", "mud", "netherrack",
    "obsidian", "pebbles", "pumice", "red_sand", "sand", "sandstone", "scoria",
    "shale", "slate", "soapstone", "stone", "travertine", "tuff",
}
FIBER_WORDS = {
    "canvas", "cloth", "cotton", "fabric", "fiber", "fibre", "flax", "hemp", "jute",
    "rope", "silk", "string", "thread", "wool",
}
ORGANIC_WORDS = {
    "apple", "bamboo", "barley", "bean", "beef", "beetroot", "berry", "blossom",
    "bone", "cabbage", "cactus", "carrot", "chicken", "cocoa", "coffee", "coral",
    "corn", "egg", "feather", "fern", "fish", "flower", "fungus", "garlic", "glow_berries",
    "grass", "hide", "honey", "kelp", "leather", "leaves", "meat", "melon", "moss",
    "mushroom", "mutton", "onion", "pork", "potato", "pumpkin", "rice", "root",
    "rotten_flesh", "sapling", "seaweed", "seeds", "slime", "sugar_cane", "tomato",
    "vine", "wheat", "wood", "yeast",
}

FORM_WORDS = {
    "block", "bricks", "brick", "button", "chiseled", "cluster", "compressed", "cut",
    "deepslate", "dirty", "door", "dust", "fence", "gate", "gear", "ingot", "lamp",
    "nugget", "ore", "plate", "planks", "polished", "raw", "rod", "shard", "slab",
    "small", "smooth", "stairs", "tiles", "wall",
}
WOOD_FORMS = ("_log", "_wood", "_stem", "_hyphae", "_planks")
ORGANIC_FORMS = ("_seeds", "_sapling", "_leaves", "_leaf", "_crop", "_root")


def words(value):
    return set(re.findall(r"[a-z0-9]+", value.lower().replace("-", "_")))


def contains_phrase(value, phrases):
    padded = f"_{value.lower().replace('-', '_')}_"
    return next((phrase for phrase in sorted(phrases, key=lambda item: (-len(item), item)) if f"_{phrase}_" in padded), None)


def canonical(value, category, matched=None):
    value = value.lower().replace("-", "_")
    value = re.sub(r"_[1-9]x$", "", value)
    value = re.sub(r"_0$", "", value)
    value = re.sub(r"^(block_of_|raw_|dirty_|deepslate_|nether_|end_|stripped_)", "", value)
    if category == "木头":
        value = re.sub(r"_(log|wood|stem|hyphae|planks|boat|chest_boat)$", "", value)
    elif category == "纤维":
        value = re.sub(r"_(fiber|fibre|thread|string|cloth|fabric|canvas|rope|wool)$", "", value)
    elif category == "有机物":
        value = re.sub(r"_?(seed_block|seeds|seed|sapling|leaves|leaf|crop|root)$", "", value)
        value = re.sub(r"^(raw_|uncooked_)", "", value)
    else:
        if category == "岩石":
            value = re.sub(r"^(chiseled_|cobbled_|cracked_|cut_|polished_|smooth_)", "", value)
        value = re.sub(r"_(block|bricks?|cluster|crystal|dust|gem|gear|ingot|nugget|ore|plate|rod|shard)$", "", value)
        value = re.sub(r"_(brick|bricks|tiles)$", "", value)
    value = re.sub(r"_(slab|stairs|wall|button|door|fence|fence_gate)$", "", value)
    return matched or value.strip("_")


def classify(path, name, source_type):
    value = path.lower().replace("-", "_")
    label = name.lower().replace("-", "_").replace(" ", "_")
    combined = f"{value}_{label}"
    token_set = words(combined)

    metal = contains_phrase(value, METALS)
    metal_form = r"(block|clump|crystal|dust|gear|ingot|nugget|ore|plate|raw|rod|shard|sheet)"
    if metal and (re.search(rf"(^|_){re.escape(metal)}_{metal_form}$", value) or re.search(rf"^(raw_|dirty_|deepslate_)?{re.escape(metal)}_ore$", value) or value == f"block_of_{metal}"):
        return "金属", canonical(value, "金属", metal)

    gem = contains_phrase(combined, GEMS)
    equipment = {"sword", "pickaxe", "axe", "shovel", "hoe", "helmet", "chestplate", "leggings", "boots", "knife", "bow", "shield", "armor", "tool"}
    machines = {"aggregator", "apothecary", "assembler", "casing", "chamber", "controller", "drive", "engine", "fixer", "furnace", "generator", "grid", "lamp", "machine", "matrix", "pedestal", "teleporter", "upgrade"}
    if gem and not token_set & (equipment | machines):
        return "宝石", canonical(value, "宝石", gem)
    if re.search(r"(^|_)(gem|crystal|shard)$", value) and not token_set & (equipment | machines | {"socket", "essence"}):
        return "宝石", canonical(value, "宝石")

    if source_type == "blocks" and (value.endswith(WOOD_FORMS) or token_set & {"log", "wood", "stem", "hyphae", "planks"}):
        if not token_set & {"attached", "machine", "casing", "framed", "copycat", "melon", "pumpkin"}:
            return "木头", canonical(value, "木头")

    rock = contains_phrase(combined, ROCK_WORDS)
    if rock and not token_set & (equipment | machines | {"pauldrons", "pressure", "copycat", "metal"}):
        return "岩石", canonical(value, "岩石", rock if rock not in {"stone", "sand"} else None)

    fiber = contains_phrase(value, FIBER_WORDS)
    if fiber and not token_set & {"bucket", "candy", "machine", "paint", "upgrade", "armor", "helmet", "chestplate", "leggings", "boots"}:
        return "纤维", canonical(value, "纤维", fiber if fiber not in {"fiber", "fibre"} else None)

    organic = contains_phrase(combined, ORGANIC_WORDS)
    if organic and not token_set & {"machine", "crate", "cabinet", "table", "chair", "door", "fence", "boat"}:
        return "有机物", canonical(value, "有机物", organic if organic not in {"leaves", "seeds", "sapling"} else None)
    if value.endswith(ORGANIC_FORMS):
        return "有机物", canonical(value, "有机物")
    return None


def score(entry, category):
    path = entry["id"].split(":", 1)[1]
    tokens = words(path)
    preferred = {
        "金属": ["ingot", "raw", "nugget", "dust", "block", "ore"],
        "宝石": ["gem", "crystal", "shard", "dust", "block", "ore"],
        "木头": ["log", "stem", "hyphae", "wood", "planks"],
        "岩石": ["stone", "rock", "cobblestone", "sand", "clay", "block"],
        "纤维": ["fiber", "fibre", "string", "thread", "silk", "wool", "cloth", "fabric"],
        "有机物": ["raw", "crop", "root", "leaves", "sapling", "seeds"],
    }[category]
    form_rank = next((index for index, form in enumerate(preferred) if form in tokens), len(preferred))
    material = entry.get("material", "")
    exact_form_rank = 0 if path == material or any(path == f"{material}_{form}" for form in preferred) else 1
    namespace_rank = 0 if entry["id"].startswith("minecraft:") else 1
    type_rank = 0 if entry["source_type"] == "items" else 1
    processed_penalty = len(tokens & FORM_WORDS)
    compressed_penalty = 1 if re.search(r"_[1-9]x$", path) else 0
    return exact_form_rank, namespace_rank, form_rank, compressed_penalty, type_rank, processed_penalty, len(path), path


def main():
    grouped = {category: {} for category in ("木头", "岩石", "纤维", "宝石", "金属", "有机物")}
    scanned = 0
    for file in sorted(INPUT_DIR.glob("*.json")):
        data = json.loads(file.read_text(encoding="utf-8"))
        for source_type in ("items", "blocks"):
            for resource_id, name in data.get(source_type, {}).items():
                scanned += 1
                if resource_id.split(":", 1)[0] in EXCLUDED_NAMESPACES:
                    continue
                path = resource_id.split(":", 1)[-1]
                if "." in path or "%s" in name:
                    continue
                result = classify(path, name, source_type)
                if not result:
                    continue
                category, material = result
                entry = {"id": resource_id, "name": name, "source_type": source_type, "material": material}
                grouped[category].setdefault(material, []).append(entry)

    output = {"meta": {"source_json_count": len(list(INPUT_DIR.glob('*.json'))), "scanned_entries": scanned}}
    for category, materials in grouped.items():
        selected = []
        for material, entries in materials.items():
            entries.sort(key=lambda entry: score(entry, category))
            representative = dict(entries[0])
            representative["material"] = material
            representative["merged_count"] = len(entries)
            selected.append(representative)
        selected.sort(key=lambda entry: (entry["material"], entry["id"]))
        output[category] = selected
        output["meta"][f"{category}_count"] = len(selected)

    OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_FILE.write_text(json.dumps(output, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    assert all(entry["merged_count"] >= 1 for category in grouped for entry in output[category])
    assert len({entry["material"] for entry in output["金属"]}) == len(output["金属"])
    print(f"Wrote {OUTPUT_FILE}")
    for category in grouped:
        print(f"{category}: {len(output[category])}")


if __name__ == "__main__":
    main()
