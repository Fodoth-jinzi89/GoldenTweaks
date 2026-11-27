"""量出一张边框模板的「角尺寸」和「带内周期」，用来给镜像取样定位。

角尺寸决定了边框实心带到装饰带的过渡位置；带内周期决定了镜像该取多宽的取样带。
两者都从像素里量，不靠猜。
"""
import sys
from itertools import groupby

from PIL import Image

SIZE = 32


def alpha_rows(pixels, size=SIZE):
    return [[pixels[x, y][3] for x in range(size)] for y in range(size)]


def opaque_profile(frame):
    """逐行与逐列的不透明像素数，用来找边框厚度。"""
    pixels = frame.load()
    rows = [sum(1 for x in range(SIZE) if pixels[x, y][3] > 16) for y in range(SIZE)]
    columns = [sum(1 for y in range(SIZE) if pixels[x, y][3] > 16) for x in range(SIZE)]
    return rows, columns


def find_period(values):
    """在一条 alpha 序列里找最小重复周期（只考虑可打印的部分）。"""
    length = len(values)
    for period in range(1, length // 2 + 1):
        if all(values[i] == values[i + period] for i in range(length - period)):
            return period
    return None


def main(argv):
    for path in argv:
        frame = Image.open(path).convert("RGBA")
        print(f"  {path}")
        rows, columns = opaque_profile(frame)
        print(f"    行不透明数: {rows}")
        print(f"    列不透明数: {columns}")

        # 最外一行的 alpha 序列，用来判断实心带有多厚。
        pixels = frame.load()
        print("    第 0 行 alpha:", [pixels[x, 0][3] for x in range(SIZE)])
        print("    第 0 列 alpha:", [pixels[0, y][3] for y in range(SIZE)])

        # 中部窗口的 alpha 行，用来看周期。
        for y in range(2, 6):
            line = [pixels[x, y][3] for x in range(8, 24)]
            print(f"    y={y} [8..23] alpha: {line}  周期={find_period(line)}")

        for y in range(16):
            line = [pixels[x, y][3] for x in range(SIZE)]
            non_opaque = sum(1 for v in line if 0 < v < 255)
            print(f"    y={y:2d}: 半透明 {non_opaque:2d}, 不透明 {sum(1 for v in line if v == 255):2d}")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
