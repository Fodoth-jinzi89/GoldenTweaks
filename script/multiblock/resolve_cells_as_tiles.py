"""Resolve each 32px output cell into its four 16px quadrant tiles, exhaustively.

The outputs are 32px cells, but the frame only has 16px quadrants, so a cell is a 2x2 arrangement of
transformed quadrants. Resolving at the cell level (rather than at an arbitrary 16px grid) keeps tiles
aligned to cell boundaries — which is what the earlier, misaligned pass got wrong.

    python script/multiblock/resolve_cells_as_tiles.py
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


def diff_at(a, b, w, h):
    return sum(1 for y in range(h) for x in range(w)
               if a.getpixel((x, y)) != b.getpixel((x, y)))


def main():
    frame = load("template_frame.png")
    unit = frame.size[0] // 2

    quadrants = {}
    for qy in range(2):
        for qx in range(2):
            quadrants[(qx, qy)] = frame.crop((qx * unit, qy * unit,
                                              (qx + 1) * unit, (qy + 1) * unit))

    candidates = {}
    for key, source in quadrants.items():
        for label, transform in TRANSFORMS.items():
            candidates[(key, label)] = transform(source)

    image = load("template_ct.png")
    cell_w, cell_h = image.size[0] // 4, image.size[1] // 4
    units_x, units_y = cell_w // unit, cell_h // unit
    print(f"  template_ct.png: {image.size[0]}x{image.size[1]}, "
          f"cell {cell_w}x{cell_h} = {units_x}x{units_y} units of {unit}px")

    unresolved = 0
    for cy in range(4):
        for cx in range(4):
            print(f"  cell ({cx},{cy}):")
            for uy in range(units_y):
                row = []
                for ux in range(units_x):
                    tile = image.crop((cx * cell_w + ux * unit, cy * cell_h + uy * unit,
                                       cx * cell_w + (ux + 1) * unit,
                                       cy * cell_h + (uy + 1) * unit))
                    hit = None
                    best = None
                    for (key, label), candidate in candidates.items():
                        d = diff_at(candidate, tile, unit, unit)
                        if d == 0:
                            hit = (key, label)
                            break
                        if best is None or d < best[0]:
                            best = (d, key, label)
                    if hit:
                        row.append(f"q{hit[0][0]}{hit[0][1]}/{hit[1]}")
                    else:
                        row.append(f"?{best[0]}")
                        unresolved += 1
                print("    " + " ".join(f"{v:<16}" for v in row))
    print()
    print(f"  unresolved units: {unresolved}/{4 * 4 * units_x * units_y}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
