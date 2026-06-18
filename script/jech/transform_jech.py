import json

INPUT_FILE = "input.txt"
OUTPUT_FILE = "generate.yaml"

FIELDS = ["suffix", "contains", "regExp", "equals"]

def dedup(seq):
    seen = set()
    out = []
    for x in seq:
        if x not in seen:
            seen.add(x)
            out.append(x)
    return out

def format_array(arr):
    if not arr:
        return "[]"
    items = []
    for i, x in enumerate(arr):
        item = json.dumps(x, ensure_ascii=False)
        if i < len(arr) - 1:
            items.append(item + ",")
        else:
            items.append(item)
    return "[\n" + "\n".join(items) + "\n]"

def main():
    with open(INPUT_FILE, "r", encoding="utf-8") as f:
        data = json.load(f)

    merged = {k: [] for k in FIELDS}

    # 合并所有 jars
    for jar in data.get("jars", []):
        for k in FIELDS:
            merged[k].extend(jar.get(k, []))

    # 去重
    for k in FIELDS:
        merged[k] = dedup(merged[k])

    # 输出
    lines = []
    for k in FIELDS:
        lines.append(f"\"{k}\":{format_array(merged[k])}")

    result = "\n".join(lines)

    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write(result)

if __name__ == "__main__":
    main()