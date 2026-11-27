"""移除全部 ct_test_* 测试机壳资源。

这些方块原本用于并排比较八种 CT 布局，布局确定后已从注册代码里删除。
这里清掉它们留下的资源：纹理、blockstate、方块/物品模型、图集条目、语言键。

只做删除，不碰 shell_pe / shell_pp —— 那两个是正式机壳。
"""
import glob
import json
import os
import sys

ASSETS = os.path.join("src", "main", "resources", "assets", "goldentweaks")
PREFIX = "ct_test_"


def write_json(path, data):
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def main():
    removed = 0

    patterns = (
        "textures/block",
        "blockstates",
        "models/block",
        "models/item",
        "models/item",
    )
    for folder in ("textures/block", "blockstates", "models/block", "models/item"):
        for path in glob.glob(os.path.join(ASSETS, folder, f"{PREFIX}*")):
            os.remove(path)
            removed += 1
    print(f"  删除 {removed} 个资源文件")

    atlas_path = os.path.join(ASSETS, "atlases", "blocks.json")
    with open(atlas_path, encoding="utf-8") as handle:
        atlas = json.load(handle)
    before = len(atlas.get("sources", []))
    atlas["sources"] = [
        entry for entry in atlas.get("sources", [])
        if PREFIX not in str(entry.get("resource", ""))
    ]
    write_json(atlas_path, atlas)
    print(f"  图集: {before} -> {len(atlas['sources'])} 条")

    for lang in ("en_us", "zh_cn"):
        path = os.path.join(ASSETS, "lang", f"{lang}.json")
        with open(path, encoding="utf-8") as handle:
            entries = json.load(handle)
        stale = [key for key in entries if PREFIX in key]
        for key in stale:
            del entries[key]
        if stale:
            write_json(path, entries)
        print(f"  {lang}: 移除 {len(stale)} 个键")

    left = glob.glob(os.path.join(ASSETS, "**", f"{PREFIX}*"), recursive=True)
    print(f"  剩余 ct_test_* 文件: {len(left)}")
    for path in left[:5]:
        print(f"    {path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
