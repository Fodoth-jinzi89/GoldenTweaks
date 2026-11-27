"""Compare nine-slice pieces by shape rather than by colour.

The frame template encodes position through colour on purpose, so two structurally identical pieces
still differ in RGB. Comparing raw pixels therefore rejects every symmetry that actually holds. This
reduces each pixel to "is it in the border band or the interior band" — a two-tone shape mask — and
then tests symmetry on that, which is what the geometry claims.

    python script/multiblock/nine_slice_shape.py [output.png]
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
}


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def shape_mask(image):
    """Reduce an image to a boolean grid: dark-ish vs light-ish, i.e. border vs interior."""
    w, h = image.size
    values = []
    for y in range(h):
        for x in range(w):
            r, g, b, a = image.getpixel((x, y))
            values.append((r * 299 + g * 587 + b * 114) // 1000)

    # The split point is the midpoint of the range present in this piece, which separates a two-tone
    # frame from its interior regardless of the actual colours used.
    low, high = min(values), max(values)
    if low == high:
        return tuple([False] * len(values))
    mid = (low + high) // 2
    return tuple(v <= mid for v in values)


def main(argv):
    frame = load("template_frame.png")
    pieces = {}
    index = 0
    for yi in range(3):
        for xi in range(3):
            pieces[NAMES[index]] = frame.crop((EDGES[xi], EDGES[yi],
                                               EDGES[xi + 1], EDGES[yi + 1]))
            index += 1

    masks = {name: shape_mask(piece) for name, piece in pieces.items()}

    print("  frame internal symmetry on shape masks:")
    pairs = [("tl", "tr", "flipx"), ("tl", "bl", "flipy"), ("tl", "br", "rot180"),
             ("t", "b", "flipy"), ("l", "r", "flipx"), ("tr", "bl", "rot180"),
             ("tr", "br", "flipy"), ("bl", "br", "flipx")]
    for a, b, transform in pairs:
        candidate = shape_mask(TRANSFORMS[transform](pieces[a]))
        if len(candidate) != len(masks[b]):
            print(f"    {b:3s} == {transform:9s}({a:3s}): size differs")
            continue
        mismatch = sum(1 for p, q in zip(candidate, masks[b]) if p != q)
        print(f"    {b:3s} == {transform:9s}({a:3s}): "
              + ("yes" if mismatch == 0 else f"no ({mismatch} px)"))

    if not argv:
        return 0

    image = load(argv[0])
    out_w, out_h = image.size
    cell = out_w // 4
    scale = cell / 32
    cuts = [int(round(v * scale)) for v in EDGES]

    print()
    print(f"  === {argv[0]}: cell {cell}px, cuts {cuts} ===")
    unresolved = 0
    for cy in range(4):
        for cx in range(4):
            print(f"  cell ({cx},{cy}):")
            for yi in range(3):
                row = []
                for xi in range(3):
                    tile = image.crop((cx * cell + cuts[xi], cy * cell + cuts[yi],
                                       cx * cell + cuts[xi + 1], cy * cell + cuts[yi + 1]))
                    target = shape_mask(tile)
                    hit = None
                    for name, piece in pieces.items():
                        for label, transform in TRANSFORMS.items():
                            candidate = shape_mask(transform(piece))
                            if candidate == target:
                                hit = (name, label)
                                break
                        if hit:
                            break
                    if hit:
                        row.append(f"{hit[0]}/{hit[1]}")
                    else:
                        row.append("?")
                        unresolved += 1
                print("    " + " ".join(f"{v:<12}" for v in row))
    print()
    print(f"  unresolved by shape: {unresolved}/144")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
