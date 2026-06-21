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
    "brass"
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


def resolve_tag(material: str, category: str):
    """
    只负责 matchItems（输入来源 tag）
    """

    return f"#c:{category}/{material}"


def resolve_result_item(material: str, category: str):
    """
    只负责输出 item（resultItems）
    """

    # plate 特殊：Create 优先
    if category == "plates" and material in CREATE_METALS:
        if material == "gold":
            return f"create:golden_sheet"
        return f"create:{material}_sheet"

    if material in CREATE_METALS:
        if material == "brass" and category in {"ingots", "storage_blocks", "nuggets"}:
            if category == "storage_blocks":
                return f"create:{material}_block"
            singular = category[:-1]  # ingots -> ingot, nuggets -> nugget
            return f"create:{material}_{singular}"

    # Mekanism 输出体系
    if material in MEKANISM_METALS and category in {"ingots", "nuggets", "dusts"}:
        if category == "storage_blocks":
            return f"mekanism:block_{material}"
        return f"mekanism:{category[:-1]}_{material}"

    # 默认输出（本模组）
    if category == "storage_blocks":
        return f"{MODID}:{material}_block"
    return f"{MODID}:{material}_{category[:-1]}"


def gen_metal_entry(metal: str):
    entries = [{
        "matchItems": [resolve_tag(metal, "ores")],
        "resultItems": f"{MODID}:{metal}_ore"
    }, {
        "matchItems": [
            resolve_tag(metal, "ores").replace("ores/", "ores/deepslate_")
        ],
        "resultItems": f"{MODID}:deepslate_{metal}_ore"
    }, {
        "matchItems": [resolve_tag(metal, "raw_materials")],
        "resultItems": f"{MODID}:raw_{metal}"
    }, {
        "matchItems": [f"#c:storage_blocks/raw_{metal}"],
        "resultItems": f"{MODID}:raw_{metal}_block"
    }, {
        "matchItems": [resolve_tag(metal, "ingots")],
        "resultItems": resolve_result_item(metal, "ingots")
    }, {
        "matchItems": [resolve_tag(metal, "nuggets")],
        "resultItems": resolve_result_item(metal, "nuggets")
    }, {
        "matchItems": [resolve_tag(metal, "dusts")],
        "resultItems": resolve_result_item(metal, "dusts")
    }, {
        "matchItems": [resolve_tag(metal, "plates")],
        "resultItems": resolve_result_item(metal, "plates")
    }, {
        "matchItems": [resolve_tag(metal, "rods")],
        "resultItems": resolve_result_item(metal, "rods")
    }, {
        "matchItems": [resolve_tag(metal, "gears")],
        "resultItems": resolve_result_item(metal, "gears")
    }, {
        "matchItems": [resolve_tag(metal, "storage_blocks")],
        "resultItems": resolve_result_item(metal, "storage_blocks")
    }]

    return entries


def gen_gem_entry(gem: str):
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