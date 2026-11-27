"""Report how much of each FTB mod's English is translated in this mod's overlay.

FTB ships its own zh_cn, but this pack overrides several of them, and an override replaces the file
wholesale rather than merging. A key the mod added after our copy was taken therefore renders as its
key name in game. This compares our file against the shipped en_us and prints the gap per mod.

    python script/lang/ftb_gaps.py
"""
import io
import json
import os
import zipfile

MODS = r"E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods"
ASSETS = "src/main/resources/assets"

# jar file name fragment -> asset namespace the lang file lives under
TARGETS = [
    ("ftbchunks", "ftbchunks"),
    ("ftbfiltersystem", "ftbfiltersystem"),
    ("ftbquests-", "ftbquests"),
    ("ftbquest_slot_rewards", "ftbquest_slot_rewards"),
    ("ftbultimine", "ftbultimine"),
    ("ftblibrary", "ftblibrary"),
    ("ftbteams", "ftbteams"),
    ("ftbxmodcompat", "ftbxmodcompat"),
    ("ftbxaerocompat", "ftbxaerocompat"),
    ("ftbquestsentityvis", "ftbquestsentityvis"),
]


def find_jar(fragment):
    for name in os.listdir(MODS):
        if name.endswith(".jar") and fragment in name:
            return os.path.join(MODS, name)
    return None


def shipped_keys(jar, namespace):
    path = f"assets/{namespace}/lang/en_us.json"
    try:
        with zipfile.ZipFile(jar) as archive:
            if path not in archive.namelist():
                return None
            return json.loads(archive.read(path).decode("utf-8"))
    except (zipfile.BadZipFile, KeyError, ValueError):
        return None


def main():
    for fragment, namespace in TARGETS:
        jar = find_jar(fragment)
        if not jar:
            print(f"  {namespace}: jar not found")
            continue
        english = shipped_keys(jar, namespace)
        if english is None:
            print(f"  {namespace}: no en_us in jar")
            continue

        overlay_path = os.path.join(ASSETS, namespace, "lang", "zh_cn.json")
        if os.path.exists(overlay_path):
            with io.open(overlay_path, encoding="utf-8") as handle:
                ours = json.load(handle)
        else:
            ours = {}

        missing = [key for key in english if key not in ours]
        stale = [key for key in ours if key not in english]
        print(f"  {namespace:24s} en={len(english):4d} zh={len(ours):4d} "
              f"missing={len(missing):4d} stale={len(stale):3d}")


if __name__ == "__main__":
    main()
