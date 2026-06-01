import json
from pathlib import Path

INPUT_DIR = Path("./cluster")
OUTPUT_DIR = Path("./raw_crystal")

OUTPUT_DIR.mkdir(parents=True, exist_ok=True)


def make_raw_name(item_id: str) -> str:
    """
    spectrum:pure_azurite -> spectrum:raw_azurite
    spectrum:bismuth_crystal -> spectrum:raw_bismuth
    """

    namespace, path = item_id.split(":", 1)

    if path.startswith("pure_"):
        material = path[5:]
    elif path.endswith("_crystal"):
        material = path[:-8]
    else:
        material = path

    return f"{namespace}:raw_{material}"


for file in INPUT_DIR.glob("*.json"):
    try:
        with open(file, "r", encoding="utf-8") as f:
            recipe = json.load(f)

        pure_item = recipe["results"][0]["id"]
        raw_item = make_raw_name(pure_item)

        generated = {
            "neoforge:conditions": [
                {
                    "type": "neoforge:mod_loaded",
                    "modid": "spectrum"
                }
            ],
            "type": "create:crushing",
            "ingredients": [
                {
                    "type": "neoforge:compound",
                    "ingredients": [
                        {
                            "item": pure_item
                        }
                    ]
                }
            ],
            "processing_time": 25,
            "results": [
                {
                    "count": 1,
                    "id": raw_item
                }
            ]
        }

        output_file = OUTPUT_DIR / raw_item.split(":")[1]
        output_file = output_file.with_suffix(".json")

        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(generated, f, ensure_ascii=False, indent=2)

        print(f"[OK] {output_file.name}")

    except Exception as e:
        print(f"[ERROR] {file}: {e}")