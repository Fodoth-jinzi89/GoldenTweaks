"""对比 PP 与 PE 的 frame 四象限角部，找出「角块」到底在哪一侧。

PP 的 _r 已被验收为逐像素正确，它的角块切法就是答案。这里把两张 frame 的左上象限完整打印，
并标出 4x4 / 6x6 两种切法各自取到的内容，用来判断 PE 应该取外角还是内角。
"""
import sys

from PIL import Image

SIZE = 32
QUAD = 16


def dump(pixels, x0, y0, w, h, label):
    print(f"    {label}:")
    for y in range(y0, y0 + h):
        row = ""
        for x in range(x0, x0 + w):
            alpha = pixels[x, y][3]
            if alpha < 16:
                row += " "
            elif alpha < 250:
                row += "+"
            else:
                row += "#"
        print(f"      {row}")


def main(argv):
    for path in argv:
        frame = Image.open(path).convert("RGBA")
        pixels = frame.load()
        print(f"  {path}")

        # 左上象限的四个角：外角 (0,0)、内角 (12,12)
        dump(pixels, 0, 0, 8, 8, "左上象限 外角 8x8")
        dump(pixels, QUAD - 8, QUAD - 8, 8, 8, "左上象限 内角 8x8")

        # 右上象限的内角
        dump(pixels, QUAD, QUAD - 8, 8, 8, "右上象限 内角 8x8")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
