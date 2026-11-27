"""Compare two connected sheets cell by cell.

When one is known good and the other is the output being judged, the useful question is not "how many
pixels differ" but "which cells, and inside them which region". A cell whose difference is confined to
its corners is a corner-mark problem; one that differs along an edge is an edge problem; one that
differs in the middle is a base-pattern problem. Reporting the region turns a pixel count into a cause.
"""
import os
import sys

from PIL import Image

CELL = 32


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def region_of(lx, ly, cell):
    """Name the band of a cell a pixel falls in: corner, edge, or middle."""
    third = cell // 4
    near_left = lx < third
    near_right = lx >= cell - third
    near_top = ly < third
    near_bottom = ly >= cell - third

    if (near_left or near_right) and (near_top or near_bottom):
        x = "left" if near_left else "right"
        y = "top" if near_top else "bottom"
        return f"corner {y}-{x}"
    if near_left or near_right:
        return "edge left" if near_left else "edge right"
    if near_top or near_bottom:
        return "edge top" if near_top else "edge bottom"
    return "middle"


def main(argv):
    if len(argv) < 3:
        print("usage: diff_sheets.py <good.png> <suspect.png> <grid>")
        return 1
    good = Image.open(argv[0]).convert("RGBA")
    bad = Image.open(argv[1]).convert("RGBA")
    grid = int(argv[2])
    if good.size != bad.size:
        print(f"  sizes differ: {good.size} vs {bad.size}")
        return 1
    cell = good.size[0] // grid

    per_cell = {}
    per_region = {}
    per_kind = {}
    for y in range(good.size[1]):
        for x in range(good.size[0]):
            a = good.getpixel((x, y))
            b = bad.getpixel((x, y))
            if a == b:
                continue
            cx, cy = x // cell, y // cell
            index = cx + cy * grid
            per_cell[index] = per_cell.get(index, 0) + 1
            region = region_of(x % cell, y % cell, cell)
            per_region[region] = per_region.get(region, 0) + 1
            # Classify: did the good one have ink where the suspect does not, or the reverse?
            a_ink = a[3] > 0 and luminance(a) < 160
            b_ink = b[3] > 0 and luminance(b) < 160
            if a_ink and not b_ink:
                kind = "good has ink, suspect clean"
            elif b_ink and not a_ink:
                kind = "suspect has ink, good clean"
            else:
                kind = "both inked, tone differs"
            per_kind[kind] = per_kind.get(kind, 0) + 1

    print(f"  {os.path.basename(argv[0])} (good) vs {os.path.basename(argv[1])} (suspect)")
    print(f"  {grid}x{grid}, cell {cell}, differing pixels {sum(per_cell.values())}")
    print("  by region:")
    for region, count in sorted(per_region.items(), key=lambda kv: -kv[1]):
        print(f"    {region:22s} {count:6d}")
    print("  by kind:")
    for kind, count in sorted(per_kind.items(), key=lambda kv: -kv[1]):
        print(f"    {kind:28s} {count:6d}")
    print("  by cell (worst 12):")
    for index, count in sorted(per_cell.items(), key=lambda kv: -kv[1])[:12]:
        print(f"    cell {index:2d} (col {index % grid}, row {index // grid}): {count:5d}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
