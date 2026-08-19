import argparse
import json
from pathlib import Path


NAMES = {
    "arcanist": "神秘学家协会",
    "arcanist_sell": "奥术奇物商店",
    "alchemist": "炼金术师协会",
    "alchemist_sell": "炼金材料商店",
}


parser = argparse.ArgumentParser()
parser.add_argument("lang_file", type=Path)
parser.add_argument("professions", nargs="+")
args = parser.parse_args()

with args.lang_file.open("r", encoding="utf-8") as f:
    lang = json.load(f)

for profession in args.professions:
    for stem in (profession, f"{profession}_sell"):
        lang[f"bountiful.decree.{stem}.name"] = NAMES[stem]

with args.lang_file.open("w", encoding="utf-8") as f:
    json.dump(lang, f, ensure_ascii=False, indent=2)
    f.write("\n")

print(f"updated {args.lang_file}")
