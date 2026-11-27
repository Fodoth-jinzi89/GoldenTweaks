"""Merge the translated familiarslib batches into the mod's zh_cn.json.

Same shape as the alshanex merge, for the library mod this one depends on: existing entries are kept,
new ones added, and keys the mod no longer ships are dropped so a stale entry cannot masquerade as a
translation.

    python script/lang/merge_familiarslib_lang.py
"""
import glob
import io
import json
import os
import sys

ZH = "src/main/resources/assets/familiarslib/lang/zh_cn.json"
EN = r"C:\Users\peiranyu\AppData\Local\Temp\flib\en.json"
BATCHES = "script/lang/_familiarslib_part*.json"


def english_keys():
    """Every key the mod ships, or an empty set when the reference dump is absent."""
    if not os.path.exists(EN):
        print(f"  (no {EN}; stale-key cleanup skipped)")
        return set()
    with io.open(EN, encoding="utf-8") as handle:
        return set(json.load(handle))


def main():
    with io.open(ZH, encoding="utf-8") as handle:
        merged = json.load(handle)

    before = len(merged)
    added = 0
    updated = 0

    for path in sorted(glob.glob(BATCHES)):
        if not os.path.exists(path):
            continue
        with io.open(path, encoding="utf-8") as handle:
            batch = json.load(handle)
        for key, value in batch.items():
            if key in merged:
                if merged[key] != value:
                    merged[key] = value
                    updated += 1
            else:
                merged[key] = value
                added += 1
        print(f"  {os.path.basename(path)}: {len(batch)} entries")

    ordered = {key: merged[key] for key in sorted(merged)}

    en = english_keys()
    stale = [key for key in ordered if key not in en]
    for key in stale:
        del ordered[key]

    with io.open(ZH, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(ordered, handle, indent=2, ensure_ascii=False)
        handle.write("\n")

    print(f"  before: {before}  added: {added}  updated: {updated}  "
          f"stale removed: {len(stale)}  after: {len(ordered)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
