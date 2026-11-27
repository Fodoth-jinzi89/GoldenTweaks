"""Compare one generated cell with the hand-made one, pixel by pixel.

When a generator is wrong at this scale it helps to see the first few bytes rather than the total count:
whether the content is shifted, tiled from a different phase, or simply absent each look different.

    python script/multiblock/debug_cell.py <cx> <cy>
"""
import os
import sys

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")
SCRATCH = os.environ.get("COMMANDCODE_SCRATCHPAD", ".")


def load(path):
    return Image.open(path).convert("RGBA")


def main(argv):
    cx = int(argv[0]) if argv else 2
    cy = int(argv[1]) if len(argv) > 1 else 1

    ref = load(os.path.join(FOLDER, "template_ct.png"))
    gen_path = os.path.join(SCRATCH, "gen2_ct.png")
    if not os.path.exists(gen_path):
        print(f"  {gen_path} missing, run gen_ct_from_frame.py first")
        return 1
    gen = load(gen_path)

    cell = ref.size[0] // 4
    ref_cell = ref.crop((cx * cell, cy * cell, (cx + 1) * cell, (cy + 1) * cell))
    gen_cell = gen.crop((cx * cell, cy * cell, (cx + 1) * cell, (cy + 1) * cell))

    print(f"  cell ({cx},{cy}) {cell}x{cell}: reference vs generated")
    print("      " + "  ".join(f"{x:02d}" for x in range(0, cell, 4)))
    for y in range(cell):
        ref_row = " ".join(f"{ref_cell.getpixel((x, y))[0]:02x}" for x in range(0, cell, 4))
        gen_row = " ".join(f"{gen_cell.getpixel((x, y))[0]:02x}" for x in range(0, cell, 4))
        mark = "" if ref_row == gen_row else "   <-"
        print(f"  {y:3d} ref {ref_row}{mark}")
        if ref_row != gen_row:
            print(f"      gen {gen_row}")

    # Does the generated cell equal the reference shifted by some offset?
    print()
    print("  shift search (generated vs reference, offset applied to generated):")
    results = []
    for dy in range(-8, 9):
        for dx in range(-8, 9):
            mismatch = 0
            counted = 0
            for y in range(cell):
                for x in range(cell):
                    sx, sy = x + dx, y + dy
                    if not (0 <= sx < cell and 0 <= sy < cell):
                        continue
                    counted += 1
                    if gen_cell.getpixel((sx, sy)) != ref_cell.getpixel((x, y)):
                        mismatch += 1
            if counted:
                results.append((mismatch / counted, dx, dy))
    results.sort()
    for ratio, dx, dy in results[:5]:
        print(f"    dx={dx:+d} dy={dy:+d}: {ratio:.1%} differ")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
