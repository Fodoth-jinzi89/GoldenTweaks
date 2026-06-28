import json
from pathlib import Path

# 你的 recipe 文件夹
RECIPE_DIR = Path("recipe")

OLD_ITEM = "hazennstuff:steel_ingot"
NEW_TAG = "c:ingots/steel"


def replace(obj):
    """递归遍历 JSON"""
    if isinstance(obj, dict):
        # 如果正好是 {"item": "..."} 这种结构
        if obj.get("item") == OLD_ITEM:
            del obj["item"]
            obj["tag"] = NEW_TAG

        # 继续递归
        for value in obj.values():
            replace(value)

    elif isinstance(obj, list):
        for item in obj:
            replace(item)


count = 0

for file in RECIPE_DIR.rglob("*.json"):
    try:
        with open(file, "r", encoding="utf-8") as f:
            data = json.load(f)

        before = json.dumps(data, sort_keys=True)

        replace(data)

        after = json.dumps(data, sort_keys=True)

        if before != after:
            with open(file, "w", encoding="utf-8") as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
                f.write("\n")
            print(f"✔ 修改 {file}")
            count += 1

    except Exception as e:
        print(f"✘ 处理失败 {file}: {e}")

print(f"\n完成，共修改 {count} 个文件。")