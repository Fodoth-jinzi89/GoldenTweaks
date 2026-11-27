"""Dump a sheet's structure after quantising colour, so anti-aliasing stops hiding the design.

A hand-drawn template has a few structural colours and many in-between pixels along every edge. Listing
all of them buries the shape, so this snaps each channel to `LEVELS` steps and prints the result as a
letter map, which shows the actual regions.

    python script/multiblock/quantise_sheet.py <image.png> [levels]
"""
import sys
from collections import Counter

from PIL import Image


def main(argv):
    if not argv:
        print("usage: quantise_sheet.py <image.png> [levels]")
        return 1
    path = argv[0]
    levels = int(argv[1]) if len(argv) > 1 else 4

    image = Image.open(path).convert("RGBA")
    width, height = image.size
    pixels = image.load()

    def quantise(pixel):
        r, g, b, a = pixel
        if a < 16:
            return None
        step = 256 // levels
        return (r // step, g // step, b // step)

    counts = Counter()
    for y in range(height):
        for x in range(width):
            key = quantise(pixels[x, y])
            counts[key] += 1

    ordered = [key for key, _ in counts.most_common()]
    letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#%&*+=~^"
    symbol = {key: letters[i] if i < len(letters) else "?" for i, key in enumerate(ordered)}

    step = 256 // levels
    print(f"  {path}: {width}x{height}, quantised to {levels} levels "
          f"({len(ordered)} groups)")
    print()
    print("  legend:")
    for key in ordered:
        if key is None:
            print(f"    {symbol[key]} : transparent : {counts[key]:4d} px")
        else:
            r, g, b = (c * step + step // 2 for c in key)
            print(f"    {symbol[key]} : ~({r:3d},{g:3d},{b:3d}) : {counts[key]:4d} px")

    print()
    print("      " + "".join(str(x % 10) for x in range(width)))
    for y in range(height):
        row = []
        for x in range(width):
            key = quantise(pixels[x, y])
            row.append(" " if key is None else symbol[key])
        print(f"  {y:3d} " + "".join(row))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
