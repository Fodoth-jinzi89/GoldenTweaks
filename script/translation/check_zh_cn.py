from pathlib import Path

FILE = Path("zh_cn.json")

def check_missing_commas(path: Path):
    lines = path.read_text(encoding="utf-8").splitlines()

    errors = []

    for i, line in enumerate(lines):
        stripped = line.strip()

        # 跳过空行和括号
        if not stripped or stripped in ("{", "}"):
            continue

        # 当前行看起来像一个键值对
        is_entry = ":" in stripped

        if not is_entry:
            continue

        # 找下一条有效行
        next_valid = None
        for j in range(i + 1, len(lines)):
            nxt = lines[j].strip()
            if nxt:
                next_valid = nxt
                break

        # 如果下一行还是键值对，而当前行没逗号
        if (
            next_valid
            and next_valid != "}"
            and ":" in next_valid
            and not stripped.endswith(",")
        ):
            errors.append((i + 1, line))

    if not errors:
        print("没有发现缺失逗号的问题。")
    else:
        print("发现可能缺失逗号的行：\n")
        for lineno, content in errors:
            print(f"第 {lineno} 行:")
            print(content)
            print()

if __name__ == "__main__":
    check_missing_commas(FILE)
