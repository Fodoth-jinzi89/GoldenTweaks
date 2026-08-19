import json
import argparse
from decimal import Decimal, getcontext
from pathlib import Path

# 提高精度，避免累计误差
getcontext().prec = 28


RARITY_MAP = {
    "c": "COMMON",
    "u": "UNCOMMON",
    "r": "RARE",
    "e": "EPIC",
    "l": "LEGENDARY"
}


# =========================
# 安全计算（使用 Decimal）
# =========================
def compute_value(unit_worth: Decimal, min_v: int, max_v: int) -> Decimal:
    return Decimal(unit_worth) * Decimal("0.5") * Decimal(min_v + max_v)


def infer_rarity(value: Decimal) -> str:
    v = float(value)

    if v < 900:
        return "COMMON"
    elif v < 8100:
        return "UNCOMMON"
    elif v < 72900:
        return "RARE"
    elif v < 656100:
        return "EPIC"
    else:
        return "LEGENDARY"


def parse_raw_line(line: str):
    parts = line.strip().split()

    if len(parts) not in (6, 7):
        return None

    item_id = parts[1]

    return {
        "item_id": item_id,
        "namespace": item_id.split(":")[0],
        "key": item_id.split(":")[1],
        "min": int(parts[2]),
        "max": int(parts[3]),
        "unitWorth": Decimal(parts[4]),
        "rarity_code": parts[6] if len(parts) == 7 else None
    }


# =========================
# 安全 float -> 普通小数
# =========================
def safe_number(x):
    if isinstance(x, Decimal):
        s = format(x, "f")  # 禁止科学计数法
        if "." in s:
            s = s.rstrip("0").rstrip(".")
        return float(s) if "." in s else int(s)

    if isinstance(x, float):
        s = format(Decimal(str(x)), "f")
        if "." in s:
            s = s.rstrip("0").rstrip(".")
        return float(s) if "." in s else int(s)

    return x


def process(obj):
    if isinstance(obj, dict):
        return {k: process(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [process(v) for v in obj]
    else:
        return safe_number(obj)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("input", nargs="?", default="input.json")
    parser.add_argument("output", nargs="?", default="output.json")
    args = parser.parse_args()

    raw_items = []

    with open(args.input, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue

            parsed = parse_raw_line(line)
            if parsed:
                raw_items.append(parsed)

    # =========================
    # 第一阶段：计算 value
    # =========================
    total_value = Decimal("0")

    for item in raw_items:
        value = compute_value(item["unitWorth"], item["min"], item["max"])
        item["value"] = value
        total_value += value

    # =========================
    # 第二阶段：生成结构
    # =========================
    data = {
        "content": {},
        "requires": sorted({i["namespace"] for i in raw_items})
    }

    for item in raw_items:
        value = item["value"]

        if item["rarity_code"]:
            rarity = RARITY_MAP.get(
                item["rarity_code"].lower(),
                infer_rarity(value)
            )
        else:
            rarity = infer_rarity(value)

        weight = (value / total_value) if total_value > 0 else Decimal("0")

        data["content"][item["key"]] = {
            "type": "item",
            "content": item["item_id"],
            "rarity": rarity,
            "amount": {
                "min": item["min"],
                "max": item["max"]
            },
            "unitWorth": item["unitWorth"],
            "weightMult": weight,
            "effectiveValue": value
        }

    # =========================
    # 第三阶段：安全输出 JSON
    # =========================
    data = process(data)

    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("w", encoding="utf-8") as f:
        json.dump(
            data,
            f,
            ensure_ascii=False,
            indent=4
        )

    print(f"done -> {output}")


if __name__ == "__main__":
    main()
