"""检查角标图的四个象限各自裁出的「标记」是否相同，并打印它们的落点。

若四块内容不同，那么「贴反」就是取错了象限的标记，而不是位置算错——两者的修法完全不同。
"""
import os
import sys

from PIL import Image

SIZE = 32
HALF = 16

QUADRANTS = {
    "topLeft": (0, 0, HALF, HALF),
    "topRight": (HALF, 0, SIZE, HALF),
    "bottomLeft": (0, HALF, HALF, SIZE),
    "bottomRight": (HALF, HALF, SIZE, SIZE),
}


def main(argv):
    sheet = Image.open(argv[0]).convert("RGBA")
    pixels = sheet.load()

    print(f"  {os.path.basename(argv[0])}")
    marks = {}
    for name, (x0, y0, x1, y1) in QUADRANTS.items():
        found = [(x, y) for y in range(y0, y1) for x in range(x0, x1)
                 if pixels[x, y][3] > 16]
        if not found:
            print(f"    {name}: 空")
            continue
        left = min(x for x, _ in found)
        top = min(y for _, y in found)
        right = max(x for x, _ in found) + 1
        bottom = max(y for _, y in found) + 1
        mark = sheet.crop((left, top, right, bottom))
        marks[name] = mark

        print(f"    {name}: bbox=({left},{top})-({right},{bottom})  "
              f"尺寸 {mark.width}x{mark.height}")
        for y in range(mark.height):
            print("      " + str([mark.getpixel((x, y)) for x in range(mark.width)]))

    names = list(marks)
    print()
    for i in range(len(names)):
        for j in range(i + 1, len(names)):
            a, b = marks[names[i]], marks[names[j]]
            if a.size != b.size:
                print(f"    {names[i]} vs {names[j]}: 尺寸不同")
                continue
            diff = sum(1 for y in range(a.size[1]) for x in range(a.size[0])
                       if a.getpixel((x, y)) != b.getpixel((x, y)))
            print(f"    {names[i]} vs {names[j]}: {diff} px 不同")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
