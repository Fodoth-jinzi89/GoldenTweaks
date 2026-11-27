"""Compare a good 4x4 cell against the frame it should have been built from.

The 4x4 sheet's cell for "all four sides open" is the frame itself: nothing is merged, so nothing is
removed. If that cell does not equal the frame, the frame is not the drawing the sheet was made from,
and no amount of adjusting the merge rule will close the gap.
"""
import os
import sys

from PIL import Image

CELL = 32
SIDES = ("up", "down", "left", "right")

# RECTANGLE index -> (left, right, up, down) as Create defines it.
def rectangle_state(index):
    x = index % 4
    y = index // 4
    left = x in (2, 3)
    right = x in (0, 1)
    up = y in (2, 3)
    down = y in (0, 1)
    return {"left": left, "right": right, "up": up, "down": down}


def cell_of(sheet, index, grid=4):
    cell = sheet.size[0] // grid
    column, row = index % grid, index // grid
    return sheet.crop((column * cell, row * cell, (column + 1) * cell, (row + 1) * cell))


def main(argv):
    if len(argv) < 3:
        print("usage: cell_vs_frame.py <ctm_4x4.png> <frame.png> [index]")
        return 1
    sheet = Image.open(argv[0]).convert("RGBA")
    frame = Image.open(argv[1]).convert("RGBA")

    indices = [int(argv[2])] if len(argv) > 2 else range(16)
    for index in indices:
        got = cell_of(sheet, index)
        state = rectangle_state(index)
        edges = "".join("Y" if state[s] else "n" for s in SIDES)

        diff = sum(1 for y in range(CELL) for x in range(CELL)
                   if got.getpixel((x, y)) != frame.getpixel((x, y)))
        print(f"  cell {index:2d} ({edges}): {diff:4d} px differ from the frame")

        if diff:
            print("    good cell (first 10 rows, # = differs from frame):")
            for y in range(10):
                marks = "".join("#" if got.getpixel((x, y)) != frame.getpixel((x, y)) else "."
                                for x in range(CELL))
                print(f"      {y:2d} {marks}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
