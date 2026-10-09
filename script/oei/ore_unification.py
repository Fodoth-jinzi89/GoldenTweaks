import json
from pathlib import Path

MODID = "alltheores"

ORES = [
    "aluminum",
    "copper",
    "iron",
    "gold",
    "lead",
    "nickel",
    "silver",
    "tin",
    "uranium",
    "osmium",
    "zinc",
    "platinum",
    "iridium",
    "electrum",
    "steel",
    "bronze",
    "tungsten",
    "mithril",
    "constantan",
    "brass",
    "signalum",
    "lumium",
    "enderium",
    "invar"
]

GEMS = [
    "fluorite"
]

MEKANISM_METALS = {
    "osmium",
    "steel",
    "lead",
    "tin",
    "uranium"
}

MEKANISM_GEMS = {
    "fluorite"
}

CREATE_METALS = {
    "copper",
    "iron",
    "gold",
    "brass"
}

# Alloys have no ore / raw forms: never emit ore/deepslate/raw entries for them.
NO_ORE_METALS = {
    "brass",
    "bronze",
    "constantan",
    "electrum",
    "invar",
    "lumium",
    "signalum",
    "enderium",
    "steel",
}

# Canonical providers for categories AllTheOres does not register. Anything not
# listed here falls back to alltheores:<metal>_<kind>.
#   * vanilla metals (iron/copper/gold) -> minecraft
#   * tungsten -> neoecoae (nugget/plate come from northstar)
#   * mithril  -> irons_spellbooks (block comes from hazennstuff)
SPECIAL_RESULTS = {
    "iron": {
        "ore": "minecraft:iron_ore",
        "deepslate_ore": "minecraft:deepslate_iron_ore",
        "raw": "minecraft:raw_iron",
        "raw_block": "minecraft:raw_iron_block",
        "ingot": "minecraft:iron_ingot",
        "block": "minecraft:iron_block",
        "nugget": "minecraft:iron_nugget",
    },
    "copper": {
        "ore": "minecraft:copper_ore",
        "deepslate_ore": "minecraft:deepslate_copper_ore",
        "raw": "minecraft:raw_copper",
        "raw_block": "minecraft:raw_copper_block",
        "ingot": "minecraft:copper_ingot",
        "block": "minecraft:copper_block",
    },
    "gold": {
        "ore": "minecraft:gold_ore",
        "deepslate_ore": "minecraft:deepslate_gold_ore",
        "raw": "minecraft:raw_gold",
        "raw_block": "minecraft:raw_gold_block",
        "ingot": "minecraft:gold_ingot",
        "block": "minecraft:gold_block",
        "nugget": "minecraft:gold_nugget",
    },
    "tungsten": {
        "ore": "neoecoae:tungsten_ore",
        "deepslate_ore": "neoecoae:tungsten_ore",
        "raw": "neoecoae:raw_tungsten_ore",
        "raw_block": "neoecoae:raw_tungsten_block",
        "ingot": "neoecoae:tungsten_ingot",
        "block": "neoecoae:tungsten_block",
        "dust": "neoecoae:tungsten_dust",
        "nugget": "northstar:tungsten_nugget",
        "plate": "northstar:tungsten_sheet",
    },
    "mithril": {
        "ore": "irons_spellbooks:mithril_ore",
        "deepslate_ore": "irons_spellbooks:deepslate_mithril_ore",
        "raw": "irons_spellbooks:raw_mithril",
        "ingot": "irons_spellbooks:mithril_ingot",
        "block": "hazennstuff:mithril_block",
    },
}

# Materials whose full category set is defined by SPECIAL_RESULTS; any category
# missing from their entry is skipped instead of falling back to alltheores.
SPECIAL_ONLY = {"tungsten", "mithril"}

CATEGORY_KIND = {
    "ingots": "ingot",
    "nuggets": "nugget",
    "dusts": "dust",
    "plates": "plate",
    "rods": "rod",
    "gears": "gear",
    "storage_blocks": "block",
}


def resolve_tag(material, category):
    """
    matchItems（输入来源 tag）
    """

    return f"#c:{category}/{material}"


def resolve_core_item(material, kind):
    """
    ore / deepslate_ore / raw / raw_block 输出 item
    """

    special = SPECIAL_RESULTS.get(material, {})
    if kind in special:
        return special[kind]
    if material in SPECIAL_ONLY:
        return None
    if kind == "ore":
        return f"{MODID}:{material}_ore"
    if kind == "deepslate_ore":
        return f"{MODID}:deepslate_{material}_ore"
    if kind == "raw":
        return f"{MODID}:raw_{material}"
    return f"{MODID}:raw_{material}_block"


def resolve_result_item(material, category):
    """
    ingots / nuggets / dusts / plates / rods / gears / storage_blocks 输出 item
    """

    kind = CATEGORY_KIND[category]
    special = SPECIAL_RESULTS.get(material, {})
    if kind in special:
        return special[kind]
    if material in SPECIAL_ONLY:
        return None

    # plate 特殊：Create 优先
    if category == "plates" and material in CREATE_METALS:
        if material == "gold":
            return "create:golden_sheet"
        return f"create:{material}_sheet"

    if material in CREATE_METALS:
        if material == "brass" and category in {"ingots", "storage_blocks", "nuggets"}:
            if category == "storage_blocks":
                return "create:brass_block"
            singular = category[:-1]  # ingots -> ingot, nuggets -> nugget
            return f"create:brass_{singular}"

    # Mekanism 输出体系
    if material in MEKANISM_METALS and category in {"ingots", "nuggets", "dusts"}:
        return f"mekanism:{category[:-1]}_{material}"

    # 默认输出（本模组）
    if category == "storage_blocks":
        return f"{MODID}:{material}_block"
    return f"{MODID}:{material}_{category[:-1]}"


def gen_metal_entry(metal):
    entries = []

    if metal not in NO_ORE_METALS:
        for kind, category in (
            ("ore", ("ores", None)),
            ("deepslate_ore", None),
            ("raw", ("raw_materials", None)),
            ("raw_block", None),
        ):
            if kind == "ore":
                match = resolve_tag(metal, "ores")
            elif kind == "deepslate_ore":
                match = resolve_tag(metal, "ores").replace("ores/", "ores/deepslate_")
            elif kind == "raw":
                match = resolve_tag(metal, "raw_materials")
            else:
                match = f"#c:storage_blocks/raw_{metal}"

            result = resolve_core_item(metal, kind)
            if result is None:
                continue
            entries.append({
                "matchItems": [match],
                "resultItems": result
            })

    for category in ("ingots", "nuggets", "dusts", "plates", "rods", "gears", "storage_blocks"):
        result = resolve_result_item(metal, category)
        if result is None:
            continue
        entries.append({
            "matchItems": [resolve_tag(metal, category)],
            "resultItems": result
        })

    return entries


def gen_gem_entry(gem):
    entries = [{
        "matchItems": [resolve_tag(gem, "gems")],
        "resultItems": f"{MODID}:{gem}"
    }, {
        "matchItems": [resolve_tag(gem, "storage_blocks")],
        "resultItems": f"{MODID}:{gem}_block"
    }]

    return entries


def generate_all():
    result = []

    for metal in ORES:
        result.extend(gen_metal_entry(metal))

    for gem in GEMS:
        result.extend(gen_gem_entry(gem))

    return result


def main():
    data = generate_all()

    Path("ore_unification.json").write_text(
        json.dumps(data, indent=2, ensure_ascii=False),
        encoding="utf-8"
    )

    print(f"Generated {len(data)} entries")


if __name__ == "__main__":
    main()
