"""Report the four quadrant corner shapes of an inner-corner sheet.

`shell_pe_r.png` is one tile with a dark inset corner in each quadrant. Before using it, check that
the quadrants really are four *different* corners (or deliberately the same): a converter that rotates
one quadrant into the other three produces wrong art silently if they were hand-drawn separately.

    python script/multiblock/inspect_corner_quadrants.py arts/multiblock/shell_pe_r.png
"""
import sys

from PIL import Image


def main(argv):
    if not argv:
        print("usage: inspect_corner_quadrants.py <image.png>")
        return 1

    image = Image.open(argv[0]).convert("RGBA")
    width, height = image.size
    half_w, half_h = width // 2, height // 2

    quadrants = {
        "top-left": (0, 0, half_w, half_h),
        "top-right": (half_w, 0, width, half_h),
        "bottom-left": (0, half_h, half_w, height),
        "bottom-right": (half_w, half_h, width, height),
    }

    crops = {}
    for name, box in quadrants.items():
        crop = image.crop(box)
        crops[name] = crop
        print(f"  {name}: {crop.size[0]}x{crop.size[1]}")

    print()
    print("  quadrant pairs identical?")
    names = list(crops)
    for i in range(len(names)):
        for j in range(i + 1, len(names)):
            same = list(crops[names[i]].getdata()) == list(crops[names[j]].getdata())
            print(f"    {names[i]:13s} vs {names[j]:13s}: {'IDENTICAL' if same else 'different'}")

    print()
    print("  does each quadrant equal another quadrant rotated?")
    for i in range(len(names)):
        for j in range(i + 1, len(names)):
            a, b = crops[names[i]], crops[names[j]]
            for turns in (1, 2, 3):
                rotated = a.rotate(90 * turns, expand=False)
                if list(rotated.getdata()) == list(b.getdata()):
                    print(f"    {names[j]} == {names[i]} rotated {90 * turns}deg")
                    break
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
