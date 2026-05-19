import json
from pathlib import Path

MODID = "evolvedmekanism"

INPUT_FILE = "tags.json"
OUTPUT_FILE = "fluids.json"

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    data = json.load(f)

result = {}

PREFIX = "tag.fluid.c."

for key, value in data.items():
    if not key.startswith(PREFIX):
        continue

    fluid_name = key[len(PREFIX):]

    # block
    result[f"block.{MODID}.{fluid_name}_fluid"] = value

    # fluid source
    result[f"fluid.{MODID}.{fluid_name}"] = value

    # flowing fluid
    result[f"fluid.{MODID}.flowing_{fluid_name}"] = f"流动的{value}"

    # bucket
    result[f"item.{MODID}.{fluid_name}_bucket"] = f"{value}桶"

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print(f"生成完成: {OUTPUT_FILE}")