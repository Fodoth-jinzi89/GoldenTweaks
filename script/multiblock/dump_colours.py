"""Print every pixel of a small sheet as its colour, grouped so the structure is readable.

A 32x32 template is too small to read by eye in an image viewer and too detailed to guess at. This
prints the actual raster: each distinct colour gets a letter, a legend maps letters back to RGB, and a
per-colour bounding box shows where each region sits. That is enough to tell a frame from a corner
mark and to see exactly which rows and columns each colour occupies.

    python script/multiblock/dump_colours.py <image.png>
"""
import sys
from collections import Counter

from PIL import Image


def main(argv):
    if not argv:
        print("usage: dump_colours.py <image.png>")
        return 1

    image = Image.open(argv[0]).convert("RGBA")
    width, height = image.size
    pixels = image.load()

    counts = Counter()
    for y in range(height):
        for x in range(width):
            counts[pixels[x, y]] += 1

    # Largest regions get letters first, so the main colours stay readable.
    ordered = [colour for colour, _ in counts.most_common()]
    letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    symbol = {}
    for index, colour in enumerate(ordered):
        symbol[colour] = letters[index] if index < len(letters) else "?"

    print(f"  {argv[0]}: {width}x{height}, {len(ordered)} distinct pixel values")
    print()
    print("  legend (letter : RGBA : count : bounding box):")
    bounds = {colour: [width, height, -1, -1] for colour in ordered}
    for y in range(height):
        for x in range(width):
            b = bounds[pixels[x, y]]
            b[0] = min(b[0], x)
            b[1] = min(b[1], y)
            b[2] = max(b[2], x)
            b[3] = max(b[3], y)

    for colour in ordered:
        x0, y0, x1, y1 = bounds[colour]
        r, g, b, a = colour
        print(f"    {symbol[colour]} : ({r:3d},{g:3d},{b:3d},{a:3d}) : {counts[colour]:4d} px : "
              f"x {x0:2d}..{x1:2d}  y {y0:2d}..{y1:2d}")

    print()
    print("      " + "".join(str(x % 10) for x in range(width)))
    for y in range(height):
        print(f"  {y:3d} " + "".join(symbol[pixels[x, y]] for x in range(width)))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
