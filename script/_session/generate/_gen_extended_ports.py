"""Generate the assets for the chemical hopper and the essentia tank.

2 kinds x 2 modes x 15 tiers = 60 blocks, each needing a block model, blockstate, item model
and loot table, plus four language keys per block and tag membership.
"""
import io
import json
import os
import shutil

ROOT = r"src/main/resources"
LANG = os.path.join(ROOT, "assets/goldentweaks/lang")
ARTS = r"arts/multiblock/port"
TEX = os.path.join(ROOT, "assets/goldentweaks/textures/block/multiblock/port")

# Our own icon sheets, copied from the art folder.
ICONS = {"chemical": "chemical", "essentia": "essentia"}

TIERS = [
    ("ulv", "ULV"), ("lv", "LV"), ("mv", "MV"), ("hv", "HV"), ("ev", "EV"),
    ("iv", "IV"), ("luv", "LuV"), ("zpm", "ZPM"), ("uv", "UV"), ("uhv", "UHV"),
    ("uev", "UEV"), ("uiv", "UIV"), ("umv", "UMV"), ("uxv", "UXV"), ("max", "MAX"),
]

KINDS = {
    # kind id: (zh input, zh output, en input, en output)
    "chemical": ("输入料斗", "输出料斗", "Input Hopper", "Output Hopper"),
    "essentia": ("输入罐", "输出罐", "Input Tank", "Output Tank"),
}
MODES = {"input": 0, "output": 1}

ROTATIONS = {
    "north": {}, "east": {"y": 90}, "south": {"y": 180},
    "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90},
}

RING = "goldentweaks:block/multiblock/port/level/{mode}"


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def main():
    os.makedirs(TEX, exist_ok=True)
    for kind, sheet in ICONS.items():
        for mode in MODES:
            src = os.path.join(ARTS, mode, sheet + ".png")
            dst = os.path.join(TEX, "%s_%s.png" % (mode, kind))
            if os.path.exists(src):
                shutil.copyfile(src, dst)
                print("icon:", dst.replace(ROOT + "\\", ""))

    made = 0
    zh = {}
    en = {}

    for kind, names in KINDS.items():
        for mode, name_index in MODES.items():
            base = "gt_port_%s_%s" % (kind, mode)

            for tier_id, code in TIERS:
                write(
                    "%s/assets/goldentweaks/models/block/multiblock/%s_%s.json"
                    % (ROOT, base, tier_id),
                    {
                        "parent": "goldentweaks:block/multiblock/gt_port_base",
                        "textures": {
                            "icon": "goldentweaks:block/multiblock/port/%s_%s" % (mode, kind),
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

                zh["block.goldentweaks.%s_%s" % (base, tier_id)] = code + names[name_index]
                en["block.goldentweaks.%s_%s" % (base, tier_id)] = (
                    code + " " + names[name_index + 2])

    for path, table in ((os.path.join(LANG, "zh_cn.json"), zh),
                        (os.path.join(LANG, "en_us.json"), en)):
        with io.open(path, encoding="utf-8") as handle:
            data = json.load(handle)
        data.update(table)
        with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
            json.dump(data, handle, indent=2, ensure_ascii=False)
            handle.write("\n")

    print("wrote %d files, %d language keys" % (made, len(zh)))


main()
