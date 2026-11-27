"""Show the exact pixels that differ inside one corner quadrant.

A fixed 16-pixel difference in a 8x8 quadrant is not a missing mark — the quadrant is marked in both
sheets. It is a difference in how the mark is drawn: a shifted edge, a different corner pixel. Printing
both rasters side by side makes that visible.

    python script/multiblock/show_corner_diff.py <reference.png> <candidate.png> <index> <corner>
"""
import sys

from PIL import Image


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def main(argv):
    if len(argv) < 4:
        print("usage: show_corner_diff.py <reference.png> <candidate.png> <index> <corner>")
        return 1
    ref_path, cand_path, index, corner = argv[0], argv[1], int(argv[2]), argv[3]

    ref = Image.open(ref_path).convert("RGBA")
    cand = Image.open(cand_path).convert("RGBA")
    cell = ref.size[0] // 8
    quarter = cell // 4

    column, row = index % 8, index // 8
    x0, y0 = column * cell, row * cell
    if corner in ("topLeft", "bottomLeft"):
        lx = x0
    else:
        lx = x0 + cell - quarter
    if corner in ("topLeft", "topRight"):
        ly = y0
    else:
        ly = y0 + cell - quarter

    def raster(image):
        tones = [luminance(image.getpixel((x, y)))
                 for y in range(image.size[1]) for x in range(image.size[0])
                 if image.getpixel((x, y))[3] > 0]
        threshold = (min(tones) + max(tones)) // 2
        rows = []
        for y in range(ly, ly + quarter):
            chars = []
            for x in range(lx, lx + quarter):
                r, g, b, a = image.getpixel((x, y))
                if a < 32:
                    chars.append(" ")
                elif luminance((r, g, b)) < threshold:
                    chars.append("#")
                else:
                    chars.append(".")
            rows.append("".join(chars))
        return rows

    ref_rows = raster(ref)
    cand_rows = raster(cand)

    print(f"  idx {index} {corner}  ({quarter}x{quarter} at {lx},{ly})")
    print(f"    {'reference':<{quarter}}   {'candidate':<{quarter}}   diff")
    for y in range(quarter):
        marks = "".join("X" if ref_rows[y][x] != cand_rows[y][x] else "."
                        for x in range(quarter))
        print(f"    {ref_rows[y]}   {cand_rows[y]}   {marks}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
