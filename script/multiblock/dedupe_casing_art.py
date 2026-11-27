"""把测试机壳的贴图收敛成一套，删掉重复的那份。

原本每个测试方块有两张内容相同的图：`<name>.png` 给方块模型，`<name>_ctm.png` 给连接图集。
测试方块的模型改为引用机壳自己的单块贴图后，`<name>.png` 不再需要——连接效果只读 `_ctm`。
`_omnidirectional` 那张又和正式机壳的 `_ctm` 完全相同，所以它整套删掉，正式机壳直接用它。

做三件事：
  1. 删掉 ct_test_*_omnidirectional 的方块（方块模型、物品模型、blockstate）；
  2. 删掉所有 ct_test_*.png（无后缀的那份），保留 _ctm；
  3. 让测试方块的方块模型引用 shell_pe / shell_pp。
"""
import glob
import json
import os
import sys

ASSETS = os.path.join("src", "main", "resources", "assets", "goldentweaks")
TEXTURES = os.path.join(ASSETS, "textures", "block")

LAYOUTS = ("horizontal", "horizontal_kryppers", "vertical",
           "roof", "roof_stair", "cross", "rectangle")


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def main():
    removed = 0

    # 1. The omnidirectional test casings duplicate the shipping casings exactly.
    for material in ("pe", "pp"):
        name = f"ct_test_{material}_omnidirectional"
        for folder in ("blockstates", "models/block", "models/item"):
            path = os.path.join(ASSETS, folder, f"{name}.json")
            if os.path.exists(path):
                os.remove(path)
                print(f"  删除 {path}")
                removed += 1
        for suffix in ("", "_ctm"):
            path = os.path.join(TEXTURES, f"{name}{suffix}.png")
            if os.path.exists(path):
                os.remove(path)
                print(f"  删除 {path}")
                removed += 1

    # 2. The suffix-less copy: the test casings' models now point at the shell's own art.
    for path in glob.glob(os.path.join(TEXTURES, "ct_test_*.png")):
        if path.endswith("_ctm.png"):
            continue
        os.remove(path)
        print(f"  删除 {path}")
        removed += 1

    # 3. Point every remaining test casing's model at its material's plain sheet.
    for material in ("pe", "pp"):
        for layout in LAYOUTS:
            name = f"ct_test_{material}_{layout}"
            path = os.path.join(ASSETS, "models", "block", f"{name}.json")
            if os.path.exists(path):
                write_json(path, {
                    "parent": "minecraft:block/cube_all",
                    "textures": {"all": f"goldentweaks:block/shell_{material}"},
                })
                print(f"  改模型 {name} -> shell_{material}")

    # 4. Atlas entries for the removed sheets.
    atlas_path = os.path.join(ASSETS, "atlases", "blocks.json")
    with open(atlas_path, encoding="utf-8") as handle:
        atlas = json.load(handle)
    before = len(atlas.get("sources", []))
    atlas["sources"] = [
        entry for entry in atlas.get("sources", [])
        if "ct_test" not in str(entry) or str(entry).endswith("_ctm")
    ]
    atlas["sources"] = [
        entry for entry in atlas["sources"]
        if "_omnidirectional_ctm" not in str(entry)
    ]
    with open(atlas_path, "w", encoding="utf-8") as handle:
        json.dump(atlas, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print(f"  atlas: {before} -> {len(atlas['sources'])} 条")

    # 5. Language keys for the removed blocks.
    for lang in ("en_us", "zh_cn"):
        path = os.path.join(ASSETS, "lang", f"{lang}.json")
        with open(path, encoding="utf-8") as handle:
            entries = json.load(handle)
        stale = [key for key in entries if "ct_test" in key and "omnidirectional" in key]
        for key in stale:
            del entries[key]
        if stale:
            with open(path, "w", encoding="utf-8") as handle:
                json.dump(entries, handle, indent=2, ensure_ascii=False)
                handle.write("\n")
        print(f"  {lang}: 移除 {len(stale)} 个键")

    print(f"  共删除 {removed} 个文件")
    return 0


if __name__ == "__main__":
    sys.exit(main())
