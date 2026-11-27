"""Show exactly where one output cell differs from the frame, pixel by pixel.

The cell at (0,3) of `template_ct.png` looks like the frame but is reported as differing by 384 pixels.
384 of 1024 is a regular fraction, which suggests a systematic difference rather than noise — a
transposed axis, a half-cell shift, or 2x2 blocks swapped. This prints the differing coordinates so the
pattern is visible.

    python script/multiblock/diff_cell_vs_frame.py <cx> <cy>
"""
import os
import sys
from collections import Counter

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def main(argv):
    cx = int(argv[0]) if argv else 0
    cy = int(argv[1]) if len(argv) > 1 else 3

    frame = load("template_frame.png")
    image = load("template_ct.png")
    fw, fh = frame.size
    cell_w = image.size[0] // 4
    cell_h = image.size[1] // 4
    cell = image.crop((cx * cell_w, cy * cell_h, (cx + 1) * cell_w, (cy + 1) * cell_h))

    print(f"  frame {fw}x{fh}, cell ({cx},{cy}) {cell_w}x{cell_h}")

    differing = []
    for y in range(min(fh, cell_h)):
        for x in range(min(fw, cell_w)):
            a = frame.getpixel((x, y))
            b = cell.getpixel((x, y))
            if a != b:
                differing.append((x, y, a, b))

    print(f"  differing pixels: {len(differing)} of {fw * fh}")

    if differing:
        by_row = Counter(y for _, y, _, _ in differing)
        by_col = Counter(x for x, _, _, _ in differing)
        print(f"  rows touched: {sorted(by_row)}")
        print(f"  counts per row: {[by_row[y] for y in sorted(by_row)]}")
        print(f"  columns touched: {sorted(by_col)[:20]}")

        # A shift hypothesis: does cell(x,y) equal frame(x+dx, y+dy)?
        print()
        print("  shift test (does the cell equal the frame offset by dx, dy?):")
        best = []
        for dy in range(-2, 3):
            for dx in range(-2, 3):
                mismatch = 0
                counted = 0
                for y in range(fh):
                    for x in range(fw):
                        sx, sy = x + dx, y + dy
                        if not (0 <= sx < fw and 0 <= sy < fh):
                            continue
                        counted += 1
                        if frame.getpixel((sx, sy)) != cell.getpixel((x, y)):
                            mismatch += 1
                best.append((mismatch, dx, dy, counted))
        best.sort()
        for mismatch, dx, dy, counted in best[:5]:
            print(f"    dx={dx:+d} dy={dy:+d}: {mismatch} mismatched of {counted}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
