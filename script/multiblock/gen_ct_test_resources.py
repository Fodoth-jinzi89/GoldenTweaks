"""Write the blockstate, block model and item model for every CT layout test casing.

Each test casing is a plain cube whose texture is the layout's own sheet, so the only thing that varies
between them is which sheet is named. Writing them from one list keeps the three files per casing in
step — a mismatch between blockstate and model shows up as a missing texture with no explanation.

    python script/multiblock/gen_ct_test_resources.py
"""
import io
import json
import os

ASSETS = os.path.join("src", "main", "resources", "assets", "goldentweaks")

LAYOUTS = [
    "horizontal", "horizontal_kryppers", "vertical", "omnidirectional",
    "roof", "roof_stair", "cross", "rectangle",
]
MATERIALS = ["pe", "pp"]


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def main():
    names = []
    for material in MATERIALS:
        for layout in LAYOUTS:
            names.append(f"ct_test_{material}_{layout}")
        # The two "auto" variants: the block names no layout, so it is read from the sheet's size.
        names.append(f"ct_test_{material}_auto_omni")
        names.append(f"ct_test_{material}_auto_rect")

    for name in names:
        write_json(os.path.join(ASSETS, "blockstates", f"{name}.json"),
                   {"variants": {"": {"model": f"goldentweaks:block/{name}"}}})

        write_json(os.path.join(ASSETS, "models", "block", f"{name}.json"),
                   {"parent": "minecraft:block/cube_all",
                    "textures": {"all": f"goldentweaks:block/{name}"}})

        write_json(os.path.join(ASSETS, "models", "item", f"{name}.json"),
                   {"parent": f"goldentweaks:block/{name}"})

    print(f"  wrote {len(names)} test casings x 3 files")
    for name in names:
        print(f"    {name}")
    return 0


if __name__ == "__main__":
    main()
