"""Describe the tiles that match no transform, so their actual content is visible.

Several 16px tiles match none of the 32 candidate transforms. Rather than assume they are arbitrary,
this prints each distinct unmatched tile as a colour-region map. If they turn out to be the same few
shapes, they are still reproducible — just from a different source region than assumed.

    python script/multiblock/describe_unmatched_tiles.py
"""
import os
import sys
from collections import Counter

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def signature(tile):
    """A compact description: colour group map at 4-level quantisation."""
    w, h = tile.size
    rows = []
    for y in range(h):
        row = []
        for x in range(w):
            r, g, b, a = tile.getpixel((x, y))
            row.append((r // 64, g // 64, b // 64))
        rows.append(tuple(row))
    return tuple(rows)


def main():
    frame = load("template_frame.png")
    unit = frame.size[0] // 2

    quadrants = {}
    for qy in range(2):
        for qx in range(2):
            quadrants[(qx, qy)] = frame.crop((qx * unit, qy * unit,
                                              (qx + 1) * unit, (qy + 1) * unit))

    TR = {
        "id": lambda im: im,
        "flipx": lambda im: im.transpose(Image.Transpose.FLIP_LEFT_RIGHT),
        "flipy": lambda im: im.transpose(Image.Transpose.FLIP_TOP_BOTTOM),
        "rot180": lambda im: im.transpose(Image.Transpose.ROTATE_180),
        "rot90": lambda im: im.transpose(Image.Transpose.ROTATE_90),
        "rot270": lambda im: im.transpose(Image.Transpose.ROTATE_270),
        "transpose": lambda im: im.transpose(Image.Transpose.TRANSPOSE),
        "transverse": lambda im: im.transpose(Image.Transpose.TRANSVERSE),
    }

    known = set()
    for source in quadrants.values():
        for transform in TR.values():
            known.add(signature(transform(source)))

    image = load("template_ct.png")
    cell = image.size[0] // 4
    groups = Counter()
    examples = {}
    for cy in range(4):
        for cx in range(4):
            for uy in range(2):
                for ux in range(2):
                    tile = image.crop((cx * cell + ux * unit, cy * cell + uy * unit,
                                       cx * cell + (ux + 1) * unit,
                                       cy * cell + (uy + 1) * unit))
                    sig = signature(tile)
                    if sig in known:
                        continue
                    groups[sig] += 1
                    examples.setdefault(sig, (cx, cy, ux, uy, tile))

    print(f"  distinct unmatched tile shapes: {len(groups)}, total occurrences: {sum(groups.values())}")
    print()
    for sig, count in groups.most_common():
        cx, cy, ux, uy, tile = examples[sig]
        print(f"  --- shape seen {count}x, first at cell ({cx},{cy}) unit ({ux},{uy}) ---")
        w, h = tile.size
        letters = {}
        for y in range(h):
            row = []
            for x in range(w):
                r, g, b, a = tile.getpixel((x, y))
                key = (r // 64, g // 64, b // 64)
                if key not in letters:
                    letters[key] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"[len(letters)]
                row.append(letters[key])
            print("    " + "".join(row))
        print(f"    colours: {letters}")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main())
