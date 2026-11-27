"""Identify what replaces each unmatched nine-slice piece, by comparing against the frame's pieces.

An output cell is a nine-slice reassembly, and most pieces match the frame exactly. The ones that do not
are the variable parts — the edges and corners that must appear open or closed depending on what the
cell connects to. This lists, for every unmatched piece, which frame piece it is closest to and by how
much, so the substitution rule becomes visible instead of inferred.

    python script/multiblock/identify_piece_variants.py
"""
import os
import sys
from collections import Counter

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")
EDGES = (0, 12, 20, 32)
NAMES = ("tl", "t", "tr", "l", "c", "r", "bl", "b", "br")


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def main():
    frame = load("template_frame.png")
    pieces = {}
    index = 0
    for yi in range(3):
        for xi in range(3):
            pieces[NAMES[index]] = frame.crop((EDGES[xi], EDGES[yi],
                                               EDGES[xi + 1], EDGES[yi + 1]))
            index += 1

    image = load("template_ct.png")
    cell = image.size[0] // 4

    # Group unmatched pieces by their pixel content so identical substitutions collapse together.
    variants = Counter()
    examples = {}

    for cy in range(4):
        for cx in range(4):
            for yi in range(3):
                for xi in range(3):
                    name = NAMES[yi * 3 + xi]
                    tile = image.crop((cx * cell + EDGES[xi], cy * cell + EDGES[yi],
                                       cx * cell + EDGES[xi + 1], cy * cell + EDGES[yi + 1]))
                    source = pieces[name]
                    same = (tile.size == source.size and
                            all(tile.getpixel((x, y)) == source.getpixel((x, y))
                                for y in range(tile.size[1]) for x in range(tile.size[0])))
                    if same:
                        continue

                    key = (name, tuple(tile.getpixel((x, y))
                                       for y in range(tile.size[1])
                                       for x in range(tile.size[0])))
                    variants[key] += 1
                    examples.setdefault(key, (cx, cy, tile))

    print(f"  distinct substituted pieces: {len(variants)}")
    print()
    for (name, _), count in variants.most_common():
        cx, cy, tile = examples[(name, _)]
        w, h = tile.size
        print(f"  --- slot {name} replaced, seen {count}x, first at cell ({cx},{cy}) ---")

        # Which other frame piece is identical to this tile?
        identical = [other for other, source in pieces.items()
                     if source.size == tile.size and
                     all(source.getpixel((x, y)) == tile.getpixel((x, y))
                         for y in range(h) for x in range(w))]
        if identical:
            print(f"    identical to frame piece(s): {identical}")
        else:
            # Distance to the original slot piece and to everything else.
            scored = []
            for other, source in pieces.items():
                if source.size != tile.size:
                    continue
                d = sum(1 for y in range(h) for x in range(w)
                        if source.getpixel((x, y)) != tile.getpixel((x, y)))
                scored.append((d, other))
            scored.sort()
            print(f"    closest frame pieces: {scored[:4]}")

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
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main())
