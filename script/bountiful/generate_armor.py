import re

INPUT_FILE = "armor.txt"
OUTPUT_FILE = "armor_expanded.txt"

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    lines = f.readlines()

result = []

for line in lines:
    line = line.strip()
    if not line:
        continue

    parts = line.split()

    # 已经是 item 格式
    if parts[0] == "item":
        item_id = parts[1]
        price = int(float(parts[4]))
    else:
        item_id = parts[0]
        price = int(float(parts[1]))

    result.append(
        f"item {item_id} 1 1 {price} 0.1"
    )

    if item_id.endswith("_helmet"):
        base = item_id[:-7]

        chest_price = round(price * 8 / 5)
        leg_price = round(price * 7 / 5)
        boots_price = round(price * 4 / 5)

        result.append(
            f"item {base}_chestplate 1 1 {chest_price} 0.1"
        )
        result.append(
            f"item {base}_leggings 1 1 {leg_price} 0.1"
        )
        result.append(
            f"item {base}_boots 1 1 {boots_price} 0.1"
        )

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    f.write("\n".join(result))

print(f"生成完成，共 {len(result)} 条记录")