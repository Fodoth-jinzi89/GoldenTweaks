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
    "brass",
    "tungsten",
    "mithril",
    "constantan"
]

# Gems
GEMS = [
    "fluorite"
]

# 使用 Mekanism tag 的金属
MEKANISM_METALS = {
    "osmium",
    "steel",
    "lead",
    "tin",
    "copper",
    "uranium"
}

# 使用 Mekanism tag 的宝石
MEKANISM_GEMS = {
    "fluorite"
}


def resolve_tag(material: str, category: str):
    """
    金属:
        ores / raw_materials / ingots / nuggets / dusts / storage_blocks

    宝石:
        gems / storage_blocks
    """

    # gems
    if category == "gems":
        if material in MEKANISM_GEMS:
            return f"#mekanism:gems/{material}"
        return f"#c:gems/{material}"

    # storage block for gems
    if category == "storage_blocks" and material in MEKANISM_GEMS:
        return f"#mekanism:storage_blocks/{material}"

    # metals
    if material in MEKANISM_METALS:
        return f"#mekanism:{category}/{material}"

    return f"#c:{category}/{material}"


def gen_metal_entry(metal: str):
    entries = []

    # ore
    entries.append({
        "matchItems": [resolve_tag(metal, "ores")],
        "resultItems": f"{MODID}:{metal}_ore"
    })

    # deepslate ore
    entries.append({
        "matchItems": [
            resolve_tag(metal, "ores").replace(
                "ores/",
                "ores/deepslate_"
            )
        ],
        "resultItems": f"{MODID}:deepslate_{metal}_ore"
    })

    # raw
    entries.append({
        "matchItems": [resolve_tag(metal, "raw_materials")],
        "resultItems": f"{MODID}:raw_{metal}"
    })

    # raw block
    entries.append({
        "matchItems": [f"#c:storage_blocks/raw_{metal}"],
        "resultItems": f"{MODID}:raw_{metal}_block"
    })

    # ingot
    entries.append({
        "matchItems": [resolve_tag(metal, "ingots")],
        "resultItems": f"{MODID}:{metal}_ingot"
    })

    # nugget
    entries.append({
        "matchItems": [resolve_tag(metal, "nuggets")],
        "resultItems": f"{MODID}:{metal}_nugget"
    })

    # dust
    entries.append({
        "matchItems": [resolve_tag(metal, "dusts")],
        "resultItems": f"{MODID}:{metal}_dust"
    })

    # block
    entries.append({
        "matchItems": [resolve_tag(metal, "storage_blocks")],
        "resultItems": f"{MODID}:{metal}_block"
    })

    return entries


def gen_gem_entry(gem: str):
    entries = []

    # gem
    entries.append({
        "matchItems": [resolve_tag(gem, "gems")],
        "resultItems": f"{MODID}:{gem}"
    })

    # storage block
    entries.append({
        "matchItems": [resolve_tag(gem, "storage_blocks")],
        "resultItems": f"{MODID}:{gem}_block"
    })

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
