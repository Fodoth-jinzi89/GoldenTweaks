"""Test whether each output cell is a quadrant of the frame scaled up, rather than a whole copy.

`template_ct.png` is 4x4 cells of 32px while the frame is 32px overall, so a cell cannot be a 1:1 copy
of a quadrant (that would be 16px). If the cell is a quadrant blown up 2x, the mapping is still
crop-and-scale — no redrawing — and the generator can produce it. This checks that by scaling each
quadrant to the cell size and comparing.

    python script/multiblock/test_scaled_quadrants.py
"""
import os
import sys

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")

TRANSFORMS = {
    "id": lambda im: im,
    "flipx": lambda im: im.transpose(Image.Transpose.FLIP_LEFT_RIGHT),
    "flipy": lambda im: im.transpose(Image.Transpose.FLIP_TOP_BOTTOM),
    "rot180": lambda im: im.transpose(Image.Transpose.ROTATE_180),
    "rot90": lambda im: im.transpose(Image.Transpose.ROTATE_90),
    "rot270": lambda im: im.transpose(Image.Transpose.ROTATE_270),
}

# Nearest-neighbour keeps the comparison honest: a smooth filter would blur every edge and make any
# candidate score badly, hiding a genuine match.
RESAMPLE = Image.Resampling.NEAREST


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def pixels(image):
    return list(image.getdata())


def main():
    frame = load("template_frame.png")
    fw, fh = frame.size
    quad = fw // 2

    quadrants = {}
    for qy in range(2):
        for qx in range(2):
            quadrants[(qx, qy)] = frame.crop((qx * quad, qy * quad,
                                              (qx + 1) * quad, (qy + 1) * quad))

    image = load("template_ct.png")
    out_w, out_h = image.size
    grid = 4
    cell_w, cell_h = out_w // grid, out_h // grid
    print(f"  cell {cell_w}x{cell_h}, quadrant {quad}x{quad}, scale {cell_w // quad}x")

    for cy in range(grid):
        for cx in range(grid):
            cell = image.crop((cx * cell_w, cy * cell_h,
                               (cx + 1) * cell_w, (cy + 1) * cell_h))
            target = pixels(cell)

            best = None
            for key, source in quadrants.items():
                for label, transform in TRANSFORMS.items():
                    candidate = transform(source).resize((cell_w, cell_h), RESAMPLE)
                    diff = sum(1 for a, b in zip(pixels(candidate), target) if a != b)
                    if best is None or diff < best[0]:
                        best = (diff, key, label)

            # Also try the whole frame scaled down, for completeness.
            whole = frame.resize((cell_w, cell_h), RESAMPLE)
            whole_diff = sum(1 for a, b in zip(pixels(whole), target) if a != b)
            if whole_diff < best[0]:
                best = (whole_diff, "whole", "id")

            diff, key, label = best
            mark = "exact" if diff == 0 else f"{diff} px off"
            print(f"  cell ({cx},{cy}): {key}:{label:<8} {mark}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
