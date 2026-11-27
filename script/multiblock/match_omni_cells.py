"""Recover how each octo-directional cell was built, from a known-good pair.

Given the 4x4 sheet and the octo-directional sheet that was accepted, every cell of the latter can be
matched to the cell of the former it came from, plus whatever corner marks were laid on top. Printing
that mapping is the shortest path to the rule: it says which source cell and which marks, for all 47
cells at once, with no guesswork.

The match is by content: a cell's interior is found in the 4x4 sheet, and the pixels that do not match
are the marks.
"""
import os
import sys

from PIL import Image

CELL = 32
RECT = 4
OMNI = 8


def diff(a, b):
    total = 0
    for y in range(CELL):
        for x in range(CELL):
            if a.getpixel((x, y)) != b.getpixel((x, y)):
                total += 1
    return total


def main(argv):
    if len(argv) < 2:
        print("usage: match_omni_cells.py <ctm_4x4.png> <omni.png>")
        return 1
    rect = Image.open(argv[0]).convert("RGBA")
    omni = Image.open(argv[1]).convert("RGBA")
    if rect.size[0] != CELL * RECT or omni.size[0] != CELL * OMNI:
        print("  unexpected sizes")
        return 1

    sources = {}
    for index in range(RECT * RECT):
        column, row = index % RECT, index // RECT
        sources[index] = rect.crop((column * CELL, row * CELL,
                                    (column + 1) * CELL, (row + 1) * CELL))

    print(f"  matching {os.path.basename(argv[1])} cells against the 4x4 sheet")
    print("  index  best-source  pixels-off")
    for index in range(OMNI * OMNI):
        column, row = index % OMNI, index // OMNI
        cell = omni.crop((column * CELL, row * CELL, (column + 1) * CELL, (row + 1) * CELL))
        best, best_diff = None, CELL * CELL + 1
        for source_index, source in sources.items():
            d = diff(source, cell)
            if d < best_diff:
                best, best_diff = source_index, d
        blank = all(cell.getpixel((x, y))[3] < 16 for y in range(CELL) for x in range(CELL))
        if blank:
            print(f"    {index:2d}   (empty)")
        else:
            print(f"    {index:2d}   rect {best:2d}      {best_diff:4d}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
