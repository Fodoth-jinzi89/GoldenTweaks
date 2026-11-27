"""Resolve every output cell from quadrants of the frame, not from the whole frame.

The whole-frame test showed only two cells match, so the outputs are built from smaller pieces. Since
the frame is a 2x2 grid of 16px quadrants and the smallest output cell is 32px, a cell may be a
2x2 arrangement of quadrants — each possibly transformed independently. This tries every combination
of four quadrant placements and reports the first that reproduces a cell exactly.

    python script/multiblock/map_quadrant_tiles.py
"""
import itertools
import os
import sys

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")

TRANSFORMS = {
    "id": lambda im: im,
    "flipx": lambda im: im.transpose(Image.Transpose.FLIP_LEFT_RIGHT),
    "flipy": lambda im: im.transpose(Image.Transpose.FLIP_TOP_BOTTOM),
    "rot180": lambda im: im.transpose(Image.Transpose.ROTATE_180),
}


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def pixels(image):
    return list(image.getdata())


def main():
    frame = load("template_frame.png")
    fw, fh = frame.size
    quad = fw // 2

    quadrants = {}
    for qy in range(2):
        for qx in range(2):
            quadrants[(qx, qy)] = frame.crop((qx * quad, qy * quad,
                                              (qx + 1) * quad, (qy + 1) * quad))

    image = load("template_ct.png")
    out_w, out_h = image.size
    grid = 4
    cell_w = out_w // grid
    cell_h = out_h // grid
    print(f"  template_ct.png {out_w}x{out_h}: {grid}x{grid} cells of {cell_w}x{cell_h}")
    print(f"  frame quadrant {quad}x{quad}, so a cell is {cell_w // quad}x{cell_h // quad} quadrants")
    print()

    for cy in range(grid):
        for cx in range(grid):
            cell = image.crop((cx * cell_w, cy * cell_h,
                               (cx + 1) * cell_w, (cy + 1) * cell_h))
            target = pixels(cell)
            cols, rows = cell_w // quad, cell_h // quad

            # Search every placement of a transformed quadrant in every slot.
            best = None
            options = [(key, label, pixels(transform(source)))
                       for key, source in quadrants.items()
                       for label, transform in TRANSFORMS.items()]
            for combo in itertools.product(range(len(options)), repeat=cols * rows):
                assembled = [None] * len(target)
                ok = True
                for slot, choice in enumerate(combo):
                    key, label, data = options[choice]
                    sx, sy = slot % cols, slot // cols
                    for y in range(quad):
                        for x in range(quad):
                            dx = sx * quad + x
                            dy = sy * quad + y
                            assembled[dy * cell_w + dx] = data[y * quad + x]
                diff = sum(1 for a, b in zip(assembled, target) if a != b)
                if best is None or diff < best[0]:
                    best = (diff, combo)
                if diff == 0:
                    break

            diff, combo = best
            if diff == 0:
                described = []
                for slot, choice in enumerate(combo):
                    key, label, _ = options[choice]
                    described.append(f"({slot % cols},{slot // cols})=q{key[0]}{key[1]}:{label}")
                print(f"  cell ({cx},{cy}) exact: " + "  ".join(described))
            else:
                print(f"  cell ({cx},{cy}) closest match still differs by {diff} px")
    return 0


if __name__ == "__main__":
    sys.exit(main())
