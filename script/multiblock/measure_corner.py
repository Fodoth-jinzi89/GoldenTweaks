"""Measure how far a frame's corner decoration reaches, per corner.

The amount of a frame that a connected side must erase is not a fixed number: it is however far that
frame's corner art reaches inward. A material whose corner is a tight 4x4 needs only 4px cleared, while
one drawn at 6x6 needs 6 — clearing a fixed 6 for the first eats legitimate edge art, and clearing less
than needed leaves specks where four blocks meet.

This reports the extent by finding, inside each corner cell, the last row and column that still differs
from the plain edge strip.
"""
import os
import sys

from PIL import Image

CELL = 32
GRID = (0, 6, 26, 32)


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def corner_extent(frame, corner):
    """How many pixels of the corner cell carry art that differs from the edge strip."""
    x0 = 0 if corner[0] == "left" else GRID[2]
    y0 = 0 if corner[1] == "top" else GRID[2]
    width = GRID[1] if corner[0] == "left" else CELL - GRID[2]
    height = GRID[1] if corner[1] == "top" else CELL - GRID[2]

    pixels = frame.load()

    # The edge strip beside this corner, used as the "plain" reference.
    if corner[1] == "top":
        strip_y = 0
        strip = [pixels[x, strip_y] for x in range(GRID[1], GRID[2])]
    else:
        strip_y = CELL - 1
        strip = [pixels[x, strip_y] for x in range(GRID[1], GRID[2])]
    plain = max(set(strip), key=strip.count)

    reach_x = 0
    reach_y = 0
    for dy in range(height):
        for dx in range(width):
            x = x0 + dx if corner[0] == "left" else x0 + width - 1 - dx
            y = y0 + dy if corner[1] == "top" else y0 + height - 1 - dy
            pixel = pixels[x, y]
            if pixel[3] > 16 and pixel[:3] != plain[:3] and luminance(pixel) < 160:
                reach_x = max(reach_x, dx + 1)
                reach_y = max(reach_y, dy + 1)
    return reach_x, reach_y, plain


def main(argv):
    if not argv:
        print("usage: measure_corner.py <frame.png> [frame.png ...]")
        return 1
    for path in argv:
        frame = Image.open(path).convert("RGBA")
        print(f"  {os.path.basename(path)}")
        widest = 0
        for corner in (("left", "top"), ("right", "top"), ("left", "bottom"), ("right", "bottom")):
            reach_x, reach_y, plain = corner_extent(frame, corner)
            widest = max(widest, reach_x, reach_y)
            print(f"    {corner[0]:5s}/{corner[1]:6s}: reaches {reach_x}x{reach_y}px")
        print(f"    widest reach: {widest}px")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
