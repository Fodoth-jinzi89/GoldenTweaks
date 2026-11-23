"""Reorder a Create RECTANGLE connected-texture sheet to the layout Create actually indexes.

The layout is not the bit-per-side order it looks like. `AllCTTypes.RECTANGLE.getTextureIndex` groups
sides into two small codes:

    column h:  left && right -> 2,  left -> 3,  right -> 1,  neither -> 0
    row    v:  up   && down  -> 1,  up   -> 2,  down  -> 0,  neither -> 3
    index = h + v * 4

so the sheet is not "one tile per combination in bit order" but two tables crossed. A tile is placed by
what it means, not by where it sat before, which is what this script matches on: every cell is keyed by
its connected sides and the sheet is rebuilt from the target layout's own key order.

    python script/multiblock/reorder_ct_sheet.py arts/multiblock/shell_pp_ct.png
"""
import os
import shutil
import sys

from PIL import Image

SHEET_SIZE = 4


def target_keys():
    """The connected sides of every index, in the order Create indexes them."""
    keys = [None] * (SHEET_SIZE * SHEET_SIZE)
    for v in range(SHEET_SIZE):
        for h in range(SHEET_SIZE):
            up = v in (1, 2)
            down = v in (0, 1)
            left = h in (2, 3)
            right = h in (1, 2)
            keys[h + v * SHEET_SIZE] = (up, down, left, right)
    return keys


def source_keys():
    """The connected sides the sheet was drawn for, in the old bit-per-side order."""
    keys = [None] * (SHEET_SIZE * SHEET_SIZE)
    for mask in range(SHEET_SIZE * SHEET_SIZE):
        # The previous formula: up=1, down=2, left=4, right=8.
        up = bool(mask & 1)
        down = bool(mask & 2)
        left = bool(mask & 4)
        right = bool(mask & 8)
        keys[mask] = (up, down, left, right)
    return keys


def main(argv):
    if not argv:
        print("usage: reorder_ct_sheet.py <sheet.png>", file=sys.stderr)
        return 1
    path = argv[0]
    if not os.path.exists(path):
        print(f"missing {path}", file=sys.stderr)
        return 1

    sheet = Image.open(path).convert("RGBA")
    tile = sheet.width // SHEET_SIZE
    if sheet.width != sheet.height or sheet.width % SHEET_SIZE != 0:
        print(f"expected a square {SHEET_SIZE}x{SHEET_SIZE} grid, got {sheet.size}", file=sys.stderr)
        return 1

    targets = target_keys()
    sources = source_keys()
    where = {key: index for index, key in enumerate(sources)}

    reordered = Image.new("RGBA", sheet.size)
    moved = 0
    for index, key in enumerate(targets):
        origin = where.get(key)
        if origin is None:
            print(f"no source tile for {key}", file=sys.stderr)
            return 1
        source = sheet.crop((origin % SHEET_SIZE * tile, origin // SHEET_SIZE * tile,
                             (origin % SHEET_SIZE + 1) * tile,
                             (origin // SHEET_SIZE + 1) * tile))
        reordered.paste(source, (index % SHEET_SIZE * tile, index // SHEET_SIZE * tile))
        if origin != index:
            moved += 1

    # Keep the drawing: the reorder is not reversible by eye once it is saved over.
    backup = path + ".orig"
    if not os.path.exists(backup):
        shutil.copyfile(path, backup)
        print(f"  backup: {backup}")

    reordered.save(path)
    print(f"  {path}: {reordered.width}x{reordered.height}, {moved}/{len(targets)} tiles moved")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
