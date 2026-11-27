"""Test which source produced a known-good octo-directional cell.

The pipeline has several stages, and when the final sheet is wrong the useful question is which stage
diverged. This takes a cell that is known correct and compares it against every candidate source —
the base pattern, the 4x4 artwork, the 4x4 artwork composited over the base — so the answer is a
measurement rather than a guess.
"""
import os
import sys

from PIL import Image

CELL = 32


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def cell_of(sheet, index, grid):
    cell = sheet.size[0] // grid
    column, row = index % grid, index // grid
    return sheet.crop((column * cell, row * cell, (column + 1) * cell, (row + 1) * cell))


def diff(a, b):
    if a.size != b.size:
        return -1
    total = 0
    for y in range(a.size[1]):
        for x in range(a.size[0]):
            if a.getpixel((x, y)) != b.getpixel((x, y)):
                total += 1
    return total


def main(argv):
    if len(argv) < 2:
        print("usage: trace_cell.py <target_omni.png> <omni_index> [more sources...]")
        return 1
    target = Image.open(argv[0]).convert("RGBA")
    index = int(argv[1])
    want = cell_of(target, index, 8)

    print(f"  target: {os.path.basename(argv[0])} cell {index}")
    for row in range(CELL):
        chars = []
        for x in range(CELL):
            pixel = want.getpixel((x, row))
            if pixel[3] < 16:
                chars.append(" ")
            elif luminance(pixel) < 150:
                chars.append("#")
            elif luminance(pixel) < 200:
                chars.append(":")
            else:
                chars.append(".")
        print(f"  {row:2d} {''.join(chars)}")

    print()
    for path in argv[2:]:
        source = Image.open(path).convert("RGBA")
        base = os.path.basename(path)
        # Try it as a 4x4 sheet, a single cell, and a base pattern.
        for grid in (4, 1):
            if source.size[0] % grid:
                continue
            for src_index in range(grid * grid):
                piece = cell_of(source, src_index, grid)
                if piece.size != want.size:
                    continue
                d = diff(piece, want)
                if d >= 0 and d < want.size[0] * want.size[1] * 0.35:
                    print(f"  {base} [{grid}x{grid}] cell {src_index}: {d} px differ")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
