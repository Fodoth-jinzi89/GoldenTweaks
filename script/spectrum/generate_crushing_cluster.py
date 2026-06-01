import json
from pathlib import Path

INPUT_DIR = Path("./blocks")
OUTPUT_DIR = Path("./generated_crushing")

OUTPUT_DIR.mkdir(parents=True, exist_ok=True)


def find_drop_item(data):
    """
    从 alternatives 中找到非 cluster 的掉落物
    """

    for pool in data.get("pools", []):
        for entry in pool.get("entries", []):

            if entry.get("type") != "minecraft:alternatives":
                continue

            for child in entry.get("children", []):

                name = child.get("name", "")

                if not name.endswith("_cluster"):
                    return name

    return None


for file in INPUT_DIR.rglob("*_cluster.json"):

    try:
        with open(file, "r", encoding="utf-8") as f:
            loot = json.load(f)

        cluster_item = f"spectrum:{file.stem}"

        drop_item = find_drop_item(loot)

        if drop_item is None:
            print(f"[SKIP] {file}")
            continue

        recipe = {
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
                            "item": cluster_item
                        }
                    ]
                }
            ],
            "processing_time": 200,
            "results": [
                {
                    "count": 8,
                    "id": drop_item
                }
            ]
        }

        output_file = OUTPUT_DIR / file.name

        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(recipe, f, ensure_ascii=False, indent=2)

        print(f"[OK] {file.name}")

    except Exception as e:
        print(f"[ERROR] {file}: {e}")