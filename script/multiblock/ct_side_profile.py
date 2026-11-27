"""统计参考 _ct 十六格的边框分布，用来反推「frame -> ct」的规则。

对每一格量四件事：最外两行/两列的不透明比例。有边框的边应当接近满，没有的接近零。
再看角区（6x6）和中区，就能分辨「边框」「边装饰」「角」「角装饰」四类像素各自在哪些状态下保留。
"""
import sys

from PIL import Image

CELL = 32


def band_ratio(pixels, cell, side, thickness=2):
    if side == "up":
        points = [(x, y) for y in range(thickness) for x in range(cell)]
    elif side == "down":
        points = [(x, y) for y in range(cell - thickness, cell) for x in range(cell)]
    elif side == "left":
        points = [(x, y) for y in range(cell) for x in range(thickness)]
    else:
        points = [(x, y) for y in range(cell) for x in range(cell - thickness, cell)]
    solid = sum(1 for x, y in points if pixels[x, y][3] > 16)
    return solid / len(points)


def region_ratio(pixels, cell, x0, y0, x1, y1):
    points = [(x, y) for y in range(y0, y1) for x in range(x0, x1)]
    solid = sum(1 for x, y in points if pixels[x, y][3] > 16)
    return solid / len(points)


def main(argv):
    sheet = Image.open(argv[0]).convert("RGBA")
    cell = sheet.size[0] // 4

    print("  idx  up   down  left  right | corner(6x6) 中区(20x20)")
    for index in range(16):
        column, row = index % 4, index // 4
        tile = sheet.crop((column * cell, row * cell,
                           (column + 1) * cell, (row + 1) * cell))
        pixels = tile.load()
        sides = [band_ratio(pixels, cell, s) for s in ("up", "down", "left", "right")]
        corner = region_ratio(pixels, cell, 0, 0, 6, 6)
        middle = region_ratio(pixels, cell, 6, 6, 26, 26)
        marks = "".join("Y" if value > 0.5 else ("." if value > 0.05 else "n") for value in sides)
        print(f"  {index:2d}  {marks[0]:3s} {marks[1]:5s} {marks[2]:5s} {marks[3]:6s} |"
              f"  {corner:6.0%}       {middle:5.0%}")

    print()
    print("  逐边比例（>0.9 才算边框）:")
    for index in range(16):
        column, row = index % 4, index // 4
        tile = sheet.crop((column * cell, row * cell,
                           (column + 1) * cell, (row + 1) * cell))
        pixels = tile.load()
        sides = [band_ratio(pixels, cell, s) for s in ("up", "down", "left", "right")]
        print(f"  {index:2d}  " + "  ".join(f"{value:4.0%}" for value in sides))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
