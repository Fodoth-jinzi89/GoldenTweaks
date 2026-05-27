import json
from pathlib import Path

OUTPUT_DIR = Path("./generated_recipes")

COLORS = [
    "white",
    "orange",
    "magenta",
    "light_blue",
    "yellow",
    "lime",
    "pink",
    "gray",
    "light_gray",
    "cyan",
    "purple",
    "blue",
    "brown",
    "green",
    "red",
    "black"
]

OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

for color in COLORS:
    sapling_id = f"spectrum:{color}_sapling"
    log_id = f"spectrum:{color}_log"
    pigment_id = f"spectrum:{color}_pigment"

    recipe = {
        "neoforge:conditions": [
            {
                "type": "neoforge:mod_loaded",
                "modid": "spectrum"
            }
        ],
        "type": "mekmm:planting",
        "item_input": {
            "count": 1,
            "item": sapling_id
        },
        "chemical_input": {
            "amount": 1,
            "chemical": "mekmm:nutrient_solution"
        },
        "main_output": {
            "count": 6,
            "id": log_id
        },
        "secondary_output": {
            "count": 4,
            "id": pigment_id
        },
        "per_tick_usage": True
    }

    output_file = OUTPUT_DIR / f"{color}_sapling.json"

    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(recipe, f, indent=2)

print("生成完成")