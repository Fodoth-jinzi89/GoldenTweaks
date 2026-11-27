"""Everything for a casing's connected-texture art, in one place.

A casing's art is derived from two 32x32 drawings — a base pattern and a frame — and every step after
that is machinery: the frame becomes sixteen connection states, becomes a corner tile, becomes the
octo-directional sheet, becomes any of the other seven layouts. Getting those steps in the right order
and with matching rules is the whole job, so they live together rather than in a dozen scripts whose
only record of the order is a conversation.

Commands:

    pipeline <base> <frame> <prefix>      base + frame -> every derived sheet
    corners  <frame> <out>                frame -> corner tile only
    omni     <ctm> <corners> <out>        the 4x4 sheet -> octo-directional
    layout   <omni> <layout> <out>        octo-directional -> one of the other seven
    verify   <sheet> [layout]             check a sheet against its connection states
    seams    <omni> [w] [h]               report outline pixels on interior seams
    show     <sheet> <index> [grid]       print one cell pixel for pixel
    preview  <sheet> <w> <h> [out]        lay out a block grid the way the game would read it
    layouts                               list the eight layouts and their sheet sizes

The rules, and why they are what they are:

  * The frame divides on a 6 / 20 / 6 nine-slice. A cell keeps a piece only for the sides it is open
    on, and the centre tiles through the rest. That is what makes one frame serve all sixteen states.

  * The corner tile is not a separate drawing: it is the frame's four corner pieces, kept and turned
    end for end. Turning is what puts each mark in the corner it closes.

  * The octo-directional index is Create's own, transcribed. A single wrong term there moves whole
    columns of cells and looks like an art bug; `verify` is how that gets caught.

  * Deriving a simpler layout from the octo-directional sheet is lossy in one direction only. The
    octo-directional sheet distinguishes 47 states; the simpler layouts read fewer neighbours, so
    several of its cells collapse into one of theirs. The choice is made by assuming the surface
    continues around the corner, and cannot be undone.
"""
import io
import itertools
import json
import os
import sys

from PIL import Image

# ------------------------------------------------------------------ constants

SIDES = ("up", "down", "left", "right")
CORNERS = ("topLeft", "topRight", "bottomLeft", "bottomRight")
ALL = SIDES + CORNERS

CELL = 32
RECT_GRID = 4
OMNI_GRID = 8

# 32 = 6 + 20 + 6: the frame's border is 6px and its interior 20px.
NINE_CUTS = (0, 6, 26, 32)

# Create's layouts and the number of cells per axis each one addresses.
LAYOUTS = {
    "HORIZONTAL": 2,
    "HORIZONTAL_KRYPPERS": 2,
    "VERTICAL": 2,
    "OMNIDIRECTIONAL": 8,
    "ROOF": 4,
    "ROOF_STAIR": 4,
    "CROSS": 4,
    "RECTANGLE": 4,
}


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


# -------------------------------------------------------------- index formulas
# Transcribed from Create's AllCTTypes, each as its own function so they can be read next to the
# original. Only OMNIDIRECTIONAL reads the diagonals.


def idx_horizontal(ctx):
    return (1 if ctx["right"] else 0) + (2 if ctx["left"] else 0)


def idx_horizontal_kryppers(ctx):
    if not ctx["right"] and not ctx["left"]:
        return 0
    return 3 if not ctx["right"] else (2 if not ctx["left"] else 1)


def idx_vertical(ctx):
    return (1 if ctx["up"] else 0) + (2 if ctx["down"] else 0)


def idx_omni(ctx):
    tile_x = 0
    tile_y = 0
    borders = ((0 if ctx["up"] else 1) + (0 if ctx["down"] else 1)
               + (0 if ctx["left"] else 1) + (0 if ctx["right"] else 1))
    if ctx["up"]:
        tile_x += 1
    if ctx["down"]:
        tile_x += 2
    if ctx["left"]:
        tile_y += 1
    if ctx["right"]:
        tile_y += 2

    if borders == 0:
        if ctx["topRight"]:
            tile_x += 1
        if ctx["topLeft"]:
            tile_x += 2
        if ctx["bottomRight"]:
            tile_y += 2
        if ctx["bottomLeft"]:
            tile_y += 1

    if borders == 1:
        if not ctx["right"] and (ctx["topLeft"] or ctx["bottomLeft"]):
            tile_y = 4
            tile_x = -1 + (1 if ctx["bottomLeft"] else 0) + (1 if ctx["topLeft"] else 0) * 2
        if not ctx["left"] and (ctx["topRight"] or ctx["bottomRight"]):
            tile_y = 5
            tile_x = -1 + (1 if ctx["bottomRight"] else 0) + (1 if ctx["topRight"] else 0) * 2
        if not ctx["down"] and (ctx["topLeft"] or ctx["topRight"]):
            tile_y = 6
            tile_x = -1 + (1 if ctx["topLeft"] else 0) + (1 if ctx["topRight"] else 0) * 2
        if not ctx["up"] and (ctx["bottomLeft"] or ctx["bottomRight"]):
            tile_y = 7
            tile_x = -1 + (1 if ctx["bottomLeft"] else 0) + (1 if ctx["bottomRight"] else 0) * 2

    if borders == 2 and (
            (ctx["up"] and ctx["left"] and ctx["topLeft"])
            or (ctx["down"] and ctx["left"] and ctx["bottomLeft"])
            or (ctx["up"] and ctx["right"] and ctx["topRight"])
            or (ctx["down"] and ctx["right"] and ctx["bottomRight"])):
        tile_x += 3

    return tile_x + 8 * tile_y


