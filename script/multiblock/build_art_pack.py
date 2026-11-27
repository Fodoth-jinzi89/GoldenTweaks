"""为一个材质生成整套机壳贴图，并附上各种拼接预览。

一次跑完，产物集中在一个目录里，方便逐张检查：

    art/
      <mat>_base.png           输入的底纹平铺
      <mat>_ct.png             16 种连接状态（透明芯）
      <mat>_r.png              角标图
      <mat>_ctm.png            成品 4x4 连接图
      <mat>_omni.png           八向布局（进模组的那张）
      <mat>_<layout>.png       其余 7 种布局
    layouts/
      <mat>_1x1 / 2x2 / 3x3 / 4x4 / 1x3 / 3x1
      <mat>_L / plus / U / stair

用法：
    python script/multiblock/build_art_pack.py <out_dir> <material> <base> <frame>
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from PIL import Image

from ctm import (CELL, LAYOUTS, OMNI_GRID, build_corner_tile, build_rectangle_sheet,
                 convert_to_omni, cut_layout, idx_omni)

LAYOUT_ORDER = ("HORIZONTAL", "HORIZONTAL_KRYPPERS", "VERTICAL", "OMNIDIRECTIONAL",
                "ROOF", "ROOF_STAIR", "CROSS", "RECTANGLE")

SHAPES = {
    "1x1": [(0, 0)],
    "2x2": [(x, y) for y in range(2) for x in range(2)],
    "3x3": [(x, y) for y in range(3) for x in range(3)],
    "4x4": [(x, y) for y in range(4) for x in range(4)],
    "1x3": [(0, y) for y in range(3)],
    "3x1": [(x, 0) for x in range(3)],
    "L": [(0, 0), (1, 0), (2, 0), (0, 1), (0, 2)],
    "plus": [(1, 0), (0, 1), (1, 1), (2, 1), (1, 2)],
    "U": [(0, 0), (0, 1), (0, 2), (1, 2), (2, 0), (2, 1), (2, 2)],
    "stair": [(0, 0), (1, 0), (1, 1), (2, 1), (2, 2)],
}


def assemble(omni, cells):
    """按给定的一组坐标，用 omni 拼出那种形状，按游戏的方式选格。"""
    cell = omni.size[0] // OMNI_GRID
    width = max(x for x, _ in cells) + 1
    height = max(y for _, y in cells) + 1

    def filled(x, y):
        return (x, y) in cells

    out = Image.new("RGBA", (width * cell, height * cell), (0, 0, 0, 0))
    for x, y in cells:
        ctx = {
            "up": filled(x, y - 1), "down": filled(x, y + 1),
            "left": filled(x - 1, y), "right": filled(x + 1, y),
            "topLeft": filled(x - 1, y - 1), "topRight": filled(x + 1, y - 1),
            "bottomLeft": filled(x - 1, y + 1), "bottomRight": filled(x + 1, y + 1),
        }
        index = idx_omni(ctx)
        out.paste(omni.crop(((index % OMNI_GRID) * cell, (index // OMNI_GRID) * cell,
                             (index % OMNI_GRID) * cell + cell,
                             (index // OMNI_GRID) * cell + cell)),
                  (x * cell, y * cell))
    return out


def main(argv):
    if len(argv) < 4:
        print(__doc__.strip().split("\n\n")[-1])
        return 1

    out_dir, material, base_path, frame_path = argv[0], argv[1], argv[2], argv[3]
    art_dir = os.path.join(out_dir, "art")
    layout_dir = os.path.join(out_dir, "layouts")
    os.makedirs(art_dir, exist_ok=True)
    os.makedirs(layout_dir, exist_ok=True)

    base = Image.open(base_path).convert("RGBA")
    frame = Image.open(frame_path).convert("RGBA")

    # The base, repeated.
    large = Image.new("RGBA", (CELL * 4, CELL * 4), (0, 0, 0, 0))
    for row in range(4):
        for column in range(4):
            large.paste(base, (column * CELL, row * CELL))
    large.save(os.path.join(art_dir, f"{material}_base_large.png"))

    ct = build_rectangle_sheet(frame)
    ct.save(os.path.join(art_dir, f"{material}_ct.png"))

    corners = build_corner_tile(frame)
    corners.save(os.path.join(art_dir, f"{material}_r.png"))

    ctm = Image.alpha_composite(large, ct)
    ctm.save(os.path.join(art_dir, f"{material}_ctm.png"))

    omni, filled, placed = convert_to_omni(ctm, corners)
    omni.save(os.path.join(art_dir, f"{material}_omni.png"))

    for name in LAYOUT_ORDER:
        if name == "OMNIDIRECTIONAL":
            continue
        cut, _ = cut_layout(omni, name)
        cut.save(os.path.join(art_dir, f"{material}_{name.lower()}.png"))

    for shape, cells in SHAPES.items():
        assemble(omni, cells).save(os.path.join(layout_dir, f"{material}_{shape}.png"))

    print(f"  {material}: {filled} 格填充, {placed} 个角标")
    print(f"    art/     {len(LAYOUT_ORDER)} 张布局 + base/ct/r/ctm/omni")
    print(f"    layouts/ {len(SHAPES)} 种拼接预览")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
