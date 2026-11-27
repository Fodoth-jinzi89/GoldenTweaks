"""Locate the inner-corner mark inside each quadrant of a corner sheet.

`shell_pe_r.png` is a 32x32 tile holding four quadrants, each carrying one corner mark. To composite
those marks into a connected sheet, three things have to be known exactly rather than assumed: which
corner of the quadrant the mark sits in, how large it is, and what colour it is. This prints all three
so the converter can paste only the mark instead of a whole quadrant — the quadrants carry interior
texture whose stripes would not line up with the destination cell.

    python script/multiblock/locate_corner_marks.py arts/multiblock/shell_pe_r.png
"""
import sys

from PIL import Image


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def main(argv):
    if not argv:
        print("usage: locate_corner_marks.py <corner_sheet.png>")
        return 1

    image = Image.open(argv[0]).convert("RGBA")
    width, height = image.size
    pixels = image.load()

    tones = [luminance(pixels[x, y]) for y in range(height) for x in range(width)
             if pixels[x, y][3] > 0]
    if not tones:
        print("  fully transparent")
        return 1
    threshold = (min(tones) + max(tones)) // 2

    quadrants = {
        "top-left": (0, 0, width // 2, height // 2),
        "top-right": (width // 2, 0, width, height // 2),
        "bottom-left": (0, height // 2, width // 2, height),
        "bottom-right": (width // 2, height // 2, width, height),
    }

    print(f"  {argv[0]}: {width}x{height}, dark threshold {threshold}")
    for name, (x0, y0, x1, y1) in quadrants.items():
        # The mark is the darker tone; the interior is the lighter one.
        xs, ys = [], []
        colours = {}
        for y in range(y0, y1):
            for x in range(x0, x1):
                r, g, b, a = pixels[x, y]
                if a > 0 and luminance((r, g, b)) < threshold:
                    xs.append(x)
                    ys.append(y)
                    colours[(r, g, b)] = colours.get((r, g, b), 0) + 1
        if not xs:
            print(f"  {name}: no dark mark found")
            continue
        left, right = min(xs) - x0, max(xs) - x0
        top, bottom = min(ys) - y0, max(ys) - y0
        size = f"{right - left + 1}x{bottom - top + 1}"

        # Which corner of the quadrant the mark is anchored to.
        qw, qh = x1 - x0, y1 - y0
        horizontally = "left" if left == 0 else "right" if right == qw - 1 else "middle"
        vertically = "top" if top == 0 else "bottom" if bottom == qh - 1 else "middle"

        dominant = max(colours, key=colours.get)
        print(f"  {name:13s} mark {size:7s} at ({left},{top})..({right},{bottom}) "
              f"anchored {vertically}-{horizontally}  colour {dominant}")

    # Interior tone, for comparison against the connected sheet's own interior.
    interior = {}
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            if a > 0 and luminance((r, g, b)) >= threshold:
                interior[(r, g, b)] = interior.get((r, g, b), 0) + 1
    if interior:
        common = sorted(interior.items(), key=lambda kv: -kv[1])[:3]
        print(f"  interior tones: {common}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
