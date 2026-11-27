"""Rebuild an FTB overlay from the mod's current zh_cn, keeping this pack's own wording.

The overlay for these mods is a whole-file override, so it replaces the shipped translation rather
than extending it: a key the mod added after our copy was taken simply disappears. Re-translating is
the wrong fix — the shipped file is already Chinese and already current. What has to survive is only
our own edits to it.

So the shipped zh_cn becomes the base and this pack's differing or extra entries are layered on top.

    python script/lang/rebuild_ftb_overlay.py ftbquests [--dry-run]
"""
import io
import json
import os
import sys
import zipfile

MODS = r"E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods"
ASSETS = "src/main/resources/assets"


def find_jar(fragment):
    for name in os.listdir(MODS):
        if name.endswith(".jar") and fragment in name:
            return os.path.join(MODS, name)
    return None


def read_json(path):
    with io.open(path, encoding="utf-8") as handle:
        return json.load(handle)


def main(argv):
    if not argv:
        print("usage: rebuild_ftb_overlay.py <namespace> [--dry-run]")
        return 1
    namespace = argv[0]
    dry_run = "--dry-run" in argv

    jar = find_jar(namespace)
    if not jar:
        print(f"  jar for {namespace} not found")
        return 1

    with zipfile.ZipFile(jar) as archive:
        shipped_path = f"assets/{namespace}/lang/zh_cn.json"
        if shipped_path not in archive.namelist():
            print(f"  {namespace} ships no zh_cn; nothing to rebuild from")
            return 1
        shipped = json.loads(archive.read(shipped_path).decode("utf-8"))

    overlay_path = os.path.join(ASSETS, namespace, "lang", "zh_cn.json")
    ours = read_json(overlay_path) if os.path.exists(overlay_path) else {}

    merged = dict(shipped)
    changed = []
    added = []

    for key, value in ours.items():
        if key in shipped:
            if shipped[key] != value:
                merged[key] = value
                changed.append(key)
        else:
            # Our own extension, or a key the mod has since dropped. Kept either way: a stale key is
            # harmless, and dropping one of our additions would lose a deliberate translation.
            merged[key] = value
            added.append(key)

    ordered = {key: merged[key] for key in merged}

    print(f"  {namespace}: shipped={len(shipped)} ours={len(ours)} "
          f"result={len(ordered)}")
    print(f"  our wording kept: {len(changed)}")
    print(f"  our extra keys kept: {len(added)}")

    if dry_run:
        return 0

    target_dir = os.path.dirname(overlay_path)
    os.makedirs(target_dir, exist_ok=True)
    with io.open(overlay_path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(ordered, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
