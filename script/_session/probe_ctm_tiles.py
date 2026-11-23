"""Classify each OptiFine CTM tile by which of its four edges carry a border.

The 47-tile scheme draws a border on a side that does not connect and continues the panel on a side
that does. Reading that from the artwork is the only way to assign the mapping with confidence: the
index table is conventional but not something to rely on from memory, and the two sheets this mod
ships are hand-drawn, so they may not follow the convention exactly.

The border in these tiles is a dark violet band. Judged by relative darkness rather than an absolute
threshold, since the panel fill is itself tinted.
"""
import os
import sys

from PIL import Image

TILE = 16


def band_darkness(pixels, edge):
    """Mean luminance of the middle third of an edge."""
    if edge == "up":
        coords = [(x, 1) for x in range(4, 12)]
    elif edge == "down":
        coords = [(x, TILE - 2) for x in range(4, 12)]
    elif edge == "left":
        coords = [(1, y) for y in range(4, 12)]
    else:
        coords = [(TILE - 2, y) for y in range(4, 12)]

    total = 0.0
    count = 0
    for x, y in coords:
        r, g, b, a = pixels[x, y]
        if a < 32:
            continue
        total += (r + g + b) / 3.0
        count += 1
    return total / count if count else 255.0


def centre_luminance(pixels):
    total = 0.0
    count = 0
    for y in range(6, 10):
        for x in range(6, 10):
            r, g, b, a = pixels[x, y]
            if a < 32:
                continue
            total += (r + g + b) / 3.0
            count += 1
    return total / count if count else 255.0


def main():
    tile_dir = sys.argv[1] if len(sys.argv) > 1 else None
    if not tile_dir:
        print("usage: probe_ctm_tiles.py <tile dir>", file=sys.stderr)
        return 1

    rows = []
    for index in range(47):
        path = os.path.join(tile_dir, f"{index}.png")
        if not os.path.exists(path):
            continue
        image = Image.open(path).convert("RGBA")
        if image.size != (TILE, TILE):
            image = image.resize((TILE, TILE), Image.NEAREST)
        pixels = image.load()

        centre = centre_luminance(pixels)
        edges = {e: band_darkness(pixels, e) for e in ("up", "down", "left", "right")}
        # Bordered = noticeably darker than the panel fill.
        mask = 0
        for bit, edge in ((1, "up"), (2, "down"), (4, "left"), (8, "right")):
            if edges[edge] > centre - 25:
                mask |= bit
        rows.append((index, mask, centre, edges))

    print(f"{'tile':>4} {'mask':>4} {'bin':>5}  centre   up   down  left  right")
    for index, mask, centre, edges in rows:
        print(f"{index:>4} {mask:>4} {mask:04b}  {centre:6.1f} "
              f"{edges['up']:5.1f} {edges['down']:5.1f} {edges['left']:5.1f} {edges['right']:5.1f}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
