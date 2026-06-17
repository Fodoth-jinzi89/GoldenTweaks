import json
from pathlib import Path

GEMS = [
    "alexandrite",
    "ammolite",
    "aquamarine",
    "black_diamond",
    "carnelian",
    "citrine",
    "garnet",
    "heliodor",
    "iolite",
    "kyanite",
    "moldavite",
    "opal",
    "pearl",
    "peridot",
    "rose_quartz",
    "ruby",
    "sapphire",
    "tanzanite",
    "topaz",
    "turquoise",
    "white_diamond"
]

OUTPUT_DIR = Path("glowrose_recipes")
OUTPUT_DIR.mkdir(exist_ok=True)

for gem in GEMS:
    recipe = {
        "neoforge:conditions": [
            {
                "type": "neoforge:mod_loaded",
                "modid": "silentgems"
            }
        ],
        "type": "mekmm:planting",
        "item_input": {
            "count": 1,
            "item": f"silentgems:{gem}_glowrose"
        },
        "chemical_input": {
            "amount": 1,
            "chemical": "mekmm:nutrient_solution"
        },
        "main_output": {
            "count": 2,
            "id": f"silentgems:{gem}_glowrose"
        },
        "secondary_output": {
            "count": 1,
            "id": f"silentgems:{gem}"
        },
        "secondary_chance": 0.05,
        "per_tick_usage": True
    }

    file_path = OUTPUT_DIR / f"{gem}_glowrose.json"

    with open(file_path, "w", encoding="utf-8") as f:
        json.dump(recipe, f, ensure_ascii=False, indent=2)

print(f"已生成 {len(GEMS)} 个配方文件到 {OUTPUT_DIR}")