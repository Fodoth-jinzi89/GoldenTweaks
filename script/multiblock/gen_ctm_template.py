"""Draw the template for a Create connected-texture sheet.

Create's `RECTANGLE` type picks a tile by a four-bit mask of which sides connect:

    index = (up ? 1 : 0) + (down ? 2 : 0) + (left ? 4 : 0) + (right ? 8 : 0)

so the sheet is a 4x4 grid of 16px tiles — 64x64 in total. That is not the 47-tile OptiFine layout, and
feeding it one is why every face sampled the wrong tile.

Two files are written to `data_sheets`:

  create_ctm_template.png            64x64, the sheet itself, with the grid marked
  create_ctm_template_reference.png  256x256, the same grid at 4x annotated with each mask

A tile connects on a side by continuing the panel; it is bordered on a side that connects to nothing.
"""
import os

from PIL import Image, ImageDraw, ImageFont

OUT = "data_sheets"

TILE = 16
COLUMNS = 4
ROWS = 4
SCALE = 4

BORDER = (60, 60, 60, 255)
FILL = (236, 236, 236, 255)
TEXT = (30, 30, 30, 255)

# Side letters per bit, in Create's order, for the annotation.
SIDES = ((1, "U"), (2, "D"), (4, "L"), (8, "R"))


def describe(mask):
    """The connected sides of a mask, as letters, or '-' for an isolated tile."""
    letters = "".join(name for bit, name in SIDES if mask & bit)
    return letters if letters else "-"


def load_font(size):
    """A font that can draw the text, falling back to the bitmap default."""
    for candidate in ("C:/Windows/Fonts/msyh.ttc", "C:/Windows/Fonts/simhei.ttf",
                      "C:/Windows/Fonts/arial.ttf"):
        if os.path.exists(candidate):
            try:
                return ImageFont.truetype(candidate, size)
            except OSError:
                continue
    return ImageFont.load_default()


def draw_grid(image, draw, tile, columns, rows):
    """A grid at the given tile size, with the outer border heavier than the inner lines."""
    width = columns * tile
    height = rows * tile
    for column in range(1, columns):
        x = column * tile
        draw.line([(x, 0), (x, height)], fill=BORDER, width=1)
    for row in range(1, rows):
        y = row * tile
        draw.line([(0, y), (width, y)], fill=BORDER, width=1)
    draw.rectangle([0, 0, width - 1, height - 1], outline=BORDER, width=1)


def main():
    os.makedirs(OUT, exist_ok=True)

    # ---- the sheet itself: 64x64, one tile per mask, in index order ----------------------------
    sheet = Image.new("RGBA", (COLUMNS * TILE, ROWS * TILE), FILL)
    draw = ImageDraw.Draw(sheet)
    draw_grid(sheet, draw, TILE, COLUMNS, ROWS)

    small = load_font(9)
    for mask in range(COLUMNS * ROWS):
        x = (mask % COLUMNS) * TILE
        y = (mask // COLUMNS) * TILE
        # The index is what the type returns, so it is the one number a tile is identified by.
        draw.text((x + 2, y + 1), str(mask), fill=TEXT, font=small)
        draw.text((x + 2, y + TILE - 10), describe(mask), fill=TEXT, font=small)

    sheet_path = os.path.join(OUT, "create_ctm_template.png")
    sheet.save(sheet_path)
    print(f"  {sheet_path}  {sheet.size[0]}x{sheet.size[1]}")

    # ---- the reference: the same grid, large enough to annotate --------------------------------
    big_tile = TILE * SCALE
    reference = Image.new("RGBA", (COLUMNS * big_tile, ROWS * big_tile), FILL)
    draw = ImageDraw.Draw(reference)
    draw_grid(reference, draw, big_tile, COLUMNS, ROWS)

    title = load_font(13)
    body = load_font(10)
    for mask in range(COLUMNS * ROWS):
        x = (mask % COLUMNS) * big_tile
        y = (mask // COLUMNS) * big_tile

        draw.text((x + 4, y + 3), f"#{mask}", fill=TEXT, font=title)
        draw.text((x + 4, y + 20), f"{mask:04b}", fill=TEXT, font=body)

        # The four sides, drawn as marks on the edges the tile connects to.
        middle_x = x + big_tile // 2
        middle_y = y + big_tile // 2
        reach = big_tile // 2 - 6
        for bit, name in SIDES:
            if not mask & bit:
                continue
            if name == "U":
                draw.line([(middle_x, y + 4), (middle_x, y + 4 + reach // 2)], fill=(20, 120, 200, 255), width=2)
            elif name == "D":
                draw.line([(middle_x, y + big_tile - 4), (middle_x, y + big_tile - 4 - reach // 2)], fill=(20, 120, 200, 255), width=2)
            elif name == "L":
                draw.line([(x + 4, middle_y), (x + 4 + reach // 2, middle_y)], fill=(20, 120, 200, 255), width=2)
            else:
                draw.line([(x + big_tile - 4, middle_y), (x + big_tile - 4 - reach // 2, middle_y)], fill=(20, 120, 200, 255), width=2)

        draw.text((x + 4, y + big_tile - 16), describe(mask), fill=(20, 120, 200, 255), font=body)

    reference_path = os.path.join(OUT, "create_ctm_template_reference.png")
    reference.save(reference_path)
    print(f"  {reference_path}  {reference.size[0]}x{reference.size[1]}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
