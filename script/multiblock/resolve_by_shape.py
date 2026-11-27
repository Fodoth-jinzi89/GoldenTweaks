"""Resolve output pieces to frame pieces by shape, using a shared colour-independent mask.

The frame encodes position with colour, so the same geometry appears in different hues in different
slots. Comparing raw colour therefore rejects matches that hold structurally. Both sides are reduced to
a small mask — each pixel classified against the piece's own tone bands — so two pieces compare equal
when they are drawn the same way regardless of palette.

    python script/multiblock/resolve_by_shape.py
"""
import os
import sys

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")
EDGES = (0, 12, 20, 32)
NAMES = ("tl", "t", "tr", "l", "c", "r", "bl", "b", "br")

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


def band_mask(image, bands=3):
    """Classify each pixel into `bands` tone groups, ranked against the piece's own luminance range.

    Ranking rather than absolute thresholds is what makes the mask comparable across slots drawn in
    different colours: the darkest region of one piece maps to the darkest region of another.
    """
    w, h = image.size
    values = []
    for y in range(h):
        for x in range(w):
            r, g, b, a = image.getpixel((x, y))
            values.append((r * 299 + g * 587 + b * 114) // 1000)

    unique = sorted(set(values))
    if len(unique) == 1:
        return tuple([0] * len(values))

    # Equal-count buckets over the sorted distinct tones.
    ranks = {}
    for index, value in enumerate(unique):
        ranks[value] = min(bands - 1, index * bands // len(unique))
    return tuple(ranks[v] for v in values)


def main():
    frame = load("template_frame.png")
    pieces = {}
    index = 0
    for yi in range(3):
        for xi in range(3):
            pieces[NAMES[index]] = frame.crop((EDGES[xi], EDGES[yi],
                                               EDGES[xi + 1], EDGES[yi + 1]))
            index += 1

    for bands in (2, 3, 4):
        masks = {name: band_mask(piece, bands) for name, piece in pieces.items()}
        candidates = {}
        for name, piece in pieces.items():
            for label, transform in TRANSFORMS.items():
                candidates[(name, label)] = band_mask(transform(piece), bands)

        image = load("template_ct.png")
        cell = image.size[0] // 4
        unresolved = 0
        for cy in range(4):
            for cx in range(4):
                for yi in range(3):
                    for xi in range(3):
                        tile = image.crop((cx * cell + EDGES[xi], cy * cell + EDGES[yi],
                                           cx * cell + EDGES[xi + 1],
                                           cy * cell + EDGES[yi + 1]))
                        target = band_mask(tile, bands)
                        if not any(c == target for c in candidates.values()):
                            unresolved += 1
        print(f"  bands={bands}: unresolved pieces {unresolved}/144")
    return 0


if __name__ == "__main__":
    sys.exit(main())
