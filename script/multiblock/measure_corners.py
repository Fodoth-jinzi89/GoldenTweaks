"""Measure corner ink per cell and compare it with the cell's diagonal state.

For a border-style sheet, a corner mark exists to bridge a concave junction — two open sides meeting
without a diagonal. So a cell whose corner is *connected* (diagonal present) must carry no mark there,
and a cell whose corner is *open with both adjoining sides open* is a convex outside corner and must
carry no mark either.

The 2x2 case is the check that matters: every cell there touches two neighbours, its outer corner is
convex and its inner corner is connected, so no cell may hold a mark. Any ink found in a corner
quadrant of this layout is a defect regardless of what the index formula says.

    python script/multiblock/measure_corners.py <sheet.png> [index ...]
"""
import itertools
import sys

from PIL import Image

SIDES = ("up", "down", "left", "right")
CORNERS = ("topLeft", "topRight", "bottomLeft", "bottomRight")

from convert_ct_rectangle_to_omni import omni_index


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def main(argv):
    if not argv:
        print("usage: measure_corners.py <sheet.png> [index ...]")
        return 1
    path = argv[0]
    wanted = [int(a) for a in argv[1:]]

    image = Image.open(path).convert("RGBA")
    cell = image.size[0] // 8
    pixels = image.load()
    tones = [luminance(pixels[x, y]) for y in range(image.size[1]) for x in range(image.size[0])
             if pixels[x, y][3] > 0]
    threshold = (min(tones) + max(tones)) // 2
    box = cell // 4

    states = {}
    for bits in itertools.product((False, True), repeat=8):
        ctx = dict(zip(SIDES + CORNERS, bits))
        states.setdefault(omni_index(ctx), ctx)

    def corner_ink(index, corner):
        column, row = index % 8, index // 8
        x0, y0 = column * cell, row * cell
        lx = x0 if corner in ("topLeft", "bottomLeft") else x0 + cell - box
        ty = y0 if corner in ("topLeft", "topRight") else y0 + cell - box
        return sum(1 for y in range(ty, ty + box) for x in range(lx, lx + box)
                   if pixels[x, y][3] > 0 and luminance(pixels[x, y]) < threshold)

    indices = wanted or sorted(states)
    print(f"  {path}: cell {cell}, split {threshold}, corner box {box}x{box}")
    print(f"  {'cell':>4} {'edges':11} {'diagonals':25} {'tl':>4}{'tr':>5}{'bl':>5}{'br':>5}")

    suspicious = []
    for index in indices:
        ctx = states[index]
        edges = "".join("Y" if ctx[s] else "n" for s in SIDES)
        diagonals = " ".join(f"{c[:2]}={'Y' if ctx[c] else 'n'}" for c in CORNERS)
        ink = [corner_ink(index, c) for c in CORNERS]
        print(f"  {index:4d} {edges:11} {diagonals:25} "
              f"{ink[0]:4d}{ink[1]:5d}{ink[2]:5d}{ink[3]:5d}")

        # A mark only makes sense where two connected sides meet without the diagonal.
        for corner, value in zip(CORNERS, ink):
            if value <= box:
                continue
            vertical = "up" if corner in ("topLeft", "topRight") else "down"
            horizontal = "left" if corner in ("topLeft", "bottomLeft") else "right"
            bridge = ctx[vertical] and ctx[horizontal] and not ctx[corner]
            if not bridge:
                suspicious.append((index, corner, value, edges))

    print()
    if suspicious:
        print(f"  ink in {len(suspicious)} corner(s) with nothing to bridge:")
        for index, corner, value, edges in suspicious:
            print(f"    cell {index:2d} ({edges}) {corner}: {value} dark px")
    else:
        print("  every corner mark bridges a real junction")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
