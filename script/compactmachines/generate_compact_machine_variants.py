from copy import deepcopy
from pathlib import Path
import json
import shutil

import nbtlib
from nbtlib import Int, List


ROOT = Path(__file__).resolve().parent.parent
SOURCE_DIR = ROOT / "sxript"
STRUCTURE_DIR = ROOT / "src/main/resources/data/goldentweaks/structure/compactmachines/floors"
TEMPLATE_DIR = ROOT / "src/main/resources/data/goldentweaks/compactmachines/room_templates"
RECIPE_DIR = ROOT / "src/main/resources/data/goldentweaks/recipe/compactmachines/variants"

SKIES = {
    "false_sky": {
        "color": "#87CEEB",
        "essence": "thaumcraftcelestial:solar_essence",
    },
    "starry_sky_night": {
        "color": "#17172E",
        "essence": "thaumcraftcelestial:stellar_essence",
    },
}


def translated_item(item_id: str) -> dict:
    namespace, path = item_id.split(":", 1)
    prefix = "item" if namespace == "thaumcraftcelestial" else "block"
    return {"translate": f"{prefix}.{namespace}.{path}", "color": "aqua", "italic": False}


def lore_line(label: str, item_id: str) -> dict:
    return {
        "text": "",
        "italic": False,
        "extra": [
            {"translate": f"tooltip.goldentweaks.compactmachines.{label}", "color": "gold", "italic": False},
            translated_item(item_id),
        ],
    }


def merge_floor(source_path: Path, output_path: Path) -> list[str]:
    structure = nbtlib.load(source_path)
    if [int(value) for value in structure["size"]] != [16, 1, 16]:
        raise ValueError(f"{source_path.name} is not 16x1x16")

    materials = [str(structure["palette"][index]["Name"]) for index in range(4)]
    original_blocks = list(structure["blocks"])
    tiled_blocks = []
    for tile_x in range(4):
        for tile_z in range(4):
            for original in original_blocks:
                block = deepcopy(original)
                block["pos"] = List[Int]([
                    int(original["pos"][0]) + tile_x * 16,
                    int(original["pos"][1]),
                    int(original["pos"][2]) + tile_z * 16,
                ])
                tiled_blocks.append(block)

    structure["size"] = List[Int]([64, 1, 64])
    structure["blocks"] = List[type(structure["blocks"][0])] (tiled_blocks)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    structure.save(output_path, gzipped=True)
    return materials


def component_ingredient(item_id: str, room_template: str) -> dict:
    return {
        "type": "neoforge:components",
        "items": item_id,
        "components": {"compactmachines:room_template": room_template},
    }


def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    sources = sorted(SOURCE_DIR.glob("floor_*.nbt"))
    if len(sources) != 48:
        raise ValueError(f"Expected 48 floor NBT files, found {len(sources)}")

    for directory in (STRUCTURE_DIR, RECIPE_DIR):
        if directory.exists():
            shutil.rmtree(directory)
    TEMPLATE_DIR.mkdir(parents=True, exist_ok=True)
    for old in (TEMPLATE_DIR / "false_sky.json", TEMPLATE_DIR / "starry_night.json", TEMPLATE_DIR / "starry_sky_night.json"):
        old.unlink(missing_ok=True)

    for index, source in enumerate(sources, 1):
        floor_id = f"floor_{index:04d}"
        materials = merge_floor(source, STRUCTURE_DIR / f"{floor_id}.nbt")

        for sky_id, sky in SKIES.items():
            variant_id = f"{sky_id}_{floor_id}"
            write_json(TEMPLATE_DIR / f"{variant_id}.json", {
                "color": sky["color"],
                "dimensions": {"depth": 64, "height": 64, "width": 64},
                "structures": [{
                    "template": f"goldentweaks:compactmachines/floors/{floor_id}",
                    "placement": "centered_floor",
                }],
            })

            ingredients = [
                component_ingredient("compactmachines:new_machine", "compactmachines:soaryn"),
                {"item": sky["essence"]},
                *({"item": material} for material in materials),
            ]
            lore = [
                lore_line("sky", sky["essence"]),
                lore_line("corner", materials[0]),
                lore_line("edge", materials[1]),
                lore_line("floor", materials[2]),
                lore_line("light", materials[3]),
            ]
            write_json(RECIPE_DIR / f"{variant_id}.json", {
                "type": "minecraft:crafting_shapeless",
                "category": "misc",
                "ingredients": ingredients,
                "result": {
                    "id": "compactmachines:new_machine",
                    "count": 1,
                    "components": {
                        "compactmachines:machine_color": sky["color"],
                        "compactmachines:room_template": f"goldentweaks:{variant_id}",
                        "minecraft:custom_name": {
                            "translate": "machine.goldentweaks.variant",
                            "italic": False,
                        },
                        "minecraft:lore": lore,
                    },
                },
            })

    print(f"Generated {len(sources)} tiled structures and {len(sources) * len(SKIES)} machine variants")


if __name__ == "__main__":
    main()
