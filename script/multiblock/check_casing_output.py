"""检查生成结果的关键性质，不依赖已经删掉的中间产物。

三条断言：
  1. 基础方块贴图 == frame 叠 base；
  2. 八向连接图里，四边全通的格是纯底纹（没有边框残留）；
  3. 图集登记齐全，否则连接效果会被静默跳过。
"""
import json
import os
import sys

from PIL import Image

ASSETS = os.path.join("src", "main", "resources", "assets", "goldentweaks")
TEXTURES = os.path.join(ASSETS, "textures", "block")
ATLAS = os.path.join(ASSETS, "atlases", "blocks.json")

ARTS = os.path.join("arts", "multiblock")
LAYOUTS = ("horizontal", "horizontal_kryppers", "vertical",
           "roof", "roof_stair", "cross", "rectangle")


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def main():
    ok = True

    for material in ("pe", "pp"):
        base = Image.open(os.path.join(ARTS, f"shell_{material}_base.png")).convert("RGBA")
        frame = Image.open(os.path.join(ARTS, f"shell_{material}_frame.png")).convert("RGBA")
        expected = Image.alpha_composite(base, frame)
        plain = Image.open(os.path.join(TEXTURES, f"shell_{material}.png")).convert("RGBA")
        diff = sum(1 for y in range(32) for x in range(32)
                   if expected.getpixel((x, y)) != plain.getpixel((x, y)))
        print(f"  {material}: 基础贴图 == frame叠base ? {diff == 0}  ({diff}/1024)")
        ok = ok and diff == 0

        omni = Image.open(os.path.join(TEXTURES, f"shell_{material}_ctm.png")).convert("RGBA")
        # 八向图里，四边全通的格（idx 54）应当是纯底纹。
        cell = omni.size[0] // 8
        x0, y0 = (54 % 8) * cell, (54 // 8) * cell
        inked = sum(1 for y in range(cell) for x in range(cell)
                    if omni.getpixel((x0 + x, y0 + y))[3] > 0
                    and luminance(omni.getpixel((x0 + x, y0 + y))) < 150)
        print(f"  {material}: 八向图 {omni.size[0]}x{omni.size[1]}, "
              f"全通格边框像素 {inked} (应为 0)")
        ok = ok and omni.size[0] == 256 and inked == 0

    with open(ATLAS, encoding="utf-8") as handle:
        resources = {entry.get("resource") for entry in json.load(handle).get("sources", [])}
    needed = [f"goldentweaks:block/shell_{m}_ctm" for m in ("pe", "pp")]
    needed += [f"goldentweaks:block/ct_test_{m}_{l}_ctm"
               for m in ("pe", "pp") for l in LAYOUTS]
    missing = [r for r in needed if r not in resources]
    print(f"  图集: 需要 {len(needed)} 条，缺失 {len(missing)}")
    for resource in missing:
        print(f"    缺 {resource}")
    ok = ok and not missing

    print(f"  {'全部通过' if ok else '有问题'}")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
