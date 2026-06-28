import json
import os
from pathlib import Path

# 输入、输出目录
INPUT_DIR = "recipes"
OUTPUT_DIR = "create_recipes"


def wrap_ingredient(ingredient):
    return {
        "type": "neoforge:compound",
        "ingredients": [
            ingredient
        ]
    }


def convert_recipe(recipe):
    # 只转换研钵配方
    if recipe.get("type") != "fidworkblock:cooking":
        return None

    result = recipe["result"]

    result_entry = {
        "id": result["id"]
    }

    if "count" in result:
        result_entry["count"] = result["count"]

    return {
        "neoforge:conditions": [
            {
                "type": "neoforge:mod_loaded",
                "modid": "flavor_immersed_daily"
            }
        ],
        "type": "create:crushing",
        "ingredients": [
            wrap_ingredient(i)
            for i in recipe["ingredients"]
        ],
        "processing_time": 40,
        "results": [
            result_entry
        ]
    }


def main():
    input_root = Path(INPUT_DIR)
    output_root = Path(OUTPUT_DIR)

    converted = 0

    for file in input_root.rglob("*.json"):
        with open(file, "r", encoding="utf-8") as f:
            recipe = json.load(f)

        new_recipe = convert_recipe(recipe)
        if new_recipe is None:
            continue

        relative = file.relative_to(input_root)
        output_file = output_root / relative
        output_file.parent.mkdir(parents=True, exist_ok=True)

        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(new_recipe, f, indent=2, ensure_ascii=False)

        converted += 1

    print(f"完成，共转换 {converted} 个配方。")


if __name__ == "__main__":
    main()