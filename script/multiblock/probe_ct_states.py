"""从参考 _ct 直接测出每个索引对应哪几条边连通。

判断依据：连通的侧，外圈应当与 base 一致（被抹平）；未连通的侧，外圈应当与完整方块一致
（边框保留）。对四侧各量一次，就得到索引到状态的映射，不用假设 Create 的公式。
"""
import sys

from PIL import Image

CELL = 32


def outer_band(pixels, side, border):
    if side == "up":
        return [(x, y) for y in range(border) for x in range(CELL)]
    if side == "down":
        return [(x, y) for y in range(CELL - border, CELL) for x in range(CELL)]
    if side == "left":
        return [(x, y) for y in range(CELL) for x in range(border)]
    return [(x, y) for y in range(CELL) for x in range(CELL - border, CELL)]


def main(argv):
    ref = Image.open(argv[0]).convert("RGBA")
    full = Image.open(argv[1]).convert("RGBA")
    base = Image.open(argv[2]).convert("RGBA")
    border = int(argv[3]) if len(argv) > 3 else 4

    print(f"  参考 {argv[0]}，边框 {border}px")
    print("  idx  up  down left right   （= 与 base 相符的比例高则判为连通）")
    for index in range(16):
        cx, cy = (index % 4) * CELL, (index // 4) * CELL
        marks = []
        detail = []
        for side in ("up", "down", "left", "right"):
            points = outer_band(None, side, border)
            same_base = 0
            same_full = 0
            for x, y in points:
                pixel = ref.getpixel((cx + x, cy + y))
                if pixel == base.getpixel((x % CELL, y % CELL)):
                    same_base += 1
                if pixel == full.getpixel((x, y)):
                    same_full += 1
            connected = same_base > same_full
            marks.append("Y" if connected else "n")
            detail.append(f"{same_base}/{same_full}")
        print(f"  {index:2d}  {marks[0]:2s}  {marks[1]:4s} {marks[2]:4s} {marks[3]:5s}"
              f"    {detail}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
