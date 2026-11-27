"""移除已废弃的 auto_* 测试机壳资源。

这些变体原本用来对比「按图宽推断布局」与「显式声明布局」，现在布局一律显式声明，
它们不再有用途。一次性清理：纹理、blockstate、模型、图集条目、语言键。
"""
import glob
import json
import os
import sys

ASSETS = os.path.join("src", "main", "resources", "assets", "goldentweaks")
SUFFIXES = ("auto_omni", "auto_rect")


def main():
    removed = 0

    # Textures, blockstates, models.
    for pattern in ("textures/block/ct_test_*_{}.png",
                    "blockstates/ct_test_*_{}.json",
                    "models/block/ct_test_*_{}.json",
                    "models/item/ct_test_*_{}.json"):
        for suffix in SUFFIXES:
            for path in glob.glob(os.path.join(ASSETS, pattern.format(suffix))):
                os.remove(path)
                print(f"  删除 {path}")
                removed += 1
        # The _ctm variant carries the suffix mid-name.
        for suffix in SUFFIXES:
            for path in glob.glob(os.path.join(ASSETS, pattern.format(suffix).replace(".png", "_ctm.png"))):
                os.remove(path)
                print(f"  删除 {path}")
                removed += 1

    # Atlas entries.
    atlas_path = os.path.join(ASSETS, "atlases", "blocks.json")
    with open(atlas_path, encoding="utf-8") as handle:
        atlas = json.load(handle)
    before = len(atlas.get("sources", []))
    atlas["sources"] = [
        entry for entry in atlas.get("sources", [])
        if not any(suffix in str(entry) for suffix in SUFFIXES)
    ]
    dropped = before - len(atlas["sources"])
    with open(atlas_path, "w", encoding="utf-8") as handle:
        json.dump(atlas, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print(f"  atlas 移除 {dropped} 条")
    removed += dropped

    # Language keys.
    for lang in ("en_us", "zh_cn"):
        path = os.path.join(ASSETS, "lang", f"{lang}.json")
        with open(path, encoding="utf-8") as handle:
            entries = json.load(handle)
        stale = [key for key in entries if any(suffix in key for suffix in SUFFIXES)]
        for key in stale:
            del entries[key]
        if stale:
            with open(path, "w", encoding="utf-8") as handle:
                json.dump(entries, handle, indent=2, ensure_ascii=False)
                handle.write("\n")
        print(f"  {lang}: 移除 {len(stale)} 个键")
        removed += len(stale)

    print(f"  共处理 {removed} 项")
    return 0


if __name__ == "__main__":
    sys.exit(main())
