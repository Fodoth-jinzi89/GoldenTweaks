"""Dump the same cells from two connected sheets side by side, with their neighbour state.

Both sheets are printed at a common 16-wide raster so the shapes line up even though the art is drawn
at different resolutions. The neighbour state of each index is printed with it, which is the only way
to tell whether a corner mark belongs in that cell.

    python script/multiblock/compare_cells.py <reference.png> <mine.png> [index ...]
"""
import itertools
import sys

from PIL import Image

SIDES = ("up", "down", "left", "right")
CORNERS = ("topLeft", "topRight", "bottomLeft", "bottomRight")

from convert_ct_rectangle_to_omni import omni_index

DUMP_WIDTH = 16


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def state_table():
    table = {}
    for bits in itertools.product((False, True), repeat=8):
        ctx = dict(zip(SIDES + CORNERS, bits))
        table.setdefault(omni_index(ctx), ctx)
    return table


def cell_raster(sheet, index, grid, threshold):
    """One cell as DUMP_WIDTH lines of `#`/`.`/space, sampled across the cell."""
    width, height = sheet.size
    cell = width // grid
    column, row = index % grid, index // grid
    tile = sheet.crop((column * cell, row * cell, (column + 1) * cell, (row + 1) * cell))
    pixels = tile.load()

    lines = []
    for out_y in range(DUMP_WIDTH):
        y = min(cell - 1, out_y * cell // DUMP_WIDTH)
        chars = []
        for out_x in range(DUMP_WIDTH):
            x = min(cell - 1, out_x * cell // DUMP_WIDTH)
            r, g, b, a = pixels[x, y]
            if a < 32:
                chars.append(" ")
            elif luminance((r, g, b)) < threshold:
                chars.append("#")
            else:
                chars.append(".")
        lines.append("".join(chars))
    return lines


def main(argv):
    if len(argv) < 2:
        print("usage: compare_cells.py <reference.png> <mine.png> [index ...]")
        return 1

    ref = Image.open(argv[0]).convert("RGBA")
    mine = Image.open(argv[1]).convert("RGBA")

    def threshold_of(image):
        tones = [luminance(image.getpixel((x, y)))
                 for y in range(image.size[1]) for x in range(image.size[0])
                 if image.getpixel((x, y))[3] > 0]
        return (min(tones) + max(tones)) // 2

    ref_t, mine_t = threshold_of(ref), threshold_of(mine)
    states = state_table()

    wanted = [int(a) for a in argv[2:]] or sorted(states)
    for index in wanted:
        ctx = states.get(index)
        if ctx is None:
            print(f"  cell {index}: not addressable")
            continue
        flags = " ".join(f"{s[:2]}={'Y' if ctx[s] else 'n'}" for s in SIDES)
        corners = " ".join(f"{c[:2]}={'Y' if ctx[c] else 'n'}" for c in CORNERS)
        print(f"  cell {index:2d}  {flags}  |  {corners}")

        left = cell_raster(ref, index, 8, ref_t)
        right = cell_raster(mine, index, 8, mine_t)
        print("      reference        mine")
        for a, b in zip(left, right):
            print(f"    {a}    {b}")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
