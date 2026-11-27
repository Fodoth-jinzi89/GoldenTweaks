"""Verify the nine-patch layout the sheet is built from.

The frame is cut into nine pieces on a 12/8/12 grid. A cell is then assembled from a fixed 8x8 block
plan: border pieces on the outside, and the centre piece tiled through the middle. This checks that
plan piece by piece against the hand-made sheet, so the generator is written from a verified rule
rather than from a plausible one.

    python script/multiblock/verify_ninepatch_plan.py
"""
import os
import sys

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")

# Nine-patch cuts.
CUTS = (0, 12, 20, 32)
SLOTS = ("a", "b", "c", "d", "e", "f", "g", "h", "i")


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def main():
    frame = load("template_frame.png")
    image = load("template_ct.png")
    cell = image.size[0] // 4

    pieces = {}
    index = 0
    for yi in range(3):
        for xi in range(3):
            pieces[SLOTS[index]] = frame.crop((CUTS[xi], CUTS[yi],
                                               CUTS[xi + 1], CUTS[yi + 1]))
            index += 1

    centre = pieces["e"]
    print(f"  frame pieces: " + ", ".join(f"{k}={v.size[0]}x{v.size[1]}" for k, v in pieces.items()))

    def at(tile, x, y):
        return tile.getpixel((x, y))

    def region_matches_centre(tile, x0, y0, x1, y1):
        """Whether a region equals the centre piece tiled with its 12px phase."""
        for y in range(y0, y1):
            for x in range(x0, x1):
                if at(tile, x, y) != centre.getpixel(((x - 12) % 8, (y - 12) % 8)):
                    return False
        return True

    def region_matches_piece(tile, slot, x0, y0):
        source = pieces[slot]
        w, h = source.size
        return all(at(tile, x0 + dx, y0 + dy) == source.getpixel((dx, dy))
                   for dy in range(h) for dx in range(w))

    # Probe the plan against one cell that has every side open and one that has a side closed.
    probes = [
        ("cell (2,1) all connected", image.crop((2 * cell, 1 * cell, 3 * cell, 2 * cell))),
        ("cell (2,2) down open", image.crop((2 * cell, 2 * cell, 3 * cell, 3 * cell))),
        ("cell (0,0) up+left open", image.crop((0, 0, cell, cell))),
    ]

    for label, tile in probes:
        print(f"  {label}:")
        print(f"    whole cell is centre-tiled: {region_matches_centre(tile, 0, 0, cell, cell)}")
        for slot in SLOTS:
            x0 = CUTS[SLOTS.index(slot) % 3]
            y0 = CUTS[SLOTS.index(slot) // 3]
            w, h = pieces[slot].size
            if x0 + w > cell or y0 + h > cell:
                continue
            print(f"    {slot} at ({x0},{y0}): {region_matches_piece(tile, slot, x0, y0)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
