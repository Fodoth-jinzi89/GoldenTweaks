import json
from pathlib import Path

EN_FILE = Path("en_us.json")
ZH_FILE = Path("zh_cn.json")


def load_json(path: Path) -> dict:
    if not path.exists():
        raise FileNotFoundError(f"文件不存在: {path}")

    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def save_json(path: Path, data: dict):
    with path.open("w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def main():
    en_data = load_json(EN_FILE)
    zh_data = load_json(ZH_FILE)

    missing_count = 0
    missing_items = []

    for key, value in en_data.items():
        if key not in zh_data:
            missing_items.append((key, value))
            missing_count += 1

    # 将缺失的键值对追加到末尾
    for key, value in missing_items:
        zh_data[key] = value

    save_json(ZH_FILE, zh_data)

    print(f"已补全 {missing_count} 个缺失键值对")
    print(f"输出文件: {ZH_FILE}")


if __name__ == "__main__":
    main()