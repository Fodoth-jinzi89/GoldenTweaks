"""对比两份 omni 图，把差异分成「可见」与「仅透明区 RGB」两类，并按格汇总。

透明像素的 RGB 分量不影响画面，但会把「差异像素数」灌满；分开统计才能看出真正要修的是哪几格。
"""
import os
import sys

from PIL import Image

CELL = 32
GRID = 8


def main(argv):
    if len(argv) < 2:
        print("usage: omni_diff.py <reference.png> <mine.png>")
        return 1
    ref = Image.open(argv[0]).convert("RGBA")
    mine = Image.open(argv[1]).convert("RGBA")
    if ref.size != mine.size:
        print(f"  尺寸不同 {ref.size} vs {mine.size}")
        return 1

    size = ref.size[0]
    per_cell = {}
    visible_total = 0
    invisible_total = 0

    for y in range(size):
        for x in range(size):
            a = ref.getpixel((x, y))
            b = mine.getpixel((x, y))
            if a == b:
                continue
            if a[3] > 16 or b[3] > 16:
                visible_total += 1
                key = (x // CELL, y // CELL)
                per_cell[key] = per_cell.get(key, 0) + 1
            else:
                invisible_total += 1

    print(f"  {os.path.basename(argv[0])} (基准) vs {os.path.basename(argv[1])}")
    print(f"  可见差异 {visible_total}，仅透明区 RGB 差异 {invisible_total}")

    if not per_cell:
        return 0

    print("  可见差异按格：")
    for (cx, cy), count in sorted(per_cell.items(), key=lambda kv: -kv[1])[:16]:
        index = cx + cy * GRID
        print(f"    格 {index:2d} (列 {cx}, 行 {cy}): {count} px")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
