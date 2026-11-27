"""Resolve each output cell to a transform of the whole frame template.

`template_ct.png` is 4x4 cells of 32px and the frame is 32px, so each output cell is exactly one
transformed copy of the whole frame. `template_r.png` is 32px in total — 2x2 cells of 16px — so there
one cell is a quarter of the frame.

Reports, per cell, which transform reproduces it exactly, plus the pixel difference when nothing does,
so a near miss is not silently reported as a failure of the whole approach.

    python script/multiblock/map_whole_template.py
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
    "transpose": lambda im: im.transpose(Image.Transpose.TRANSPOSE),
    "transverse": lambda im: im.transpose(Image.Transpose.TRANSVERSE),
}


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def pixels(image):
    return list(image.getdata())


def main():
    frame = load("template_frame.png")
    fw, fh = frame.size

    variants = {}
    for label, transform in TRANSFORMS.items():
        variants[label] = pixels(transform(frame))

    for name in ("template_ct.png", "template_r.png"):
        path = os.path.join(FOLDER, name)
        if not os.path.exists(path):
            continue
        image = load(name)
        out_w, out_h = image.size
        print(f"  === {name}: {out_w}x{out_h} ===")

        if out_w == fw and out_h == fh:
            # Whole-image case: one transform reproduces the entire file.
            target = pixels(image)
            scored = sorted((sum(1 for a, b in zip(cand, target) if a != b), label)
                            for label, cand in variants.items())
            best = scored[0]
            print(f"    whole file: closest {best[1]} ({best[0]} differing px)"
                  + (", exact" if best[0] == 0 else "  -- not transform-only"))
            print()
            continue

        # Cell grid: figure out how many cells tile the file at a sane size.
        for grid in (4,):
            if out_w % grid or out_h % grid:
                continue
            cell_w, cell_h = out_w // grid, out_h // grid
            print(f"    {grid}x{grid} cells of {cell_w}x{cell_h}")
            for cy in range(grid):
                line = []
                for cx in range(grid):
                    cell = image.crop((cx * cell_w, cy * cell_h,
                                       (cx + 1) * cell_w, (cy + 1) * cell_h))
                    cell_pixels = pixels(cell)
                    best = None
                    for label, cand in variants.items():
                        if len(cand) != len(cell_pixels):
                            continue
                        count = sum(1 for a, b in zip(cand, cell_pixels) if a != b)
                        if best is None or count < best[0]:
                            best = (count, label)
                    line.append(f"{best[1]}:{best[0]:<4}" if best else "size?")
                print("      " + " ".join(f"{v:<12}" for v in line))
            print()
    return 0


if __name__ == "__main__":
    sys.exit(main())
