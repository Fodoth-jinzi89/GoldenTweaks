import json
from pathlib import Path

MODID = "croptopia"

INPUT_FILE = Path("./seed_map.json")
OUTPUT_DIR = Path("./generated_recipes")


def load_mapping():
    with open(INPUT_FILE, "r", encoding="utf-8") as f:
        return json.load(f)


def to_registry_id(name: str) -> str:
    """
    item.croptopia.xxx -> croptopia:xxx
    item.minecraft.xxx -> minecraft:xxx
    """
    if name.startswith("item."):
        name = name[len("item."):]
    return name.replace(".", ":", 1)


def get_file_name(registry_id: str) -> str:
    """
    croptopia:artichoke -> artichoke.json
    """
    return registry_id.split(":")[-1] + ".json"


def build_recipe(seed, outputs):
    seed_id = to_registry_id(seed)

    # 防止空数组
    if not outputs:
        raise ValueError(f"No output for seed: {seed}")

    output_id = to_registry_id(outputs[0])

    return {
        "neoforge:conditions": [
            {
                "type": "neoforge:mod_loaded",
                "modid": MODID
            }
        ],
        "type": "mekmm:planting",
        "item_input": {
            "count": 1,
            "item": seed_id
        },
        "chemical_input": {
            "amount": 1,
            "chemical": "mekmm:nutrient_solution"
        },
        "main_output": {
            "count": 4,
            "id": output_id
        },
        "per_tick_usage": True
    }


def main():
    data = load_mapping()
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    for seed, outputs in data.items():
        try:
            recipe = build_recipe(seed, outputs)

            seed_id = to_registry_id(seed)
            file_name = get_file_name(seed_id)
            out_path = OUTPUT_DIR / file_name

            with open(out_path, "w", encoding="utf-8") as f:
                json.dump(recipe, f, ensure_ascii=False, indent=2)

            print(f"Generated: {out_path}")

        except Exception as e:
            print(f"[ERROR] {seed}: {e}")


if __name__ == "__main__":
    main()