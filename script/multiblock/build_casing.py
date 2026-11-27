"""从一个机壳的 base + frame 生成贴图，并写进模组。

一个材质只需两张输入（32x32 的底纹与边框），默认产出两张产物：

    shell_<material>.png       基础方块贴图，方块模型引用（即 frame 叠在 base 上）
    shell_<material>_ctm.png   八向连接图，正式机壳用

连接图必须登记到 atlases/blocks.json：模型不引用它，图集不会自动收集，缺失时连接效果被
静默跳过。这一步在这里一并做完，所以「提供 base + frame，跑一遍脚本」就是全部工作。

加 `--all-layouts` 时额外产出其余七种布局的连接图（ct_test_<material>_<layout>_ctm.png）。
那些是排查用的对照图，正式机壳用不到；改动布局画法时才需要它们。

    python script/multiblock/build_casing.py pe \\
        arts/multiblock/shell_pe_base.png arts/multiblock/shell_pe_frame.png
    python script/multiblock/build_casing.py pe ... --all-layouts
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from PIL import Image

from ctm import (CELL, build_corner_tile, build_rectangle_sheet,
                 convert_to_omni, cut_layout)

ASSETS = os.path.join("src", "main", "resources", "assets", "goldentweaks")
TEXTURES = os.path.join(ASSETS, "textures", "block")
ATLAS = os.path.join(ASSETS, "atlases", "blocks.json")

# 其余七种布局，仅在 --all-layouts 时产出。
OTHER_LAYOUTS = ("HORIZONTAL", "HORIZONTAL_KRYPPERS", "VERTICAL",
                 "ROOF", "ROOF_STAIR", "CROSS", "RECTANGLE")


def write_json(path, data):
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def register_atlas(resources):
    """把连接图登记进图集：保留已有条目，不重复。"""
    with open(ATLAS, encoding="utf-8") as handle:
        atlas = json.load(handle)
    existing = {entry.get("resource") for entry in atlas.get("sources", [])}
    added = 0
    for resource in resources:
        if resource in existing:
            continue
        atlas["sources"].append({"type": "minecraft:single", "resource": resource})
        existing.add(resource)
        added += 1
    write_json(ATLAS, atlas)
    return added


def main(argv):
    all_layouts = "--all-layouts" in argv
    positional = [a for a in argv if a != "--all-layouts"]

    if len(positional) < 3:
        print("usage: build_casing.py <material> <base.png> <frame.png> [--all-layouts]")
        print("  例：build_casing.py pe arts/multiblock/shell_pe_base.png"
              " arts/multiblock/shell_pe_frame.png")
        return 1

    material, base_path, frame_path = positional[0], positional[1], positional[2]
    base = Image.open(base_path).convert("RGBA")
    frame = Image.open(frame_path).convert("RGBA")
    for path, image in ((base_path, base), (frame_path, frame)):
        if image.size != (CELL, CELL):
            print(f"  {path}: 需要 {CELL}x{CELL}，实际 {image.size}")
            return 1

    # 基础方块贴图就是 frame 叠在 base 上，与游戏里单块的样子一致。
    plain = Image.alpha_composite(base, frame)
    plain.save(os.path.join(TEXTURES, f"shell_{material}.png"))
    print(f"  shell_{material}.png  {plain.size[0]}x{plain.size[1]}")

    # 4x4 连接图，再铺一层底纹，然后展开成八向。
    large = Image.new("RGBA", (CELL * 4, CELL * 4), (0, 0, 0, 0))
    for row in range(4):
        for column in range(4):
            large.paste(base, (column * CELL, row * CELL))
    ctm = Image.alpha_composite(large, build_rectangle_sheet(frame))
    corners = build_corner_tile(frame)
    omni, filled, placed = convert_to_omni(ctm, corners)

    omni.save(os.path.join(TEXTURES, f"shell_{material}_ctm.png"))
    resources = [f"goldentweaks:block/shell_{material}_ctm"]
    print(f"  shell_{material}_ctm.png  {omni.size[0]}x{omni.size[1]}"
          f"  ({filled} 格, {placed} 角标)")

    if all_layouts:
        for name in OTHER_LAYOUTS:
            cut, _ = cut_layout(omni, name)
            asset = f"ct_test_{material}_{name.lower()}_ctm"
            cut.save(os.path.join(TEXTURES, f"{asset}.png"))
            resources.append(f"goldentweaks:block/{asset}")
            print(f"  {asset}.png  {cut.size[0]}x{cut.size[1]}")

    added = register_atlas(resources)
    print(f"  图集新增 {added} 条")
    if not all_layouts:
        print("  （其余七种布局用 --all-layouts 生成）")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
