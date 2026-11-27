"""Resolve each 16px sub-tile of the outputs to a transformation of a frame quadrant.

This is the per-tile version of the mapping: the output is a grid of 16px tiles, each of which should
equal one of the frame's four quadrants under some flip or rotation. Independence per tile means no
combinatorial search — each tile is matched on its own — and it directly yields the instruction list a
generator would follow.

    python script/multiblock/map_subtiles.py
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


def pixels(image):
    return list(image.getdata())


def main():
    frame = load("template_frame.png")
    fw, fh = frame.size
    quad = fw // 2

    variants = {}
    for qy in range(2):
        for qx in range(2):
            source = frame.crop((qx * quad, qy * quad, (qx + 1) * quad, (qy + 1) * quad))
            for label, transform in TRANSFORMS.items():
                variants[(qx, qy, label)] = pixels(transform(source))

    print(f"  frame {fw}x{fh}, quadrant {quad}px, {len(variants)} candidate tiles")

    for name in ("template_ct.png", "template_r.png"):
        path = os.path.join(FOLDER, name)
        if not os.path.exists(path):
            continue
        image = load(name)
        out_w, out_h = image.size
        tiles_x, tiles_y = out_w // quad, out_h // quad
        print()
        print(f"  === {name}: {out_w}x{out_h} = {tiles_x}x{tiles_y} tiles of {quad}px ===")

        unresolved = 0
        used = Counter()
        for ty in range(tiles_y):
            row = []
            for tx in range(tiles_x):
                tile = image.crop((tx * quad, ty * quad,
                                   (tx + 1) * quad, (ty + 1) * quad))
                target = pixels(tile)
                hit = None
                for key, cand in variants.items():
                    if cand == target:
                        hit = key
                        break
                if hit:
                    row.append(f"q{hit[0]}{hit[1]}:{hit[2]}")
                    used[(hit[0], hit[1], hit[2])] += 1
                else:
                    row.append("?          ")
                    unresolved += 1
            print("    " + " ".join(f"{v:<14}" for v in row))

        print(f"    unresolved tiles: {unresolved}/{tiles_x * tiles_y}")
        if used:
            print("    tile usage:")
            for key, count in sorted(used.items()):
                print(f"      quadrant ({key[0]},{key[1]}) {key[2]:10s}: {count}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
