"""Merge the translation batches for the FTB mods into their zh_cn overlays.

Each namespace gets its own overlay file, because an asset override is per-file: mixing two mods into
one would leave the other mod's keys unresolvable. Entries the overlay already has are kept unless the
batch corrects them, and keys the mod no longer ships are dropped so a stale entry cannot look like a
translation while the real key renders untranslated.

    python script/lang/merge_ftb_lang.py
"""
import glob
import io
import json
import os
import sys
import zipfile

MODS = r"E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods"
ASSETS = "src/main/resources/assets"
BATCHES = "script/lang/_ftb*_part*.json"


def shipped_keys(namespace):
    """Every key the mod ships, or an empty set when it cannot be read."""
    for name in os.listdir(MODS):
        if not (name.endswith(".jar") and namespace in name):
            continue
        try:
            with zipfile.ZipFile(os.path.join(MODS, name)) as archive:
                path = f"assets/{namespace}/lang/en_us.json"
                if path in archive.namelist():
                    return set(json.loads(archive.read(path).decode("utf-8")))
        except (zipfile.BadZipFile, KeyError, ValueError):
            continue
    return set()


def main():
    for path in sorted(glob.glob(BATCHES)):
        with io.open(path, encoding="utf-8") as handle:
            batch = json.load(handle)
        # The batch is named for its namespace: _ftbquests_part1.json -> ftbquests.
        namespace = os.path.basename(path)[1:].split("_part")[0]

        target = os.path.join(ASSETS, namespace, "lang", "zh_cn.json")
        existing = {}
        if os.path.exists(target):
            with io.open(target, encoding="utf-8") as handle:
                existing = json.load(handle)

        before = len(existing)
        merged = dict(existing)
        added = 0
        updated = 0
        for key, value in batch.items():
            if key in merged:
                if merged[key] != value:
                    merged[key] = value
                    updated += 1
            else:
                merged[key] = value
                added += 1

        en = shipped_keys(namespace)
        stale = [key for key in merged if en and key not in en]
        for key in stale:
            del merged[key]

        ordered = {key: merged[key] for key in merged}
        os.makedirs(os.path.dirname(target), exist_ok=True)
        with io.open(target, "w", encoding="utf-8", newline="\n") as handle:
            json.dump(ordered, handle, indent=2, ensure_ascii=False)
            handle.write("\n")

        print(f"  {namespace}: {before} -> {len(ordered)} "
              f"(+{added} new, {updated} changed, {len(stale)} stale dropped)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
