import json
from pathlib import Path

def transform_file(path: Path):
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)

    stem = path.stem

    new_data = {
        "objectives": data.get("rewards", []),
        "rewards": [f"{stem}_sell"],
        "linkedProfessions": data.get("linkedProfessions", [])
    }

    new_path = path.with_name(f"{stem}_sell.json")

    with open(new_path, "w", encoding="utf-8") as f:
        json.dump(new_data, f, ensure_ascii=False, indent=4)


def main():
    for file in Path(".").glob("*.json"):
        # 避免重复处理生成文件
        if file.name.endswith("_sell.json"):
            continue

        if not file.is_file():
            continue

        try:
            transform_file(file)
            print(f"generated: {file.name} -> {file.stem}_sell.json")
        except Exception as e:
            print(f"failed: {file.name}, reason: {e}")


if __name__ == "__main__":
    main()