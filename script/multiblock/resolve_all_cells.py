"""Resolve every output cell against every transform of the frame, comparing pixel by pixel.

Reports the exact transform for each cell, and for cells that match nothing, the closest candidate and
its mismatch count. Working pixel by pixel avoids the ordering trap of comparing flattened pixel lists
from differently sized crops.

    python script/multiblock/resolve_all_cells.py
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


def compare(a, b):
    """Exact pixel mismatches between two same-sized images."""
    w, h = a.size
    if b.size != a.size:
        return None
    return sum(1 for y in range(h) for x in range(w)
               if a.getpixel((x, y)) != b.getpixel((x, y)))


def main():
    frame = load("template_frame.png")

    for name in ("template_ct.png", "template_r.png"):
        path = os.path.join(FOLDER, name)
        if not os.path.exists(path):
            continue
        image = load(name)
        out_w, out_h = image.size
        print(f"  === {name}: {out_w}x{out_h} ===")

        for grid in (4,):
            cell_w, cell_h = out_w // grid, out_h // grid
            print(f"  {grid}x{grid} cells of {cell_w}x{cell_h}")
            for cy in range(grid):
                for cx in range(grid):
                    cell = image.crop((cx * cell_w, cy * cell_h,
                                       (cx + 1) * cell_w, (cy + 1) * cell_h))

                    # Candidate 1: whole frame resized to the cell.
                    resized = frame.resize((cell_w, cell_h), Image.Resampling.NEAREST)
                    results = [("whole:resize", compare(resized, cell))]

                    # Candidate 2: whole frame at 1:1, only when sizes agree.
                    exact = compare(frame, cell)
                    if exact is not None:
                        for label, transform in TRANSFORMS.items():
                            diff = compare(transform(frame), cell)
                            if diff is not None:
                                results.append((f"whole:{label}", diff))

                    # Candidate 3: each quadrant scaled to the cell.
                    quad = frame.size[0] // 2
                    for qy in range(2):
                        for qx in range(2):
                            source = frame.crop((qx * quad, qy * quad,
                                                 (qx + 1) * quad, (qy + 1) * quad))
                            for label, transform in TRANSFORMS.items():
                                scaled = transform(source).resize(
                                    (cell_w, cell_h), Image.Resampling.NEAREST)
                                results.append((f"q{qx}{qy}:{label}", compare(scaled, cell)))

                    results = [r for r in results if r[1] is not None]
                    results.sort(key=lambda r: r[1])
                    diff, label = results[0][1], results[0][0]
                    mark = "EXACT" if diff == 0 else f"{diff} px off"
                    print(f"    cell ({cx},{cy}): {label:<16} {mark}")
            print()
    return 0


if __name__ == "__main__":
    sys.exit(main())
