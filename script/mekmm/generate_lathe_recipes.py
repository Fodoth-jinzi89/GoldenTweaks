import json
from pathlib import Path

MODID = "alltheores"

METALS = [
    "lead",
    "tin",
    "steel",
    "osmium",
    "uranium",
    "invar",
    "brass",
    "enderium",
    "lumium",
    "signalum",
    "copper",
    "diamond",
    "netherite"

]

OUTPUT_DIR = Path("generated_lathe_recipes")
OUTPUT_DIR.mkdir(exist_ok=True)

for metal in METALS:
    recipe = {
        "neoforge:conditions": [
            {
                "type": "neoforge:mod_loaded",
                "modid": MODID
            }
        ],
        "type": "mekmm:lathe",
        "input": {
            "count": 1,
            "tag": f"c:ingots/{metal}"
        },
        "output": {
            "count": 2,
            "id": f"{MODID}:{metal}_rod"
        }
    }

    output_file = OUTPUT_DIR / f"{metal}_rod.json"

    with output_file.open("w", encoding="utf-8") as f:
        json.dump(recipe, f, ensure_ascii=False, indent=2)

print("合成表生成完成。")
