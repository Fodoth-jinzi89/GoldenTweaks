import json
from pathlib import Path

MODID = "alltheores"

METALS = [
    "electrum",
    "zinc",
    "aluminum",
    "lead",
    "nickel",
    "osmium",
    "platinum",
    "silver",
    "tin",
    "uranium",
    "iridium",
    "steel",
    "invar",
    "bronze",
    "brass",
    "enderium",
    "lumium",
    "signalum",
    "constantan",
    "copper",
    "iron",
    "gold",
    "diamond",
    "netherite"
]

OUTPUT_DIR = Path("generated_rolling_mill_recipes")
OUTPUT_DIR.mkdir(exist_ok=True)

for metal in METALS:
    recipe = {
        "neoforge:conditions": [
            {
                "type": "neoforge:mod_loaded",
                "modid": MODID
            }
        ],
        "type": "mekmm:rolling_mill",
        "input": {
            "count": 1,
            "tag": f"c:ingots/{metal}"
        },
        "output": {
            "count": 1,
            "id": f"{MODID}:{metal}_plate"
        }
    }

    # 特殊处理
    if metal == "diamond":
        recipe["input"]["tag"] = "c:gems/diamond"

    elif metal == "netherite":
        recipe["input"]["tag"] = "c:ingots/netherite"

    output_file = OUTPUT_DIR / f"{metal}_plate.json"

    with output_file.open("w", encoding="utf-8") as f:
        json.dump(recipe, f, ensure_ascii=False, indent=2)

print("Rolling Mill 合成表生成完成。")
