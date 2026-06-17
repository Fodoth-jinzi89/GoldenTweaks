import json
from pathlib import Path

INPUT_FILE = "zh_cn.json"
OUTPUT_DIR = "generated_tips"

Path(OUTPUT_DIR).mkdir(exist_ok=True)

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    lang = json.load(f)

count = 0

for key in lang:
    if not key.startswith("tip.tipsmod.") or not key.endswith(".text"):
        continue

    # 去掉前后缀
    middle = key[len("tip.tipsmod."):-len(".text")]

    # create.engineers_goggles
    # -> create_engineers_goggles
    filename = middle.replace(".", "_") + ".json"

    data = {
        "type": "tipsmod:simple",
        "text": {
            "translate": key
        }
    }

    output_file = Path(OUTPUT_DIR) / filename

    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    count += 1
    print(f"Generated: {filename}")

print(f"\nGenerated {count} files.")
