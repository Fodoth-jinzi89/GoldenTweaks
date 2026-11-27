"""Split the frame on a 12/8/12 grid and test the pieces for transform relationships.

A frame drawn for connected textures is a 3x3 nine-slice: four corners, four edges, and a middle. If
the sheet is built by rearranging those nine pieces, then each output region should equal one piece
under a flip or rotation, and the pieces themselves should be symmetric about the axes a frame implies.

This reports both: the nine pieces of the frame, and whether an output's pieces resolve to them.

    python script/multiblock/nine_slice_test.py [output.png]
"""
import os
import sys

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")

# The nine-slice split: 12px border, 8px middle, 12px border.
EDGES = (0, 12, 20, 32)

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

NAMES = ("tl", "t", "tr", "l", "c", "r", "bl", "b", "br")


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def slice_nine(image):
    """The nine pieces, keyed by name, using the 12/8/12 split."""
    pieces = {}
    index = 0
    for yi in range(3):
        for xi in range(3):
            pieces[NAMES[index]] = image.crop((EDGES[xi], EDGES[yi], EDGES[xi + 1], EDGES[yi + 1]))
            index += 1
    return pieces


def diff_at(a, b):
    if a.size != b.size:
        return None
    w, h = a.size
    return sum(1 for y in range(h) for x in range(w)
               if a.getpixel((x, y)) != b.getpixel((x, y)))


def main(argv):
    frame = load("template_frame.png")
    pieces = slice_nine(frame)

    print(f"  frame {frame.size[0]}x{frame.size[1]}, split at {EDGES}:")
    for name in NAMES:
        print(f"    {name:3s} {pieces[name].size[0]}x{pieces[name].size[1]}")

    # Do the frame's own pieces relate to each other by symmetry? A frame should be symmetric.
    print()
    print("  frame internal symmetry (piece == transform of another):")
    pairs = [("tl", "tr", "flipx"), ("tl", "bl", "flipy"), ("tl", "br", "rot180"),
             ("t", "b", "flipy"), ("l", "r", "flipx"), ("tr", "bl", "rot180")]
    for a, b, transform in pairs:
        result = diff_at(TRANSFORMS[transform](pieces[a]), pieces[b])
        state = "yes" if result == 0 else (f"no ({result} px)" if result is not None else "size differs")
        print(f"    {b:3s} == {transform:9s}({a:3s}): {state}")

    if not argv:
        return 0

    image = load(argv[0])
    out_w, out_h = image.size
    cell = out_w // 4
    print()
    print(f"  === {argv[0]}: {out_w}x{out_h}, cell {cell}px ===")

    # Every output cell, split on the same proportions scaled to the cell.
    scale = cell / 32
    cuts = [int(round(v * scale)) for v in EDGES]

    unresolved = 0
    for cy in range(4):
        for cx in range(4):
            print(f"  cell ({cx},{cy}):")
            for yi in range(3):
                row = []
                for xi in range(3):
                    tile = image.crop((cx * cell + cuts[xi], cy * cell + cuts[yi],
                                       cx * cell + cuts[xi + 1], cy * cell + cuts[yi + 1]))
                    hit = None
                    best = None
                    for name, piece in pieces.items():
                        for label, transform in TRANSFORMS.items():
                            candidate = transform(piece)
                            d = diff_at(candidate, tile)
                            if d is None:
                                continue
                            if d == 0:
                                hit = (name, label)
                                break
                            if best is None or d < best[0]:
                                best = (d, name, label)
                        if hit:
                            break
                    if hit:
                        row.append(f"{hit[0]}/{hit[1]}")
                    else:
                        row.append(f"?{best[0]}")
                        unresolved += 1
                print("    " + " ".join(f"{v:<14}" for v in row))
    print()
    print(f"  unresolved pieces: {unresolved}/144")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
