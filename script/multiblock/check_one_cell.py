"""Check one generated cell against the hand-made sheet, in isolation.

When a whole-sheet diff stays flat it is usually because the generator does not take the branch that
was verified by hand. This drives a single cell and prints both its corner pixels and whether the
cell equals a plain tiling of the centre piece, so the actual code path is visible rather than assumed.

    python script/multiblock/check_one_cell.py <cx> <cy>
"""
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_ct_from_frame import CUTS, SLOTS, build_cell, rectangle_states  # noqa: E402

FOLDER = os.path.join("arts", "multiblock", "ctm")


def main(argv):
    cx = int(argv[0]) if argv else 2
    cy = int(argv[1]) if len(argv) > 1 else 1
    index = cy * 4 + cx

    frame = Image.open(os.path.join(FOLDER, "template_frame.png")).convert("RGBA")
    reference = Image.open(os.path.join(FOLDER, "template_ct.png")).convert("RGBA")

    pieces = {}
    slot = 0
    for yi in range(3):
        for xi in range(3):
            pieces[SLOTS[slot]] = frame.crop((CUTS[xi], CUTS[yi],
                                              CUTS[xi + 1], CUTS[yi + 1]))
            slot += 1

    states = rectangle_states()
    state = states[index]
    print(f"  cell ({cx},{cy}) = index {index}, state {state}")

    generated = build_cell(pieces, state)
    expected = reference.crop((cx * 32, cy * 32, (cx + 1) * 32, (cy + 1) * 32))

    centre = pieces["e"]
    phase = 12 % centre.size[0]
    pure = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    for y in range(32):
        for x in range(32):
            pure.putpixel((x, y), centre.getpixel(((x + phase) % 8, (y + phase) % 8)))

    for name, image in (("generated", generated), ("expected", expected), ("pure tiling", pure)):
        row = " ".join(f"{image.getpixel((x, 0))[0]:02x}" for x in range(0, 32, 4))
        print(f"  {name:12s} row 0: {row}")

    diff = sum(1 for y in range(32) for x in range(32)
               if generated.getpixel((x, y)) != expected.getpixel((x, y)))
    print(f"  generated vs expected: {diff}/1024 differ")

    diff_pure = sum(1 for y in range(32) for x in range(32)
                    if pure.getpixel((x, y)) != expected.getpixel((x, y)))
    print(f"  pure tiling vs expected: {diff_pure}/1024 differ")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
