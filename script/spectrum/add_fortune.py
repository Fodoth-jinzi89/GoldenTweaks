import json
from pathlib import Path

# 你的 loot table 根目录
ROOT = Path("./blocks")


FORTUNE_FUNCTION = {
    "function": "minecraft:apply_bonus",
    "enchantment": "minecraft:fortune",
    "formula": "minecraft:ore_drops"
}


def has_fortune_function(functions):
    for func in functions:
        if (
            func.get("function") == "minecraft:apply_bonus"
            and func.get("enchantment") == "minecraft:fortune"
        ):
            return True
    return False


def process_file(path: Path):
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)

    modified = False

    pools = data.get("pools", [])

    for pool in pools:
        for entry in pool.get("entries", []):

            # 只处理 alternatives
            if entry.get("type") != "minecraft:alternatives":
                continue

            for child in entry.get("children", []):

                # 只处理非 cluster 掉落
                item_name = child.get("name", "")

                if item_name.endswith("_cluster"):
                    continue

                functions = child.setdefault("functions", [])

                # 已经有时运就跳过
                if has_fortune_function(functions):
                    continue

                # 添加时运
                functions.append(FORTUNE_FUNCTION.copy())
                modified = True

                print(f"[MODIFIED] {path} -> {item_name}")

    if modified:
        with open(path, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)


def main():
    for path in ROOT.rglob("*_cluster.json"):
        try:
            process_file(path)
        except Exception as e:
            print(f"[ERROR] {path}: {e}")


if __name__ == "__main__":
    main()