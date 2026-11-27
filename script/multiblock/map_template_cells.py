"""Map every 8x8 cell of the outputs back to a cell of the frame template.

The outputs are 4x4 grids of 32px cells; the frame is a 2x2 grid of 16px quadrants. Tracing each output
cell to a frame cell — and saying which transform of it — turns the whole generation into a table. If
every output cell resolves to exactly one transformed frame cell, generation is a pure rearrangement
and can be reproduced exactly.

    python script/multiblock/map_template_cells.py
"""
import os
import sys
from collections import Counter

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


def main():
    frame = load("template_frame.png")
    fw, fh = frame.size

    # Candidate source cells: the four quadrants, and the whole frame, all as crops.
    quad_w, quad_h = fw // 2, fh // 2
    sources = {}
    for qy in range(2):
        for qx in range(2):
            sources[(qx, qy)] = frame.crop((qx * quad_w, qy * quad_h,
                                            (qx + 1) * quad_w, (qy + 1) * quad_h))
    sources["whole"] = frame

    for name in ("template_ct.png", "template_r.png"):
        path = os.path.join(FOLDER, name)
        if not os.path.exists(path):
            continue
        image = load(name)
        out_w, out_h = image.size
        grid = 4
        cell_w, cell_h = out_w // grid, out_h // grid
        print(f"  === {name}: {out_w}x{out_h}, {grid}x{grid} cells of {cell_w}x{cell_h} ===")

        table = {}
        for cy in range(grid):
            for cx in range(grid):
                cell = image.crop((cx * cell_w, cy * cell_h,
                                   (cx + 1) * cell_w, (cy + 1) * cell_h))
                found = None
                for key, source in sources.items():
                    if source.size != cell.size:
                        continue
                    for label, transform in TRANSFORMS.items():
                        if list(transform(source).getdata()) == list(cell.getdata()):
                            found = (key, label)
                            break
                    if found:
                        break
                table[(cx, cy)] = found

        resolved = sum(1 for v in table.values() if v)
        print(f"    cells resolved: {resolved}/{grid * grid}")
        for cy in range(grid):
            line = []
            for cx in range(grid):
                value = table[(cx, cy)]
                if value is None:
                    line.append("      ?      ")
                else:
                    key, label = value
                    src = "W" if key == "whole" else f"{key[0]}{key[1]}"
                    line.append(f"{src}:{label:<8}")
            print("    " + " ".join(line))
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main())
