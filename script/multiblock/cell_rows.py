"""Show exactly which pixels a known-good cell has, and what they are made of.

Reading a cell as characters loses the distinction that matters here: a border pixel and a base pixel
can both be "dark enough to look like ink" while being completely different colours. This prints each
row's colour runs instead, so a border band can be told from decorative noise by its uniformity.
"""
import os
import sys
from itertools import groupby

from PIL import Image


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


SYMBOL = {"transparent": " ", "base": ".", "border": "#", "other": "?"}


def classify(pixel, base_colours, border_colours):
    if pixel[3] < 16:
        return "transparent"
    if pixel in border_colours:
        return "border"
    if pixel in base_colours:
        return "base"
    return "other"


def runs(row_pixels, base_colours, border_colours):
    out = []
    for kind, group in groupby(row_pixels, key=lambda p: classify(p, base_colours, border_colours)):
        out.append(f"{SYMBOL[kind]}{len(list(group))}")
    return " ".join(out)


def main(argv):
    if len(argv) < 3:
        print("usage: cell_rows.py <sheet.png> <index> <base.png> [grid]")
        return 1
    sheet = Image.open(argv[0]).convert("RGBA")
    index = int(argv[1])
    base = Image.open(argv[2]).convert("RGBA")
    grid = int(argv[3]) if len(argv) > 3 else 8
    cell = sheet.size[0] // grid
    column, row = index % grid, index // grid

    base_colours = {base.getpixel((x, y)) for y in range(base.size[1]) for x in range(base.size[0])
                    if base.getpixel((x, y))[3] > 16}
    # Border colours: anything the sheet uses that the base does not.
    border_colours = set()
    for y in range(sheet.size[1]):
        for x in range(sheet.size[0]):
            pixel = sheet.getpixel((x, y))
            if pixel[3] > 16 and pixel not in base_colours:
                border_colours.add(pixel)

    print(f"  {os.path.basename(argv[0])} cell {index} ({grid}x{grid})")
    print(f"  base colours {len(base_colours)}, non-base colours {len(border_colours)}")
    for y in range(cell):
        pixels = [sheet.getpixel((column * cell + x, row * cell + y)) for x in range(cell)]
        print(f"  {y:2d} {runs(pixels, base_colours, border_colours)}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
