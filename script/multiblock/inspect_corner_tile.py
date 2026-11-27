"""Report each quadrant of a corner-mark tile: what it holds and whether it is anchored correctly.

A corner tile is meant to carry one small mark per quadrant, in the corner of that quadrant, with the
rest transparent. Anything else — a mark spanning the whole quadrant, a quadrant empty, or a mark in
the wrong corner — makes the tile unusable for compositing, and each failure points at a different fix.

    python script/multiblock/inspect_corner_tile.py <tile.png>
"""
import sys

from PIL import Image


def main(argv):
    if not argv:
        print("usage: inspect_corner_tile.py <tile.png>")
        return 1

    image = Image.open(argv[0]).convert("RGBA")
    width, height = image.size
    pixels = image.load()
    half_w, half_h = width // 2, height // 2

    total = width * height
    opaque = sum(1 for y in range(height) for x in range(width) if pixels[x, y][3] > 16)
    print(f"  {argv[0]}: {width}x{height}, opaque {opaque}/{total} ({opaque * 100 // total}%)")
    print()

    quadrants = {
        "topLeft": (0, 0, half_w, half_h),
        "topRight": (half_w, 0, width, half_h),
        "bottomLeft": (0, half_h, half_w, height),
        "bottomRight": (half_w, half_h, width, height),
    }

    for name, (x0, y0, x1, y1) in quadrants.items():
        points = [(x, y) for y in range(y0, y1) for x in range(x0, x1)
                  if pixels[x, y][3] > 16]
        if not points:
            print(f"  {name:13s}: empty (fully transparent)")
            continue

        left = min(p[0] for p in points) - x0
        top = min(p[1] for p in points) - y0
        right = max(p[0] for p in points) - x0
        bottom = max(p[1] for p in points) - y0
        size = f"{right - left + 1}x{bottom - top + 1}"

        qw, qh = x1 - x0, y1 - y0
        horizontal = "left" if left == 0 else ("right" if right == qw - 1 else "middle")
        vertical = "top" if top == 0 else ("bottom" if bottom == qh - 1 else "middle")

        print(f"  {name:13s}: {len(points):4d} px  bbox {size:7s} "
              f"anchored {vertical}-{horizontal}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
