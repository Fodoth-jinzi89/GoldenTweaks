"""Merge translation batches into a mod's zh_cn overlay.

A batch file is named `_<namespace>_part<N>.json`, so the namespace comes from its own name. Entries
already in the overlay are kept unless the batch corrects them; keys the mod no longer ships are
dropped, since a stale entry is invisible while the real key renders untranslated.

This is the general form of the FTB merge: any mod whose lang file this pack overrides can be filled
the same way.

    python script/lang/merge_overlay_lang.py [namespace ...]
"""
import glob
import io
import json
import os
import sys
import zipfile

MODS = r"E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods"
ASSETS = "src/main/resources/assets"
BATCHES = "script/lang/_*_part*.json"


def shipped_keys(namespace):
    """Every key the mod ships, or an empty set when it cannot be read."""
    for name in sorted(os.listdir(MODS)):
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


def main(argv):
    wanted = set(arg for arg in argv if not arg.startswith("--"))

    for path in sorted(glob.glob(BATCHES)):
        base = os.path.basename(path)
        namespace = base[1:].split("_part")[0]
        if wanted and namespace not in wanted:
            continue

        with io.open(path, encoding="utf-8") as handle:
            batch = json.load(handle)

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

        # Keys this pack added on purpose (a school or tab the mod does not define) are kept; only
        # entries the mod itself once had and has since dropped are removed.
        en = shipped_keys(namespace)
        if en:
            extra_ok = set(batch) - en
            stale = [key for key in merged if key not in en and key not in extra_ok]
            for key in stale:
                del merged[key]
        else:
            stale = []

        with io.open(target, "w", encoding="utf-8", newline="\n") as handle:
            json.dump(merged, handle, indent=2, ensure_ascii=False)
            handle.write("\n")

        print(f"  {namespace}: {before} -> {len(merged)} "
              f"(+{added} new, {updated} changed, {len(stale)} stale dropped)")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
