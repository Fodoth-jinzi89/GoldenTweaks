"""Print one cell of a sheet as a coarse map, for reading alongside another sheet's same cell.

Four symbols rather than two: a pixel is transparent (blank), base-coloured (dot), outline-coloured
(hash), or something else entirely (question mark). That last case is the one worth seeing — it means
the cell carries art the comparison did not anticipate.
"""
import os
import sys

from PIL import Image

SIDES = ("up", "down", "left", "right")


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def main(argv):
    if len(argv) < 2:
        print("usage: cell_map.py <sheet.png> <index> [grid]")
        return 1
    sheet = Image.open(argv[0]).convert("RGBA")
    index = int(argv[1])
    grid = int(argv[2]) if len(argv) > 2 else (8 if sheet.size[0] % 8 == 0 else 4)
    cell = sheet.size[0] // grid
    column, row = index % grid, index // grid

    print(f"  {os.path.basename(argv[0])} cell {index} ({grid}x{grid}, {cell}px)")
    for y in range(cell):
        chars = []
        for x in range(cell):
            pixel = sheet.getpixel((column * cell + x, row * cell + y))
            if pixel[3] < 16:
                chars.append(" ")
            elif luminance(pixel) < 150:
                chars.append("#")
            elif luminance(pixel) < 200:
                chars.append(":")
            else:
                chars.append(".")
        print(f"  {y:2d} {''.join(chars)}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
