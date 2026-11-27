"""Report which corner quadrants carry ink, per cell, for several sheets at once.

Sixteen pixels of difference per corner is a fixed-size mark, so the question is binary per corner:
present or absent. Printing that for both sheets side by side shows the rule the hand-checked art
follows, which is what the generator has to reproduce.

    python script/multiblock/corner_ink_table.py <sheet.png> [sheet.png ...]
"""
import itertools
import sys

from PIL import Image

SIDES = ("up", "down", "left", "right")
CORNERS = ("topLeft", "topRight", "bottomLeft", "bottomRight")

from convert_ct_rectangle_to_omni import omni_index


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def corner_ink(image, index, cell, threshold, quarter):
    column, row = index % 8, index // 8
    x0, y0 = column * cell, row * cell
    boxes = {
        "topLeft": (x0, y0),
        "topRight": (x0 + cell - quarter, y0),
        "bottomLeft": (x0, y0 + cell - quarter),
        "bottomRight": (x0 + cell - quarter, y0 + cell - quarter),
    }
    out = {}
    for name, (lx, ly) in boxes.items():
        out[name] = sum(1 for y in range(ly, ly + quarter) for x in range(lx, lx + quarter)
                        if image.getpixel((x, y))[3] > 0
                        and luminance(image.getpixel((x, y))) < threshold)
    return out


def main(argv):
    if not argv:
        print("usage: corner_ink_table.py <sheet.png> [sheet.png ...]")
        return 1

    states = {}
    for bits in itertools.product((False, True), repeat=8):
        ctx = dict(zip(SIDES + CORNERS, bits))
        states.setdefault(omni_index(ctx), ctx)

    sheets = []
    for path in argv:
        image = Image.open(path).convert("RGBA")
        cell = image.size[0] // 8
        tones = [luminance(image.getpixel((x, y)))
                 for y in range(image.size[1]) for x in range(image.size[0])
                 if image.getpixel((x, y))[3] > 0]
        threshold = (min(tones) + max(tones)) // 2
        sheets.append((path, image, cell, threshold, cell // 4))

    header = "  idx  edges diag   " + "  ".join(
        f"{path.split('/')[-1][:22]:>22s}" for path, *_ in sheets)
    print(header)
    for index in sorted(states):
        ctx = states[index]
        edges = "".join("Y" if ctx[s] else "n" for s in SIDES)
        diag = "".join("Y" if ctx[c] else "n" for c in CORNERS)
        cells = []
        for path, image, cell, threshold, quarter in sheets:
            ink = corner_ink(image, index, cell, threshold, quarter)
            # A mark is a solid block, so the count is either near zero or the full quadrant.
            marks = "".join("M" if ink[c] > quarter * quarter // 4 else "-" for c in CORNERS)
            cells.append(f"{marks:>22s}")
        print(f"  {index:3d}  {edges:5s} {diag:4s}   " + "  ".join(cells))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
