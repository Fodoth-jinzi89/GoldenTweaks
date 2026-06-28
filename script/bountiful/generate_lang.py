import json
from pathlib import Path

lang = {}

for file in sorted(Path(".").glob("*.json")):
    key = f"bountiful.decree.{file.stem}.name"
    lang[key] = ""

with open("zh_cn.json", "w", encoding="utf-8") as f:
    json.dump(lang, f, ensure_ascii=False, indent=4)

print(f"Generated {len(lang)} entries -> zh_cn.json")