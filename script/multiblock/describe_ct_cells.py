"""Describe the border structure of each cell in a 4x4 connected-texture sheet.

Needed before any inner-corner art can be generated: to composite a corner it helps to know where the
existing art puts its outline. Prints a coarse map per cell, with `#` for the darker outline colour and
`.` for the lighter interior, so the tiles are readable in a terminal.

    python script/multiblock/describe_ct_cells.py arts/multiblock/shell_pe_ct.png
"""
import sys

from PIL import Image


def luminance(pixel):
    r, g, b = pixel[0], pixel[1], pixel[2]
    return (r * 299 + g * 587 + b * 114) // 1000


def main(argv):
    if not argv:
        print("usage: describe_ct_cells.py <sheet.png>")
        return 1
    path = argv[0]
    image = Image.open(path).convert("RGBA")
    width, height = image.size
    cell = width // 4

    # The outline is the darker of the two dominant tones, so split the sheet on the midpoint between
    # its darkest and lightest opaque pixels rather than hard-coding a colour.
    tones = [luminance(p) for p in image.getdata() if p[3] > 0]
    if not tones:
        print("  sheet is fully transparent")
        return 1
    threshold = (min(tones) + max(tones)) // 2

    for row in range(4):
        for column in range(4):
            index = column + row * 4
            box = (column * cell, row * cell, (column + 1) * cell, (row + 1) * cell)
            tile = image.crop(box)

            step = cell // 8
            print(f"  cell {index:2d} (row {row}, col {column})")
            for y in range(0, cell, step):
                line = []
                for x in range(0, cell, step):
                    pixel = tile.getpixel((min(x, cell - 1), min(y, cell - 1)))
                    if pixel[3] < 32:
                        line.append(" ")
                    elif luminance(pixel) < threshold:
                        line.append("#")
                    else:
                        line.append(".")
                print("    " + "".join(line))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