def idx_cross(ctx):
    return ((1 if ctx["up"] else 0) + (2 if ctx["down"] else 0)
            + (4 if ctx["left"] else 0) + (8 if ctx["right"] else 0))


def idx_rectangle(ctx):
    x = 2 if (ctx["left"] and ctx["right"]) else (3 if ctx["left"] else (1 if ctx["right"] else 0))
    y = 1 if (ctx["up"] and ctx["down"]) else (2 if ctx["up"] else (0 if ctx["down"] else 3))
    return x + y * 4


ROOF_STAIR_MAP = [[1, 6, 9, 4], [14, 12, 13, 15], [2, 10, 8, 0], [5, 5, 5, 5]]


def idx_roof_stair(ctx):
    kind = (2 if ctx["up"] else 0) + (1 if ctx["right"] else 0)
    rot = (2 if ctx["left"] else 0) + (1 if ctx["down"] else 0)
    return ROOF_STAIR_MAP[kind][rot]


def idx_roof(ctx):
    up_drops = ctx["down"] and not ctx["up"] and (ctx["left"] or ctx["right"])
    down_drops = not ctx["down"] and ctx["up"] and (ctx["left"] or ctx["right"])
    left_drops = not ctx["left"] and ctx["right"] and (ctx["up"] or ctx["down"])
    right_drops = ctx["left"] and not ctx["right"] and (ctx["up"] or ctx["down"])

    if up_drops:
        if left_drops:
            return 0 if ctx["bottomRight"] else 5
        if right_drops:
            return 2 if ctx["bottomLeft"] else 5
        return 1
    if down_drops:
        if left_drops:
            return 8 if ctx["topRight"] else 5
        if right_drops:
            return 10 if ctx["topLeft"] else 5
        return 9
    if left_drops:
        return 4
    if right_drops:
        return 6
    if ctx["up"] and ctx["down"] and ctx["left"] and ctx["right"]:
        if ctx["bottomLeft"] and ctx["topRight"]:
            if ctx["topLeft"] and not ctx["bottomRight"]:
                return 12
            if ctx["bottomRight"] and not ctx["topLeft"]:
                return 15
            if not ctx["bottomRight"] and not ctx["topLeft"]:
                return 7
        if ctx["bottomRight"] and ctx["topLeft"]:
            if ctx["topRight"] and not ctx["bottomLeft"]:
                return 13
            if ctx["bottomLeft"] and not ctx["topRight"]:
                return 14
            if not ctx["bottomLeft"] and not ctx["topRight"]:
                return 11
        return 5
    return 5


INDEX_OF = {
    "HORIZONTAL": idx_horizontal,
    "HORIZONTAL_KRYPPERS": idx_horizontal_kryppers,
    "VERTICAL": idx_vertical,
    "OMNIDIRECTIONAL": idx_omni,
    "ROOF": idx_roof,
    "ROOF_STAIR": idx_roof_stair,
    "CROSS": idx_cross,
    "RECTANGLE": idx_rectangle,
}


def contexts():
    return [dict(zip(ALL, bits)) for bits in itertools.product((False, True), repeat=8)]


def cell_states(layout):
    """The connection state each cell of a layout stands for, taking the first reached."""
    table = {}
    index_of = INDEX_OF[layout]
    for ctx in contexts():
        table.setdefault(index_of(ctx), ctx)
    return table


# ------------------------------------------------------------------- art steps


def rectangle_states():
    """The connection state each RECTANGLE index stands for."""
    return cell_states("RECTANGLE")


# The sixteen connection states, laid out as an 8x8 grid of 4px squares. Each letter names a piece of
# the frame, following the formula the artwork was drawn to:
#
#     A B A G G G G B        A B    frame's top-left / top-right quadrant
#     E F E 0 0 0 0 F        C D    frame's bottom-left / bottom-right quadrant
#     E F E 0 0 0 0 F        E F    the middle band: E is its left end, F the row through it
#     E F E 0 0 0 0 F        G H    the side band: G its top half, H the row through it
#     E F E 0 0 0 0 F        0      the centre, tiled
#     C D C H H H H D
#     A B A G G G G B
#     C D C H H H H D
#
# A cell keeps a border on a side it is open on and continues into the base on a side it connects on,
# which is what the eight rows spell out. Reading it as pixel windows is what makes one 32x32 drawing
# yield all sixteen states.

