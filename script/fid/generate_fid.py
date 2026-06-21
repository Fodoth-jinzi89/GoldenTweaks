import json
import os

# 输出目录
OUTPUT_DIR = "generated_recipes"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# 所有输入种子
seeds = [
    "winterjujubesapling",
    "tangerinesapling",
    "plumsapling",
    "lycheesapling",
    "duriansapling",
    "pawpawsapling",
    "loquatleavesapling",
    "greenplumsapling",
    "mulberrysapling",
    "hawthornsapling",
    "mangosteensapling",
    "pomegranatesapling",
    "sweetmelonsapling",
    "reddatesapling",
    "pistachionutsapling"
]

# 特殊输出映射
special_output = {
}

def to_output_id(seed: str) -> str:
    # 特殊情况优先
    if seed in special_output:
        return special_output[seed]

    # 默认规则：去掉后缀
    if seed.endswith("sapling"):
        return seed[:-7]
    if seed.endswith("_seed"):
        return seed[:-5]
    if seed.endswith("seed"):
        return seed[:-4]
    if seed.endswith("seeds"):
        return seed[:-5]

    return seed


def make_recipe(seed: str):
    output_item = to_output_id(seed)

    return {
        "neoforge:conditions": [
            {
                "type": "neoforge:mod_loaded",
                "modid": "flavor_immersed_daily"
            }
        ],
        "type": "mekmm:planting",
        "item_input": {
            "count": 1,
            "item": f"flavor_immersed_daily:{seed}"
        },
        "chemical_input": {
            "amount": 1,
            "chemical": "mekmm:nutrient_solution"
        },
        "main_output": {
            "count": 4,
            "id": f"flavor_immersed_daily:{output_item}"
        },
        "per_tick_usage": True
    }


for seed in seeds:
    recipe = make_recipe(seed)

    path = os.path.join(OUTPUT_DIR, f"{seed}.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(recipe, f, ensure_ascii=False, indent=2)

    print(f"generated: {path}")