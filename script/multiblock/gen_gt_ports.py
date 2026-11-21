"""Generate blockstates, models, item models, loot tables and tags for GT multiblock ports."""
import json
import os

ROOT = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\resources"

KINDS = ["item", "fluid", "energy"]
MODES = ["input", "output"]
TIERS = list(range(1, 10))

ROTATIONS = {
    "north": {},
    "east": {"y": 90},
    "south": {"y": 180},
    "west": {"y": 270},
    "up": {"x": 270},
    "down": {"x": 90},
}

BASE_MODEL = {
    "parent": "minecraft:block/block",
    "render_type": "minecraft:cutout",
    "textures": {
        "shell": "goldentweaks:block/shell_pe",
        "particle": "goldentweaks:block/shell_pe",
    },
    "elements": [
        {
            "from": [0, 0, 0],
            "to": [16, 16, 16],
            "faces": {
                "down": {"uv": [0, 0, 16, 16], "texture": "#shell", "cullface": "down"},
                "up": {"uv": [0, 16, 16, 0], "texture": "#shell", "cullface": "up"},
                "north": {"uv": [0, 0, 16, 16], "texture": "#shell", "cullface": "north"},
                "south": {"uv": [0, 0, 16, 16], "texture": "#shell", "cullface": "south"},
                "west": {"uv": [0, 0, 16, 16], "texture": "#shell", "cullface": "west"},
                "east": {"uv": [0, 0, 16, 16], "texture": "#shell", "cullface": "east"},
            },
        },
        {
            "from": [0, 0, -0.01],
            "to": [16, 16, 0],
            "faces": {"north": {"uv": [0, 0, 16, 16], "texture": "#icon"}},
        },
        {
            "from": [0, 0, -0.02],
            "to": [16, 16, -0.01],
            "faces": {"north": {"uv": [0, 0, 16, 16], "texture": "#ring"}},
        },
    ],
}


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def main():
    made = 0
    for kind_id in KINDS:
        for mode in MODES:
            block = "gt_port_%s_%s" % (kind_id, mode)

            for tier in TIERS:
                write(r"%s\assets\goldentweaks\models\block\multiblock\%s_t%d.json" % (
                    ROOT, block, tier), {
                    "parent": "goldentweaks:block/multiblock/gt_port_base",
                    "textures": {
                        "icon": "avaritia_more_items:block/multiblock_port/input/%s" % kind_id,
                        "ring": "avaritia_more_items:block/multiblock_port/level/%s/%d" % (
                            mode, tier),
                    },
                })
                made += 1

            variants = {}
            for facing, rotation in ROTATIONS.items():
                for tier in TIERS:
                    entry = {"model": "goldentweaks:block/multiblock/%s_t%d" % (block, tier)}
                    entry.update(rotation)
                    variants["facing=%s,tier=%d" % (facing, tier)] = entry
            write(r"%s\assets\goldentweaks\blockstates\%s.json" % (ROOT, block),
                  {"variants": variants})
            made += 1

            write(r"%s\assets\goldentweaks\models\item\%s.json" % (ROOT, block),
                  {"parent": "goldentweaks:block/multiblock/%s_t1" % block})
            made += 1

            write(r"%s\data\goldentweaks\loot_table\blocks\%s.json" % (ROOT, block), {
                "type": "minecraft:block",
                "pools": [{
                    "rolls": 1.0,
                    "bonus_rolls": 0.0,
                    "entries": [{"type": "minecraft:item", "name": "goldentweaks:%s" % block}],
                    "conditions": [{"condition": "minecraft:survives_explosion"}],
                }],
            })
            made += 1

    write(r"%s\assets\goldentweaks\models\block\multiblock\gt_port_base.json" % ROOT, BASE_MODEL)
    made += 1

    for kind_id in KINDS:
        for mode in MODES:
            write(r"%s\data\c\tags\block\multiblock\%s\%s.json" % (ROOT, mode, kind_id), {
                "replace": False,
                "values": ["goldentweaks:gt_port_%s_%s" % (kind_id, mode)],
            })
            made += 1

    print("wrote %d files" % made)


if __name__ == "__main__":
    main()
