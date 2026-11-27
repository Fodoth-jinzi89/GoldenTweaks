"""Resolve every 16px tile of an output cell to a transformed frame quadrant, by exhaustive match.

Earlier attempts compared whole cells or guessed at a couple of transforms. Since the frame is 16px
quadrants and the outputs are multiples of 16, every tile should equal one quadrant under one of the
eight symmetries. This checks all eight for each slot, so a tile is either explained exactly or
reported as genuinely not a transform — which is the fact that decides whether generation is possible.

    python script/multiblock/resolve_tiles_full.py
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

    for name in ("template_ct.png", "template_r.png"):
        path = os.path.join(FOLDER, name)
        if not os.path.exists(path):
            continue
        image = load(name)
        out_w, out_h = image.size
        tiles_x, tiles_y = out_w // unit, out_h // unit
        print(f"  === {name}: {out_w}x{out_h} = {tiles_x}x{tiles_y} tiles of {unit}px ===")

        grid = []
        unresolved = 0
        for ty in range(tiles_y):
            row = []
            for tx in range(tiles_x):
                tile = image.crop((tx * unit, ty * unit,
                                   (tx + 1) * unit, (ty + 1) * unit))
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
                    row.append(f"?({best[0]})")
                    unresolved += 1
            grid.append(row)

        max_label = max(len(v) for row in grid for v in row)
        for row in grid:
            print("    " + " ".join(f"{v:<{max_label}}" for v in row))
        print(f"    unresolved: {unresolved}/{tiles_x * tiles_y}")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main())
