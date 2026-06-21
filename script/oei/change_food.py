import json

INPUT_FILE = "input.json"
OUTPUT_FILE = "output.json"

def pick_flavor_item(match_items):
    for item in match_items:
        if item.startswith("flavor_immersed_daily:"):
            return item
    return None

def process(data):
    for entry in data:
        match_items = entry.get("matchItems", [])
        result_item = entry.get("resultItems", "")

        # 1. 先检查 croptopia
        has_croptopia = result_item.startswith("croptopia:")
        croptopia_item = result_item if has_croptopia else None

        # 2. flavor 覆盖 result
        flavor_item = pick_flavor_item(match_items)
        if flavor_item:
            result_item = flavor_item

        entry["resultItems"] = result_item

        # 3. 如果原 result 是 croptopia → 加入 matchItems
        if croptopia_item and croptopia_item not in match_items:
            match_items.append(croptopia_item)

        entry["matchItems"] = match_items

    return data

def main():
    with open(INPUT_FILE, "r", encoding="utf-8") as f:
        data = json.load(f)

    data = process(data)

    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print("done ->", OUTPUT_FILE)

if __name__ == "__main__":
    main()