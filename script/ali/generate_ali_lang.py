import json
from pathlib import Path

# loot_table 根目录
ROOT = Path("loot_table")

lang = {}

for file in ROOT.rglob("*.json"):
    # 获取相对路径并去掉 .json 后缀
    rel = file.relative_to(ROOT).with_suffix("")

    # 例如：
    # chests/airship_village/airship_village1
    key = f"ali/loot_table/{rel.as_posix()}"

    # value 为文件名（不含扩展名）
    value = rel.stem

    lang[key] = value

# 输出 lang.json
with open("lang.json", "w", encoding="utf-8") as f:
    json.dump(lang, f, ensure_ascii=False, indent=2)

print(f"已生成 {len(lang)} 条语言键到 lang.json")