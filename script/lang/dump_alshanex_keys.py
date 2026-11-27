"""Dump the untranslated alshanex_familiars keys with their English text, for a translation pass.

Writes one line per key as `KEY\tENGLISH` so the output can be read in batches and the zh_cn file
built from it.

    python script/lang/dump_alshanex_keys.py guide > out.txt
"""
import io
import json
import sys

DEFAULT_EN = r"C:\Users\peiranyu\AppData\Local\Temp\alsh\en.json"
ZH = "src/main/resources/assets/alshanex_familiars/lang/zh_cn.json"


def main(argv):
    en_path = argv[0] if argv and argv[0].endswith(".json") else DEFAULT_EN
    prefix = argv[1] if len(argv) > 1 else None
    if argv and not argv[0].endswith(".json"):
        prefix = argv[0]

    en = json.load(io.open(en_path, encoding="utf-8"))
    zh = json.load(io.open(ZH, encoding="utf-8"))

    for key in en:
        if key in zh:
            continue
        if prefix and not key.startswith(prefix):
            continue
        print(f"{key}\t{en[key]}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
