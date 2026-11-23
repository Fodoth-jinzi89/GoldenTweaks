"""Add the six tiers above UV.

Each tier is its own block and item, so each needs a block model, a blockstate, an item
model, a loot table, four language keys and three tag memberships. Everything here mirrors
what the existing UV entries look like — this only fills in the new ids.
"""
import io
import json
import os

ROOT = r"src/main/resources"
LANG = os.path.join(ROOT, "assets/goldentweaks/lang")

KINDS = {
    "item": ("输入总线", "输出总线", "Input Bus", "Output Bus"),
    "fluid": ("输入仓", "输出仓", "Input Hatch", "Output Hatch"),
    "energy": ("能源仓", "动力仓", "Energy Hatch", "Dynamo Hatch"),
}
MODES = {"input": 0, "output": 1}

# id, display code — the six new rungs, in ladder order.
NEW_TIERS = [
    ("uhv", "UHV"),
    ("uev", "UEV"),
    ("uiv", "UIV"),
    ("umv", "UMV"),
    ("uxv", "UXV"),
    ("max", "MAX"),
]

ROTATIONS = {
    "north": {},
    "east": {"y": 90},
    "south": {"y": 180},
    "west": {"y": 270},
    "up": {"x": 270},
    "down": {"x": 90},
}

# The same grey sheets every tier uses; the colour comes from the tint at draw time.
RING = "goldentweaks:block/multiblock/port/level/{mode}"
ICON = "avaritia_more_items:block/multiblock_port/input/{kind}"


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def main():
    made = 0
    lang = {"zh": {}, "en": {}}

    for kind, names in KINDS.items():
        for mode, name_index in MODES.items():
            base = "gt_port_%s_%s" % (kind, mode)

            for tier_id, _code in NEW_TIERS:
                write(
                    "%s/assets/goldentweaks/models/block/multiblock/%s_%s.json"
                    % (ROOT, base, tier_id),
                    {
                        "parent": "goldentweaks:block/multiblock/gt_port_base",
                        "textures": {
                            "icon": ICON.format(kind=kind),
                            "ring": RING.format(mode=mode),
                        },
                    },
                )
                made += 1

                write(
                    "%s/assets/goldentweaks/blockstates/%s_%s.json" % (ROOT, base, tier_id),
                    {"variants": {
                        "facing=%s" % facing: dict(
                            {"model": "goldentweaks:block/multiblock/%s_%s" % (base, tier_id)},
                            **rotation)
                        for facing, rotation in ROTATIONS.items()
                    }},
                )
                made += 1

                write(
                    "%s/assets/goldentweaks/models/item/%s_%s.json" % (ROOT, base, tier_id),
                    {"parent": "goldentweaks:block/multiblock/%s_%s" % (base, tier_id)},
                )
                made += 1

                write(
                    "%s/data/goldentweaks/loot_table/blocks/%s_%s.json" % (ROOT, base, tier_id),
                    {
                        "type": "minecraft:block",
                        "pools": [{
                            "rolls": 1.0,
                            "bonus_rolls": 0.0,
                            "entries": [{
                                "type": "minecraft:item",
                                "name": "goldentweaks:%s_%s" % (base, tier_id),
                            }],
                            "conditions": [{"condition": "minecraft:survives_explosion"}],
                        }],
                    },
                )
                made += 1

                # The full name is written as two parts: GTPortItem colours the code itself, so
                # only the part name lives in the language file.
                lang["zh"]["block.goldentweaks.%s_%s" % (base, tier_id)] = (
                    _code + names[name_index])
                lang["en"]["block.goldentweaks.%s_%s" % (base, tier_id)] = (
                    _code + " " + names[name_index + 2])

    for language, table in lang.items():
        path = os.path.join(LANG, "%s_us.json" % language if language == "en" else "zh_cn.json")
        with io.open(path, encoding="utf-8") as handle:
            data = json.load(handle)
        data.update(table)
        with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
            json.dump(data, handle, indent=2, ensure_ascii=False)
            handle.write("\n")

    print("wrote %d files, %d language keys" % (made, len(lang["zh"])))


main()
