"""Register every CT test sheet in the block atlas.

A `_ctm` sheet is never referenced by a model, so the atlas does not pick it up on its own — it has to
be listed explicitly. Missing an entry does not fail loudly: the sprite resolves to the missing-texture
checker and the casing draws as magenta and black, which looks like a CT bug rather than a listing bug.

    python script/multiblock/register_ct_test_atlas.py
"""
import io
import json
import os

ATLAS = os.path.join("src", "main", "resources", "assets", "goldentweaks", "atlases", "blocks.json")

LAYOUTS = [
    "horizontal", "horizontal_kryppers", "vertical", "omnidirectional",
    "roof", "roof_stair", "cross", "rectangle",
]
MATERIALS = ["pe", "pp"]


def main():
    with io.open(ATLAS, encoding="utf-8") as handle:
        data = json.load(handle)

    existing = {entry["resource"] for entry in data["sources"]
                if entry.get("type") == "minecraft:single"}

    added = 0
    for material in MATERIALS:
        names = [f"ct_test_{material}_{layout}" for layout in LAYOUTS]
        names += [f"ct_test_{material}_auto_omni", f"ct_test_{material}_auto_rect"]
        for name in names:
            resource = f"goldentweaks:block/{name}_ctm"
            if resource in existing:
                continue
            data["sources"].append({"type": "minecraft:single", "resource": resource})
            existing.add(resource)
            added += 1

    with io.open(ATLAS, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")

    print(f"  added {added} entries, {len(data['sources'])} total")
    return 0


if __name__ == "__main__":
    main()