# Each entry is (source x, source y) of a 16x16 window on the frame. The frame is 32x32, so those
# windows are the four quadrants and the two middle strips.
CT_PIECES = {
    "A": (0, 0),
    "B": (16, 0),
    "C": (8, 0),
    "D": (0, 8),
    "E": (16, 8),
    "F": (0, 16),
    "G": (16, 16),
    "H": (8, 16),
}

CT_LAYOUT = (
    "ABACCCCB",
    "DED....E",
    "DED....E",
    "DED....E",
    "DED....E",
    "FGFHHHHG",
    "ABACCCCB",
    "FGFHHHHG",
)

def border_thickness(frame):
    """角块的边长：九宫格该在哪里切。

    量法是从边缘向内数「整行铺满」的行数，但单靠它不够：一张角上有斜纹装饰的模板（PP）只在
    最外 2 行是满的，它的角块却延伸到 6px；而一张线条简洁的模板（PE）最外 4 行都满，角块就是
    4px。区分点是 4 —— 实心区不到 4 行说明角块比实心区更大，取模板的 6px 网格。

    这个值可以覆盖：`pipeline --corner N`。
    """
    width, height = frame.size
    solid = 0
    for y in range(height // 2):
        row = [frame.getpixel((x, y))[3] for x in range(width)]
        if all(alpha > 16 for alpha in row):
            solid += 1
        else:
            break

    # 实心区够厚，就是角块本身；太薄说明角块被装饰包着，仍按 6px 网格切。
    return solid if solid >= 4 else 6


# The four pieces that sit against the sheet's interior — C, H along the horizontal run and D, E along
# the vertical one — do not copy their window verbatim. A window taken from a frame's border carries the
# border's taper, and pasted whole it reads as a seam at the join. The artwork instead takes that
# window's middle band and mirrors it outward to fill the 16px square, which turns the taper into a
# repeating pattern that continues across the join.
#
# The band's width is a quarter of the window, which is what makes the mirrors line up for both a 6px
# corner and a 4px one: the taper it spans is proportional to the corner it belongs to.
CT_MIRROR_X = {"C", "H"}
CT_MIRROR_Y = {"D", "E"}


def _mirror_axis(piece, axis):
    """Fill a piece along one axis by mirroring its middle band outward."""
    width, height = piece.size
    length = width if axis == "x" else height
    quarter = length // 4
    out = Image.new("RGBA", piece.size, (0, 0, 0, 0))

    for index in range(height if axis == "x" else width):
        if axis == "x":
            line = [piece.getpixel((x, index)) for x in range(width)]
        else:
            line = [piece.getpixel((index, y)) for y in range(height)]

        band = line[quarter:quarter + 2 * quarter]
        if len(band) < 2 * quarter:
            rebuilt = line
        else:
            lead = band[:quarter][::-1]
            tail = band[:quarter]
            built = lead + band + tail
            # Still short only when the window itself is narrower than the square; repeat the band.
            while len(built) < length:
                built = built + band
            rebuilt = built[:length]

        for position in range(length):
            if axis == "x":
                out.putpixel((position, index), rebuilt[position])
            else:
                out.putpixel((index, position), rebuilt[position])
    return out


def _mirror_x(piece):
    return _mirror_axis(piece, "x")


def _mirror_y(piece):
    return _mirror_axis(piece, "y")


def build_rectangle_sheet(frame):
    """The 4x4 sheet: sixteen states, each 32x32, drawn from 16x16 windows on the frame.

    The centre of the sheet is left transparent so the base shows through — that is why the layout's
    middle is dots rather than a letter.
    """
    sheet = Image.new("RGBA", (CELL * RECT_GRID, CELL * RECT_GRID), (0, 0, 0, 0))
    window = 16

    for row, line in enumerate(CT_LAYOUT):
        for column, letter in enumerate(line):
            if letter == ".":
                continue
            x, y = CT_PIECES[letter]
            piece = frame.crop((x, y, x + window, y + window))
            if letter in CT_MIRROR_X:
                piece = _mirror_x(piece)
            elif letter in CT_MIRROR_Y:
                piece = _mirror_y(piece)
            sheet.paste(piece, (column * window, row * window))
    return sheet


def build_rectangle_cell(frame, base, state, size=CELL):
    """One cell of the 4x4 sheet, for callers that want a single state rather than the sheet.

    Kept as the inverse of the layout: a cell whose sides are open is where the borders sit, so a cell
    that connects on all four sides is left empty.
    """
    tile = Image.new("RGBA", (size, size), (0, 0, 0, 0))

    # Which sheet index this state is, then cut it out of the laid-out sheet.
    index = idx_rectangle(state)
    sheet = build_rectangle_sheet(frame)
    column, row = index % RECT_GRID, index // RECT_GRID
    return sheet.crop((column * size, row * size, (column + 1) * size, (row + 1) * size))


def build_corner_tile(frame):
    """The corner tile: the frame's four corner pieces, kept in place.

    The split is measured, not assumed: a template drawn with a 4px border has its corners in the
    first 4px, and cutting at 6 would take a slice of the side into the corner and leave the mark
    the wrong shape.

    No rotation is applied. An earlier version turned the result 180 degrees, which happens to be a
    no-op for a template whose corners are point-symmetric (so it passed) but moves a mark to the
    opposite corner for one whose are not — the mark then closes the wrong side. Keeping the pieces
    where they were drawn is what puts each mark in the corner it closes.
    """
    width, height = frame.size
    border = border_thickness(frame)
    cuts = (0, border, width - border, width)

    corners = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    for slot_x, slot_y in ((0, 0), (2, 0), (0, 2), (2, 2)):
        piece = frame.crop((cuts[slot_x], cuts[slot_y], cuts[slot_x + 1], cuts[slot_y + 1]))
        corners.paste(piece, (cuts[slot_x], cuts[slot_y]))
    return corners


def corner_needs(ctx):
    """The corners a combination needs a mark on.

    A mark goes wherever two connected sides meet without the diagonal between them being connected —
    the junction where the surface steps inward. This holds for interior cells too: an earlier version
    skipped cells whose four sides were all connected, which left them visibly unmarked.
    """
    up, down, left, right = (ctx[s] for s in SIDES)
    needs = set()
    if up and left and not ctx["topLeft"]:
        needs.add("topLeft")
    if up and right and not ctx["topRight"]:
        needs.add("topRight")
    if down and left and not ctx["bottomLeft"]:
        needs.add("bottomLeft")
    if down and right and not ctx["bottomRight"]:
        needs.add("bottomRight")
    return frozenset(needs)


def find_marks(corner_sheet, recolour=None):
    """The four marks, keyed by quadrant, cropped to the mark alone.

    A mark is the opaque pixels of each quadrant, not the darker ones: a template is often drawn in
    arbitrary colours that carry position rather than tone, and a luminance threshold finds nothing.
    """
    width, height = corner_sheet.size
    half_w, half_h = width // 2, height // 2
    quadrants = {
        "topLeft": (0, 0, half_w, half_h),
        "topRight": (half_w, 0, width, half_h),
        "bottomLeft": (0, half_h, half_w, height),
        "bottomRight": (half_w, half_h, width, height),
    }

    pixels = corner_sheet.load()
    marks = {}
    for name, (x0, y0, x1, y1) in quadrants.items():
        found = [(x, y) for y in range(y0, y1) for x in range(x0, x1) if pixels[x, y][3] > 16]
        if not found:
            raise ValueError(f"no mark found in the {name} quadrant")
        left = min(x for x, _ in found)
        top = min(y for _, y in found)
        right = max(x for x, _ in found) + 1
        bottom = max(y for _, y in found) + 1

        mark = corner_sheet.crop((left, top, right, bottom))
        if recolour is not None:
            flat = Image.new("RGBA", mark.size, (recolour[0], recolour[1], recolour[2], 255))
            flat.putalpha(mark.split()[3])
            mark = flat
        marks[name] = mark
    return marks


def build_omni_table():
    """Every octo-directional cell, with the source cell and the corner marks it needs."""
    table = {}
    conflicts = {}
    for ctx in contexts():
        target = idx_omni(ctx)
        entry = (idx_rectangle(ctx), corner_needs(ctx))
        if target in table and table[target] != entry:
            conflicts.setdefault(target, {table[target]}).add(entry)
        else:
            table.setdefault(target, entry)
    return table, conflicts


# Where each mark goes, and which way it faces.
#
# The position is the identity mapping: a mark taken from the frame's top-left corner closes the cell's
# top-left corner. That is fixed.
#
# What is not fixed is the mark's *orientation*. A corner piece is drawn to sit in the frame's corner,
# where its outline faces outward; pasted into an inner corner it has to face inward instead, which is
# the same piece turned end for end about its own centre. So each mark is rotated 180 degrees before
# being pasted — its anchor stays where it was, only the pixels inside turn around.
#
# `--no-flip-marks` pastes them unturned.
ANCHOR = {"topLeft": (0, 0), "topRight": (1, 0),
          "bottomLeft": (0, 1), "bottomRight": (1, 1)}


def convert_to_omni(ct_sheet, corner_sheet, recolour=None, flip_marks=True):
    """The 4x4 sheet plus a corner tile, rewritten as the octo-directional sheet."""
    width, height = ct_sheet.size
    if width % RECT_GRID or height % RECT_GRID:
        raise ValueError(f"{width}x{height} is not a {RECT_GRID}x{RECT_GRID} sheet")
    cell = width // RECT_GRID
    if height // RECT_GRID != cell:
        raise ValueError("cells are not square")

    table, conflicts = build_omni_table()
    if conflicts:
        raise ValueError(f"ambiguous cells: {sorted(conflicts)}")

    cells = [ct_sheet.crop((c * cell, r * cell, (c + 1) * cell, (r + 1) * cell))
             for r in range(RECT_GRID) for c in range(RECT_GRID)]

    marks = find_marks(corner_sheet, recolour)

    out = Image.new("RGBA", (cell * OMNI_GRID, cell * OMNI_GRID), (0, 0, 0, 0))
    placed = 0
    for index, (source, needs) in sorted(table.items()):
        tile = cells[source].copy()
        for quadrant in sorted(needs):
            mark = marks[quadrant]
            if flip_marks:
                # Turn the mark about its own centre; the anchor below stays put.
                mark = mark.transpose(Image.Transpose.ROTATE_180)
            column, row = ANCHOR[quadrant]
            x = 0 if column == 0 else cell - mark.width
            y = 0 if row == 0 else cell - mark.height
            tile.paste(mark, (x, y))
            placed += 1
        out.paste(tile, ((index % OMNI_GRID) * cell, (index // OMNI_GRID) * cell))
    return out, len(table), placed


def choose_omni_source(group, layout):
    """The octo-directional cell to use for one cell of a simpler layout.

    For OMNIDIRECTIONAL the mapping is the identity — each of its cells is reached by exactly one
    combination, so it is already the right picture.

    For the others the diagonals are filled in as if the surface continued around the corner. That is a
    choice, not a lookup: those layouts do not read the diagonals, so several octo-directional cells
    suit one of their cells equally.
    """
    if layout == "OMNIDIRECTIONAL":
        return idx_omni(group[0])

    votes = {}
    for ctx in group:
        filled = dict(ctx)
        for corner, (a, b) in (("topLeft", ("up", "left")), ("topRight", ("up", "right")),
                               ("bottomLeft", ("down", "left")), ("bottomRight", ("down", "right"))):
            filled[corner] = ctx[a] and ctx[b]
        index = idx_omni(filled)
        votes[index] = votes.get(index, 0) + 1
    return max(votes.items(), key=lambda kv: (kv[1], -kv[0]))[0]


def cut_layout(omni_sheet, layout):
    """Cut one of the other layouts out of an octo-directional sheet."""
    grid = LAYOUTS[layout]
    cell = omni_sheet.size[0] // OMNI_GRID
    index_of = INDEX_OF[layout]

    groups = {}
    for ctx in contexts():
        groups.setdefault(index_of(ctx), []).append(ctx)

    out = Image.new("RGBA", (cell * grid, cell * grid), (0, 0, 0, 0))
    picked = {}
    for index in sorted(groups):
        source = choose_omni_source(groups[index], layout)
        picked[index] = source
        tile = omni_sheet.crop(((source % OMNI_GRID) * cell, (source // OMNI_GRID) * cell,
                                (source % OMNI_GRID + 1) * cell, (source // OMNI_GRID + 1) * cell))
        out.paste(tile, ((index % grid) * cell, (index // grid) * cell))
    return out, picked


# ------------------------------------------------------------------- commands


def cmd_pipeline(argv):
    flip_marks = "--no-flip-marks" not in argv
    argv = [a for a in argv if a != "--no-flip-marks"]
    if len(argv) < 3:
        print("usage: pipeline <base.png> <frame.png> <out_prefix> [--no-flip-marks]")
        return 1
    base_path, frame_path, prefix = argv[0], argv[1], argv[2]

    base = Image.open(base_path).convert("RGBA")
    frame = Image.open(frame_path).convert("RGBA")
    for path, image in ((base_path, base), (frame_path, frame)):
        if image.size != (CELL, CELL):
            print(f"  {path}: expected {CELL}x{CELL}, got {image.size[0]}x{image.size[1]}")
            return 1

    os.makedirs(os.path.dirname(os.path.abspath(prefix)) or ".", exist_ok=True)

    # The base, repeated. Straight copies: resampling would blur the seams between cells.
    large = Image.new("RGBA", (CELL * RECT_GRID, CELL * RECT_GRID), (0, 0, 0, 0))
    for row in range(RECT_GRID):
        for column in range(RECT_GRID):
            large.paste(base, (column * CELL, row * CELL))
    large.save(f"{prefix}_base_large.png")

    # The 4x4 sheet of connection states, drawn as 16px windows of the frame. Its middle stays
    # transparent, so the base shows through there once the two are composited.
    ct = build_rectangle_sheet(frame)
    ct.save(f"{prefix}_ct.png")

    # The corner tile, and the same thing written beside the frame for reuse.
    corners = build_corner_tile(frame)
    corners.save(f"{prefix}_r.png")

    # The base with the sheet laid over it: the frame's borders on top, the base pattern between them.
    ctm = Image.alpha_composite(large, ct)
    ctm.save(f"{prefix}_ctm.png")

    # And the octo-directional form, which the game prefers because it draws inner corners.
    omni, filled, placed = convert_to_omni(ctm, corners, flip_marks=flip_marks)
    omni.save(f"{prefix}_omni.png")

    print(f"  {base_path} + {frame_path} -> {prefix}_*")
    for suffix in ("base_large", "ct", "r", "ctm", "omni"):
        path = f"{prefix}_{suffix}.png"
        with Image.open(path) as image:
            print(f"    {os.path.basename(path):28s} {image.size[0]:3d}x{image.size[1]:<3d} "
                  f"{os.path.getsize(path):6d} B")
    print(f"    octo-directional: {filled} cells filled, {placed} corner marks placed")
    return 0


def cmd_corners(argv):
    if len(argv) < 2:
        print("usage: corners <frame.png> <out.png>")
        return 1
    frame = Image.open(argv[0]).convert("RGBA")
    if frame.size != (CELL, CELL):
        print(f"  {argv[0]}: expected {CELL}x{CELL}")
        return 1
    out = build_corner_tile(frame)
    os.makedirs(os.path.dirname(os.path.abspath(argv[1])) or ".", exist_ok=True)
    out.save(argv[1])
    print(f"  {argv[0]} -> {argv[1]} (corner tile, four pieces turned 180)")
    return 0


def cmd_omni(argv):
    recolour = None
    flip_marks = True
    positional = []
    index = 0
    while index < len(argv):
        if argv[index] == "--recolour" and index + 1 < len(argv):
            value = argv[index + 1].lstrip("#")
            recolour = tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))
            index += 2
        elif argv[index] == "--no-flip-marks":
            flip_marks = False
            index += 1
        else:
            positional.append(argv[index])
            index += 1

    if len(positional) < 3:
        print("usage: omni <ctm.png> <corners.png> <out.png> "
              "[--recolour RRGGBB] [--no-flip-marks]")
        return 1

    ct_sheet = Image.open(positional[0]).convert("RGBA")
    corner_sheet = Image.open(positional[1]).convert("RGBA")
    out, filled, placed = convert_to_omni(ct_sheet, corner_sheet, recolour, flip_marks)
    os.makedirs(os.path.dirname(os.path.abspath(positional[2])) or ".", exist_ok=True)
    out.save(positional[2])
    print(f"  {positional[0]} + {positional[1]} -> {positional[2]}")
    print(f"    {out.size[0]}x{out.size[1]}, {filled} cells filled, {placed} marks placed"
          f"{', marks flipped' if flip_marks else ''}")
    return 0


def cmd_layout(argv):
    if len(argv) < 3:
        print("usage: layout <omni.png> <layout> <out.png>")
        return 1
    name = argv[1].upper()
    if name not in LAYOUTS:
        print(f"  unknown layout '{argv[1]}'; try: {', '.join(LAYOUTS)}")
        return 1
    sheet = Image.open(argv[0]).convert("RGBA")
    if sheet.size[0] % OMNI_GRID:
        print(f"  {argv[0]}: not an {OMNI_GRID}x{OMNI_GRID} sheet")
        return 1
    out, picked = cut_layout(sheet, name)
    os.makedirs(os.path.dirname(os.path.abspath(argv[2])) or ".", exist_ok=True)
    out.save(argv[2])
    print(f"  {argv[0]} -> {argv[2]}  ({name}, {out.size[0]}x{out.size[1]}, "
          f"{len(picked)} cells)")
    return 0


def cmd_verify(argv):
    if not argv:
        print("usage: verify <sheet.png> [layout]")
        return 1
    path = argv[0]
    sheet = Image.open(path).convert("RGBA")
    pixels = sheet.load()

    # With no layout named, guess from the size. Only two sizes are unambiguous.
    if len(argv) > 1:
        layout = argv[1].upper()
    elif sheet.size[0] == CELL * OMNI_GRID:
        layout = "OMNIDIRECTIONAL"
    elif sheet.size[0] == CELL * RECT_GRID:
        layout = "RECTANGLE"
    else:
        print(f"  cannot guess the layout of a {sheet.size[0]}px sheet; name one")
        return 1

    if layout not in LAYOUTS:
        print(f"  unknown layout '{layout}'")
        return 1
    grid = LAYOUTS[layout]
    cell = sheet.size[0] // grid

    tones = [luminance(pixels[x, y]) for y in range(sheet.size[1]) for x in range(sheet.size[0])
             if pixels[x, y][3] > 0]
    threshold = (min(tones) + max(tones)) // 2

    table = cell_states(layout)

    def edge_dark(index, side):
        column, row = index % grid, index // grid
        x0, y0 = column * cell, row * cell
        lo, hi = cell // 3, 2 * cell // 3
        if side == "up":
            points = [(x0 + x, y0) for x in range(lo, hi)]
        elif side == "down":
            points = [(x0 + x, y0 + cell - 1) for x in range(lo, hi)]
        elif side == "left":
            points = [(x0, y0 + y) for y in range(lo, hi)]
        else:
            points = [(x0 + cell - 1, y0 + y) for y in range(lo, hi)]
        dark = sum(1 for x, y in points
                   if pixels[x, y][3] > 0 and luminance(pixels[x, y]) < threshold)
        return dark / len(points)

    wrong = []
    for index in sorted(table):
        for side in SIDES:
            connected = table[index][side]
            ratio = edge_dark(index, side)
            bordered = ratio > 0.5
            if connected and bordered:
                wrong.append((index, side, "connected but bordered", ratio))
            elif not connected and not bordered:
                wrong.append((index, side, "open but clear", ratio))

    print(f"  {path}: {layout}, {grid}x{grid}, cell {cell}, {len(table)} cells")
    if wrong:
        print(f"  edges contradicting their state: {len(wrong)}")
        for index, side, why, ratio in wrong[:15]:
            edges = "".join("Y" if table[index][s] else "n" for s in SIDES)
            print(f"    cell {index:2d} ({edges}): {side} {why} ({ratio:.0%})")
        return 1
    print("  every edge matches its state")
    return 0


def cmd_show(argv):
    if len(argv) < 2:
        print("usage: show <sheet.png> <index> [grid]")
        return 1
    sheet = Image.open(argv[0]).convert("RGBA")
    index = int(argv[1])
    grid = int(argv[2]) if len(argv) > 2 else (OMNI_GRID if sheet.size[0] % OMNI_GRID == 0
                                               else RECT_GRID)
    cell = sheet.size[0] // grid
    column, row = index % grid, index // grid
    tile = sheet.crop((column * cell, row * cell, (column + 1) * cell, (row + 1) * cell))

    tones = [luminance(sheet.getpixel((x, y)))
             for y in range(sheet.size[1]) for x in range(sheet.size[0])
             if sheet.getpixel((x, y))[3] > 0]
    threshold = (min(tones) + max(tones)) // 2

    print(f"  {argv[0]} cell {index} of {grid}x{grid} ({cell}px)")
    print("      " + "".join(str(x % 10) for x in range(cell)))
    for y in range(cell):
        chars = []
        for x in range(cell):
            r, g, b, a = tile.getpixel((x, y))
            if a < 32:
                chars.append(" ")
            elif luminance((r, g, b)) < threshold:
                chars.append("#")
            else:
                chars.append(".")
        print(f"  {y:3d} " + "".join(chars))
    return 0


def cmd_preview(argv):
    if len(argv) < 3:
        print("usage: preview <omni.png> <width> <height> [out.png]")
        return 1
    sheet = Image.open(argv[0]).convert("RGBA")
    width, height = int(argv[1]), int(argv[2])
    cell = sheet.size[0] // OMNI_GRID

    def context_of(x, y):
        def filled(xx, yy):
            return 0 <= xx < width and 0 <= yy < height
        return {"up": filled(x, y - 1), "down": filled(x, y + 1),
                "left": filled(x - 1, y), "right": filled(x + 1, y),
                "topLeft": filled(x - 1, y - 1), "topRight": filled(x + 1, y - 1),
                "bottomLeft": filled(x - 1, y + 1), "bottomRight": filled(x + 1, y + 1)}

    out = Image.new("RGBA", (width * cell, height * cell), (0, 0, 0, 0))
    indices = []
    for y in range(height):
        row = []
        for x in range(width):
            index = idx_omni(context_of(x, y))
            row.append(index)
            tile = sheet.crop(((index % OMNI_GRID) * cell, (index // OMNI_GRID) * cell,
                               (index % OMNI_GRID + 1) * cell, (index // OMNI_GRID + 1) * cell))
            out.paste(tile, (x * cell, y * cell))
        indices.append(row)

    print(f"  {argv[0]}: {width}x{height} layout")
    for row in indices:
        print("    " + " ".join(f"{i:3d}" for i in row))
    if len(argv) > 3:
        out.save(argv[3])
        print(f"  written {argv[3]} ({out.size[0]}x{out.size[1]})")
    return 0


def cmd_seams(argv):
    """Assemble a block grid and report outline pixels landing where blocks meet.

    The failure this catches: a cell keeps a mark it should not, and the mark only becomes visible
    once neighbours surround it. A 2x2 is the smallest grid that exposes it — four inner corners meet
    in the middle, where anything drawn reads as a seam.
    """
    if not argv:
        print("usage: seams <omni.png> [width] [height]")
        return 1
    sheet = Image.open(argv[0]).convert("RGBA")
    width = int(argv[1]) if len(argv) > 1 else 2
    height = int(argv[2]) if len(argv) > 2 else width
    cell = sheet.size[0] // OMNI_GRID

    def context_of(x, y):
        def filled(xx, yy):
            return 0 <= xx < width and 0 <= yy < height
        return {"up": filled(x, y - 1), "down": filled(x, y + 1),
                "left": filled(x - 1, y), "right": filled(x + 1, y),
                "topLeft": filled(x - 1, y - 1), "topRight": filled(x + 1, y - 1),
                "bottomLeft": filled(x - 1, y + 1), "bottomRight": filled(x + 1, y + 1)}

    assembled = [[None] * (width * cell) for _ in range(height * cell)]
    states = [[None] * width for _ in range(height)]
    for by in range(height):
        for bx in range(width):
            ctx = context_of(bx, by)
            index = idx_omni(ctx)
            states[by][bx] = ctx
            for y in range(cell):
                for x in range(cell):
                    pixel = sheet.getpixel(((index % OMNI_GRID) * cell + x,
                                            (index // OMNI_GRID) * cell + y))
                    assembled[by * cell + y][bx * cell + x] = (pixel, index)

    # Walk the interior seams, but only the sides that actually connect, and only their middle run: an
    # open side draws its own outline by design, and a connected side's two ends belong to whichever
    # perpendicular side is open there. Checking the ends too would report those by-design outlines.
    margin = 3
    inset = 12
    offenders = {}
    for by in range(height):
        for bx in range(width):
            x0, y0 = bx * cell, by * cell
            ctx = states[by][bx]
            zones = []
            if ctx["up"] and by > 0:
                zones.append(("up", x0 + inset, x0 + cell - inset, y0, y0 + margin))
            if ctx["down"] and by < height - 1:
                zones.append(("down", x0 + inset, x0 + cell - inset, y0 + cell - margin, y0 + cell))
            if ctx["left"] and bx > 0:
                zones.append(("left", x0, x0 + margin, y0 + inset, y0 + cell - inset))
            if ctx["right"] and bx < width - 1:
                zones.append(("right", x0 + cell - margin, x0 + cell, y0 + inset, y0 + cell - inset))
            for side, zx0, zx1, zy0, zy1 in zones:
                for y in range(zy0, zy1):
                    for x in range(zx0, zx1):
                        pixel, index = assembled[y][x]
                        if pixel[3] == 0 or luminance(pixel) >= 150:
                            continue
                        key = (bx, by, index, side)
                        offenders[key] = offenders.get(key, 0) + 1

    print(f"  {argv[0]}: {width}x{height} assembled, cell {cell}")
    if not offenders:
        print("  interior seams clean: no outline pixels where blocks meet")
        return 0
    total = sum(offenders.values())
    print(f"  outline pixels on connected interior sides: {total}")
    for (bx, by, index, side), count in sorted(offenders.items(), key=lambda kv: -kv[1])[:12]:
        print(f"    block ({bx},{by}) cell {index:2d} {side:6s}: {count} px")
    return 1


def cmd_layouts(argv):
    print("  layout                 cells  sheet size")
    for name, grid in LAYOUTS.items():
        print(f"  {name:22s} {grid * grid:5d}  {CELL * grid}x{CELL * grid}")
    print()
    print("  Only OMNIDIRECTIONAL can be cut from itself; the others collapse several of its cells")
    print("  into one, because they read fewer neighbours.")
    return 0


COMMANDS = {
    "pipeline": cmd_pipeline,
    "corners": cmd_corners,
    "omni": cmd_omni,
    "layout": cmd_layout,
    "verify": cmd_verify,
    "seams": cmd_seams,
    "show": cmd_show,
    "preview": cmd_preview,
    "layouts": cmd_layouts,
}


def main(argv):
    if not argv or argv[0] in ("-h", "--help", "help"):
        print(__doc__.strip().split("\n\n")[1])
        return 0
    command = COMMANDS.get(argv[0])
    if command is None:
        print(f"  unknown command '{argv[0]}'; try: {', '.join(COMMANDS)}")
        return 1
    try:
        return command(argv[1:])
    except (ValueError, FileNotFoundError) as error:
        print(f"  {error}")
        return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
