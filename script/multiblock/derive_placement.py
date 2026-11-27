"""Derive, per cell, exactly which frame pieces appear and where.

Instead of assuming a rule and testing it, this measures the hand-made sheet: for every cell it slides
the whole frame template across it and records where the template matches exactly. A match at offset
(dx, dy) means that cell was built by placing the frame there, so the set of matches per cell is the
real instruction list.

    python script/multiblock/derive_placement.py
"""
import os
import sys

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def main():
    frame = load("template_frame.png")
    ref = load("template_ct.png")
    fw, fh = frame.size
    cell = ref.size[0] // 4

    print(f"  frame {fw}x{fh}, cell {cell}px — frame offsets that reproduce part of each cell")

    for cy in range(4):
        for cx in range(4):
            tile = ref.crop((cx * cell, cy * cell, (cx + 1) * cell, (cy + 1) * cell))
            hits = []
            for dy in range(0, cell - fh + 1):
                for dx in range(0, cell - fw + 1):
                    if all(tile.getpixel((dx + x, dy + y)) == frame.getpixel((x, y))
                           for y in range(fh) for x in range(fw)):
                        hits.append((dx, dy))
            print(f"    cell ({cx},{cy}): frame fits at {hits}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
