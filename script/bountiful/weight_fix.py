import json
from pathlib import Path

def transform_file(path: Path):
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)

    modified = False

    for entry in data.get("content", {}).values():
        if "weightMult" in entry and isinstance(entry["weightMult"], (int, float)):
            entry["weightMult"] *= 1000
            modified = True

    if modified:
        with open(path, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=4)

        print(f"updated: {path.name}")

def main():
    for file in Path(".").glob("*.json"):
        if file.is_file():
            try:
                transform_file(file)
            except Exception as e:
                print(f"failed: {file.name}: {e}")

if __name__ == "__main__":
    main()