"""Generate the blockstates, models, loot tables and tags for the general IO assemblies.

The general assembly is a new port kind, so its resources follow the same shape the
existing ports use: one blockstate per tier, one model per tier that names the icon and
the tier ring, an item model, a loot table, and the two tag lists.

Naming follows the live scheme (`gt_port_<kind>_<mode>_<tier.id>`), not the older
`_t1.._t9` files that predate the extended ladder.
"""
import json
import os

ROOT = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\resources"

KIND = "general"
MODES = ["input", "output"]
TIERS = ["ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv",
         "uhv", "uev", "uiv", "umv", "uxv", "max"]

# How many plates a port can show; mirrors GTMultiblockBlocks.CASINGS.
CASINGS = 3

ROTATIONS = {
    "north": {},
    "east": {"y": 90},
    "south": {"y": 180},
    "west": {"y": 270},
    "up": {"x": 270},
    "down": {"x": 90},
}

# Casing 0 is the default and is listed for all six faces. The others only ever appear on a
# vertical wall, so they carry the four horizontal facings — the same shape the shipped port
# blockstates use.
VERTICAL_FACINGS = ["north", "east", "south", "west"]


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def main():
    made = 0
    for mode in MODES:
        block = "gt_port_%s_%s" % (KIND, mode)

        for tier in TIERS:
            write(r"%s\assets\goldentweaks\models\block\multiblock\%s_%s.json" % (
                ROOT, block, tier), {
                "parent": "goldentweaks:block/multiblock/gt_port_base",
                "textures": {
                    "icon": "goldentweaks:block/multiblock/port/%s_%s" % (mode, KIND),
                    "ring": "goldentweaks:block/multiblock/port/level/%s" % mode,
                },
            })
            made += 1

        # Each tier is its own block, so each gets its own blockstate pointing at its own model.
        # The state carries `casing` (which plate the port shows) and `facing`.
        for tier in TIERS:
            variants = {}
            for facing, rotation in ROTATIONS.items():
                entry = {"model": "goldentweaks:block/multiblock/%s_%s" % (block, tier)}
                entry.update(rotation)
                variants["casing=0,facing=%s" % facing] = entry
            for casing in range(1, CASINGS):
                for facing in VERTICAL_FACINGS:
                    entry = {"model": "goldentweaks:block/multiblock/%s_%s" % (block, tier)}
                    entry.update(ROTATIONS[facing])
                    variants["casing=%d,facing=%s" % (casing, facing)] = entry
            write(r"%s\assets\goldentweaks\blockstates\%s_%s.json" % (ROOT, block, tier),
                  {"variants": variants})
            made += 1

        for tier in TIERS:
            write(r"%s\assets\goldentweaks\models\item\%s_%s.json" % (ROOT, block, tier),
                  {"parent": "goldentweaks:block/multiblock/%s_%s" % (block, tier)})
            made += 1

        # One loot table per tier block: each tier is its own block and drops its own item.
        for tier in TIERS:
            write(r"%s\data\goldentweaks\loot_table\blocks\%s_%s.json" % (ROOT, block, tier), {
                "type": "minecraft:block",
                "pools": [{
                    "rolls": 1.0,
                    "bonus_rolls": 0.0,
                    "entries": [{
                        "type": "minecraft:item",
                        "name": "goldentweaks:%s_%s" % (block, tier),
                    }],
                    "conditions": [{"condition": "minecraft:survives_explosion"}],
                }],
            })
            made += 1

        # The common tag lists every tier of this mode under one entry.
        write(r"%s\data\c\tags\block\multiblock\%s\%s.json" % (ROOT, mode, KIND), {
            "replace": False,
            "values": ["goldentweaks:%s_%s" % (block, tier) for tier in TIERS],
        })
        made += 1

    print("wrote %d files" % made)


if __name__ == "__main__":
    main()
