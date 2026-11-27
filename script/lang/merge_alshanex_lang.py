"""Merge the translated alshanex_familiars batches into the mod's zh_cn.json.

The translation was produced in batches (part1..part3) so each could be written and checked on its
own. This merges them in place: keys already present are overwritten with the new text, keys absent
are added, and nothing else in the file is touched. Existing entries are preserved verbatim, which
matters because earlier translations of this mod are still correct.

    python script/lang/merge_alshanex_lang.py
"""
import glob
import io
import json
import os
import sys

ZH = "src/main/resources/assets/alshanex_familiars/lang/zh_cn.json"
EN = r"C:\Users\peiranyu\AppData\Local\Temp\alsh\en.json"
BATCHES = "script/lang/_alshanex_part*.json"


def _english_keys():
    """Every key the mod actually ships, or an empty set when the reference dump is absent."""
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

    # Sorted so the file stays diffable; the game does not care about order.
    ordered = {key: merged[key] for key in sorted(merged)}

    # Drop keys this mod no longer ships: an earlier pass introduced a misspelling of one entry, and
    # a stale key is worse than none — it lingers in the file looking like a translation while the
    # real one is reported missing.
    en = _english_keys()
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
