"""Work out where a known-good cell's pixels came from: the frame, or the base.

Each pixel of a good cell is looked up in both sources. Counting which source supplies it — and at
which position — reconstructs the rule that produced the cell, instead of guessing at it.
"""
import os
import sys
from collections import Counter

from PIL import Image

CELL = 32


def main(argv):
    if len(argv) < 4:
        print("usage: attribute_cell.py <good.png> <index> <frame.png> <base.png> [grid]")
        return 1
    good = Image.open(argv[0]).convert("RGBA")
    index = int(argv[1])
    frame = Image.open(argv[2]).convert("RGBA")
    base = Image.open(argv[3]).convert("RGBA")
    grid = int(argv[4]) if len(argv) > 4 else 8
    cell = good.size[0] // grid
    column, row = index % grid, index // grid

    frame_pixels = {(x, y): frame.getpixel((x, y)) for y in range(frame.size[1])
                    for x in range(frame.size[0])}
    base_of = lambda x, y: base.getpixel((x % base.size[0], y % base.size[1]))

    tally = Counter()
    per_row = {}
    for ly in range(cell):
        kinds = []
        for lx in range(cell):
            want = good.getpixel((column * cell + lx, row * cell + ly))
            if want[3] < 16:
                kinds.append(" ")
                tally["transparent"] += 1
                continue
            if frame_pixels.get((lx, ly)) == want:
                kinds.append("F")
                tally["frame at same position"] += 1
            elif base_of(lx, ly) == want:
                kinds.append("b")
                tally["base at same position"] += 1
            else:
                kinds.append("?")
                tally["neither"] += 1
        per_row[ly] = "".join(kinds)

    print(f"  {os.path.basename(argv[0])} cell {index}")
    for ly in range(cell):
        print(f"  {ly:2d} {per_row[ly]}")
    print()
    for kind, count in tally.most_common():
        print(f"  {kind:26s} {count:5d}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
