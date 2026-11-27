"""Test whether output cells are built by tiling the frame's eight-pixel middle band.

The cells repeat on an 8px period, which matches the frame's 8px middle band rather than any of its
12px border pieces. That would mean a cell is filled by repeating the interior band and then having
its open sides replaced by border. This checks the tiling part first, since everything else depends on
it.

    python script/multiblock/test_tile_period.py
"""
import os
import sys

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")
EDGES = (0, 12, 20, 32)


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def main():
    frame = load("template_frame.png")
    image = load("template_ct.png")
    cell = image.size[0] // 4

    # The frame's middle band: the 8x8 centre plus the 8-wide edges beside it.
    bands = {
        "c": frame.crop((12, 12, 20, 20)),
        "t_mid": frame.crop((12, 0, 20, 12)),
        "b_mid": frame.crop((12, 20, 20, 32)),
        "l_mid": frame.crop((0, 12, 12, 20)),
        "r_mid": frame.crop((20, 12, 32, 20)),
    }

    print(f"  frame bands: {[(k, v.size) for k, v in bands.items()]}")
    print()

    # Does every cell repeat on an 8px period?
    print("  per-cell 8px horizontal period check (row 0..31, column repeats):")
    for cy in range(4):
        for cx in range(4):
            tile = image.crop((cx * cell, cy * cell, (cx + 1) * cell, (cy + 1) * cell))
            horizontal_ok = all(
                tile.getpixel((x, y)) == tile.getpixel((x + 8, y))
                for y in range(cell) for x in range(cell - 8)
            )
            vertical_ok = all(
                tile.getpixel((x, y)) == tile.getpixel((x, y + 8))
                for y in range(cell - 8) for x in range(cell)
            )
            print(f"    cell ({cx},{cy}): horizontal 8px period={horizontal_ok}, "
                  f"vertical={vertical_ok}")

    # What is the actual content of one 8x8 unit, for a cell with only the top open?
    print()
    print("  cell (2,0) first 8px column strip, top to bottom:")
    tile = image.crop((2 * cell, 0, 3 * cell, cell))
    for y in range(cell):
        row = []
        for x in range(8):
            r, g, b, a = tile.getpixel((x, y))
            row.append(f"{r // 64}{g // 64}{b // 64}")
        print(f"    y={y:2d} " + " ".join(row))
    return 0


if __name__ == "__main__":
    sys.exit(main())
