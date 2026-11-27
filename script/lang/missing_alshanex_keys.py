"""Report which alshanex_familiars names are missing a Chinese translation.

The guide book is data-driven: every entry file holds lang keys (`guide.alshanex_familiars.*`)
rather than literal text, so translating the book means adding those keys to the mod's zh_cn.json.
This script compares the shipped en_us against our overlay and groups what is absent, so a
translation pass can be done in batches instead of by hunting through 400 keys at once.

    python script/lang/missing_alshanex_keys.py [en.json]
"""
import io
import json
import sys

DEFAULT_EN = r"C:\Users\peiranyu\AppData\Local\Temp\alsh\en.json"
ZH = "src/main/resources/assets/alshanex_familiars/lang/zh_cn.json"


def main(argv):
    en_path = argv[0] if argv else DEFAULT_EN
    en = json.load(io.open(en_path, encoding="utf-8"))
    zh = json.load(io.open(ZH, encoding="utf-8"))

    missing = [k for k in en if k not in zh]

    def group(key):
        parts = key.split(".")
        if key.startswith("guide."):
            return "guide"
        if parts[0] in ("block", "item", "entity"):
            return parts[0]
        if parts[0] in ("ui", "tooltip", "screen", "menu"):
            return parts[0]
        return parts[0]

    buckets = {}
    for key in missing:
        buckets.setdefault(group(key), []).append(key)

    print(f"  en_us keys : {len(en)}")
    print(f"  zh_cn keys : {len(zh)}")
    print(f"  missing    : {len(missing)}")
    print()
    for name in sorted(buckets, key=lambda n: -len(buckets[n])):
        print(f"  [{len(buckets[name]):3d}] {name}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
