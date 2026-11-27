"""Show which pixels of one output cell differ from the frame, as a bitmap.

The mismatch counts are regular (384, 622, 860, 636), which means the difference is structural rather
than noise. Printing which pixels differ — and in which direction they seem displaced — turns that
number into a shape that can be read.

    python script/multiblock/show_cell_diff.py <cx> <cy>
"""
import os
import sys

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def main(argv):
    cx = int(argv[0]) if argv else 0
    cy = int(argv[1]) if len(argv) > 1 else 0

    frame = load("template_frame.png")
    image = load("template_ct.png")
    fw, fh = frame.size
    cell_w, cell_h = image.size[0] // 4, image.size[1] // 4
    cell = image.crop((cx * cell_w, cy * cell_h, (cx + 1) * cell_w, (cy + 1) * cell_h))

    print(f"  cell ({cx},{cy}) {cell_w}x{cell_h} vs frame {fw}x{fh}")
    print("  X = pixel differs, . = same")
    print("      " + "".join(str(x % 10) for x in range(min(fw, cell_w))))
    for y in range(min(fh, cell_h)):
        row = []
        for x in range(min(fw, cell_w)):
            same = frame.getpixel((x, y)) == cell.getpixel((x, y))
            row.append("." if same else "X")
        print(f"  {y:3d} " + "".join(row))

    # Would a horizontal or vertical flip of one half explain it?
    print()
    print("  per-quadrant comparison (cell quadrant vs frame quadrant):")
    half_w, half_h = cell_w // 2, cell_h // 2
    for qy in range(2):
        for qx in range(2):
            cq = cell.crop((qx * half_w, qy * half_h, (qx + 1) * half_w, (qy + 1) * half_h))
            for fy in range(2):
                for fx in range(2):
                    fq = frame.crop((fx * half_w, fy * half_h,
                                     (fx + 1) * half_w, (fy + 1) * half_h))
                    diff = sum(1 for y in range(half_h) for x in range(half_w)
                               if cq.getpixel((x, y)) != fq.getpixel((x, y)))
                    if diff == 0:
                        print(f"    cell quadrant ({qx},{qy}) == frame quadrant ({fx},{fy}) exactly")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
