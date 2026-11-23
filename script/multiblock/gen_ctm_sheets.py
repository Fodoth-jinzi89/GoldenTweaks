"""Assemble Create's connected-texture sheet from this mod's per-tile CTM artwork.

Create's `RECTANGLE` type indexes its sheet with a four-bit mask:

    index = (up ? 1 : 0) + (down ? 2 : 0) + (left ? 4 : 0) + (right ? 8 : 0)

so the sheet is a 4x4 grid of 16px tiles. The artwork this mod ships is the 47-tile scheme, which uses
a different index order; handing it over unchanged is why every face sampled the wrong tile and the
plate came out as garbage.

Rather than trust a remembered index table, each of the 47 tiles is classified by its own pixels: in
this art style a side that does not connect is drawn with a dark border, and a side that does
continues the panel. That gives every tile a mask, and the mask is exactly Create's index. The mapping
is therefore derived from the files, not asserted.

    python script/multiblock/gen_ctm_sheets.py
"""
import os
import sys

from PIL import Image

TEXTURES = "src/main/resources/assets/goldentweaks/textures/block"
CTM = os.path.join(TEXTURES, "ctm")

TILE = 16
COLUMNS = 4
ROWS = 4
MASK_COUNT = 16

# A bordered side is noticeably darker than the panel fill, which sits near 208 in this artwork.
BORDER_LUMINANCE = 195.0


def edge_luminance(pixels, edge):
    """Mean luminance of the middle of an edge."""
    if edge == "up":
        coords = [(x, 1) for x in range(4, 12)]
    elif edge == "down":
        coords = [(x, TILE - 2) for x in range(4, 12)]
    elif edge == "left":
        coords = [(1, y) for y in range(4, 12)]
    else:
        coords = [(TILE - 2, y) for y in range(4, 12)]

    total = 0.0
    count = 0
    for x, y in coords:
        r, g, b, a = pixels[x, y]
        if a < 32:
            continue
        total += (r + g + b) / 3.0
        count += 1
    return total / count if count else 255.0


def classify(path):
    """The connection mask a tile draws, or None when it has no usable pixels."""
    image = Image.open(path).convert("RGBA")
    if image.size != (TILE, TILE):
        image = image.resize((TILE, TILE), Image.NEAREST)
    pixels = image.load()

    if not any(pixels[x, y][3] > 32 for y in range(TILE) for x in range(TILE)):
        return None

    mask = 0
    # A side that connects has no border, so it is the bright side.
    for bit, edge in ((1, "up"), (2, "down"), (4, "left"), (8, "right")):
        if edge_luminance(pixels, edge) >= BORDER_LUMINANCE:
            mask |= bit
    return mask


def collect(tile_dir):
    """The best tile for each mask, preferring the first that matches."""
    by_mask = {}
    for index in range(47):
        path = os.path.join(tile_dir, f"{index}.png")
        if not os.path.exists(path):
            continue
        mask = classify(path)
        if mask is None:
            continue
        by_mask.setdefault(mask, index)
    return by_mask


def pack(tile_dir):
    """Pack a casing's tiles into Create's 4x4 sheet."""
    by_mask = collect(tile_dir)
    sheet = Image.new("RGBA", (COLUMNS * TILE, ROWS * TILE), (0, 0, 0, 0))
    unresolved = []

    for mask in range(MASK_COUNT):
        source = by_mask.get(mask)
        if source is None:
            # Nothing in the artwork draws this exact combination. The nearest mask with fewer
            # connections is the closest panel; a blank tile would be far more visible.
            source = nearest(by_mask, mask)
        if source is None:
            unresolved.append(mask)
            continue

        tile = Image.open(os.path.join(tile_dir, f"{source}.png")).convert("RGBA")
        if tile.size != (TILE, TILE):
            tile = tile.resize((TILE, TILE), Image.NEAREST)
        sheet.paste(tile, ((mask % COLUMNS) * TILE, (mask // COLUMNS) * TILE))

    return sheet, by_mask, unresolved


def nearest(by_mask, mask):
    """The tile whose mask differs from this one by the fewest bits."""
    best = None
    best_distance = 99
    for candidate in by_mask:
        distance = bin(candidate ^ mask).count("1")
        if distance < best_distance:
            best_distance = distance
            best = by_mask[candidate]
    return best


def main():
    if not os.path.isdir(CTM):
        print("no ctm directory", file=sys.stderr)
        return 1

    written = 0
    for name in sorted(os.listdir(CTM)):
        tile_dir = os.path.join(CTM, name)
        if not os.path.isdir(tile_dir):
            continue

        sheet, by_mask, unresolved = pack(tile_dir)
        out = os.path.join(TEXTURES, f"{name}_ctm.png")
        sheet.save(out)

        found = ", ".join(f"{m:04b}" for m in sorted(by_mask))
        print(f"  {name}_ctm.png  {sheet.size[0]}x{sheet.size[1]}")
        print(f"    masks drawn by the artwork: {len(by_mask)}/16  [{found}]")
        if unresolved:
            print(f"    no tile at all for masks: {unresolved}")
        written += 1

    print(f"sheets written: {written}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
