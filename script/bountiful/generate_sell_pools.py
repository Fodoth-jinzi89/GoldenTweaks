import json
from pathlib import Path

def transform_file(path: Path):
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)

    new_data = {
        "content": {},
        "requires": data.get("requires", [])
    }

    content = data.get("content", {})

    for key, value in content.items():
        new_value = dict(value)

        amount = new_value.get("amount", {})
        min_v = amount.get("min")
        max_v = amount.get("max")

        # 判断倍率
        if min_v == 1 and max_v == 1:
            factor = 5
        else:
            factor = 2

        if "unitWorth" in new_value and isinstance(new_value["unitWorth"], (int, float)):
            new_value["unitWorth"] *= factor

        if "effectiveValue" in new_value and isinstance(new_value["effectiveValue"], (int, float)):
            new_value["effectiveValue"] *= factor

        new_data["content"][key] = new_value

    new_path = path.with_name(f"{path.stem}_sell.json")

    with open(new_path, "w", encoding="utf-8") as f:
        json.dump(new_data, f, ensure_ascii=False, indent=4)


def main():
    for file in Path(".").glob("*.json"):
        if file.name.endswith("_sell.json"):
            continue

        if not file.is_file():
            continue

        transform_file(file)
        print(f"generated: {file.name} -> {file.stem}_sell.json")


if __name__ == "__main__":
    main()