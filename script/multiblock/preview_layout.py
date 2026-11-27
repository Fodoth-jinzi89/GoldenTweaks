"""Render a small block layout through the connected-texture indices and show the result.

Reasoning about indices on paper is how a corner defect gets missed: the index can be right while the
cell it selects still carries a mark. This lays out a grid of connected blocks, works out each one's
context from its neighbours, looks up the cell, and pastes those cells together — the same thing the
game does, so what it prints is what the block would draw.

    python script/multiblock/preview_layout.py <sheet.png> <w> <h> [out.png]

With no out path it prints an ASCII view of the assembled surface instead.
"""
import sys

from PIL import Image

SIDES = ("up", "down", "left", "right")
CORNERS = ("topLeft", "topRight", "bottomLeft", "bottomRight")

from convert_ct_rectangle_to_omni import omni_index


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def context_of(cells, x, y, width, height):
    """The neighbour state of one cell in a filled grid of `width` x `height`."""
    def filled(xx, yy):
        return 0 <= xx < width and 0 <= yy < height

    up = filled(x, y - 1)
    down = filled(x, y + 1)
    left = filled(x - 1, y)
    right = filled(x + 1, y)
    top_left = filled(x - 1, y - 1)
    top_right = filled(x + 1, y - 1)
    bottom_left = filled(x - 1, y + 1)
    bottom_right = filled(x + 1, y + 1)

    return {
        "up": up, "down": down, "left": left, "right": right,
        "topLeft": top_left, "topRight": top_right,
        "bottomLeft": bottom_left, "bottomRight": bottom_right,
    }


def main(argv):
    if len(argv) < 3:
        print("usage: preview_layout.py <sheet.png> <w> <h> [out.png]")
        return 1
    path, width, height = argv[0], int(argv[1]), int(argv[2])
    out_path = argv[3] if len(argv) > 3 else None

    sheet = Image.open(path).convert("RGBA")
    cell = sheet.size[0] // 8

    tones = [luminance(sheet.getpixel((x, y)))
             for y in range(sheet.size[1]) for x in range(sheet.size[0])
             if sheet.getpixel((x, y))[3] > 0]
    threshold = (min(tones) + max(tones)) // 2

    out = Image.new("RGBA", (width * cell, height * cell), (0, 0, 0, 0))
    indices = []
    for y in range(height):
        row = []
        for x in range(width):
            index = omni_index(context_of(None, x, y, width, height))
            row.append(index)
            tile = sheet.crop(((index % 8) * cell, (index // 8) * cell,
                               (index % 8 + 1) * cell, (index // 8 + 1) * cell))
            out.paste(tile, (x * cell, y * cell))
        indices.append(row)

    print(f"  {width}x{height} layout, cells from {path}")
    print("  indices used:")
    for row in indices:
        print("    " + " ".join(f"{i:3d}" for i in row))

    if out_path:
        out.save(out_path)
        print(f"  written {out_path} ({out.size[0]}x{out.size[1]})")
        return 0

    # ASCII view: mark where a cell has a mark anchored in any of its corners.
    print()
    print("  corner marks present in the assembled surface (X = mark, . = clear):")
    box = cell // 4
    for y in range(height):
        lines = [""] * 8
        for x in range(width):
            index = indices[y][x]
            x0, y0 = (index % 8) * cell, (index // 8) * cell
            for out_y in range(8):
                sy = y0 + min(cell - 1, out_y * cell // 8)
                chars = []
                for out_x in range(8):
                    sx = x0 + min(cell - 1, out_x * cell // 8)
                    r, g, b, a = sheet.getpixel((sx, sy))
                    if a < 32:
                        chars.append(" ")
                    elif luminance((r, g, b)) < threshold:
                        chars.append("#")
                    else:
                        chars.append(".")
                lines[out_y] += "".join(chars)
        for line in lines:
            print("    " + line)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
