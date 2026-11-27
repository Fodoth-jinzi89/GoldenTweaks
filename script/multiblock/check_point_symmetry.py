"""检查一张 frame 是否关于图像中心点对称（旋转 180 度后与自身相同）。

这决定了 _r 的构造是否需要额外旋转：如果 frame 本身点对称，那么「切角后旋转 180」与
「切角后不旋转」得到的图完全一样，两种写法都能通过验收；若不对称，两者结果不同，
必须选对的那一种。
"""
import sys

from PIL import Image

SIZE = 32


def main(argv):
    for path in argv:
        frame = Image.open(path).convert("RGBA")
        width, height = frame.size
        diff = 0
        for y in range(height):
            for x in range(width):
                mirror = frame.getpixel((width - 1 - x, height - 1 - y))
                if frame.getpixel((x, y)) != mirror:
                    diff += 1
        total = width * height
        print(f"  {path}: 点对称差异 {diff}/{total}"
              f"  ({'对称' if diff == 0 else '不对称'})")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
