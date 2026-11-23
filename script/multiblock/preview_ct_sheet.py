"""Preview a RECTANGLE sheet with the sides Create will read from each tile.

Annotates the sheet at 2x so the index and the sides it is selected for can be checked by eye.

    python script/multiblock/preview_ct_sheet.py arts/multiblock/shell_pp_ct.png
"""
import sys

from PIL import Image, ImageDraw

SHEET_SIZE = 4
SCALE = 2


def sides(index):
    """The connected sides Create reads for an index."""
    h = index % SHEET_SIZE
    v = index // SHEET_SIZE
    up = v in (1, 2)
    down = v in (0, 1)
    left = h in (2, 3)
    right = h in (1, 2)
    return "".join(name for on, name in
                   ((up, "U"), (down, "D"), (left, "L"), (right, "R")) if on) or "-"


def main(argv):
    path = argv[0] if argv else "arts/multiblock/shell_pp_ct.png"
    sheet = Image.open(path).convert("RGBA")
    tile = sheet.width // SHEET_SIZE
    big = sheet.resize((sheet.width * SCALE, sheet.height * SCALE), Image.NEAREST)
    draw = ImageDraw.Draw(big)

    for index in range(SHEET_SIZE * SHEET_SIZE):
        x = index % SHEET_SIZE * tile * SCALE
        y = index // SHEET_SIZE * tile * SCALE
        draw.rectangle([x, y, x + tile * SCALE - 1, y + tile * SCALE - 1], outline=(255, 0, 0))
        draw.text((x + 3, y + 2), f"{index}", fill=(200, 0, 0))
        draw.text((x + 3, y + tile * SCALE - 12), sides(index), fill=(0, 90, 200))

    out = "data_sheets/_ct_preview.png"
    big.save(out)
    print(f"  {out}  {big.width}x{big.height}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
