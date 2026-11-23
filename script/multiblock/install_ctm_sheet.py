"""Install the hand-drawn connected-texture sheet for polypropylene.

The sheet is authored at 128x128: a 4x4 grid of 32px tiles, one per mask, matching Create's
`RECTANGLE` layout. It replaces the sheet that was packed from the 47 OptiFine tiles, which could only
describe seven of the sixteen masks and got the rest by nearest-neighbour guesswork.

The source resolution does not matter to Create — a CT sheet is sampled by relative position inside
each grid cell — so the 32px tiles and the 16px base texture coexist without any conversion.

    python script/multiblock/install_ctm_sheet.py
"""
import os
import shutil

from PIL import Image

ARTS = "arts/multiblock"
TEXTURES = "src/main/resources/assets/goldentweaks/textures/block"

TILE = 16
COLUMNS = 4
ROWS = 4


def main():
    source = os.path.join(ARTS, "shell_pp_ct.png")
    if not os.path.exists(source):
        raise SystemExit(f"missing {source}")

    image = Image.open(source).convert("RGBA")
    expected = COLUMNS * TILE * 2  # 4 cells of 32px
    if image.size != (expected, expected):
        raise SystemExit(f"expected {expected}x{expected}, got {image.size[0]}x{image.size[1]}")

    base = os.path.join(ARTS, "shell_pp.png")
    if os.path.exists(base):
        shutil.copyfile(base, os.path.join(TEXTURES, "shell_pp.png"))
        print("  shell_pp.png updated")

    target = os.path.join(TEXTURES, "shell_pp_ctm.png")
    image.save(target)
    print(f"  shell_pp_ctm.png  {image.size[0]}x{image.size[1]}  4x4 grid of 32px tiles")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
