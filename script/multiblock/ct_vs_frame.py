"""对照参考 _ct 与 frame，还原「每格保留 frame 的哪些像素」。

_ct 是叠在 base 上的图层，所以它必须只保留 frame 的边框与装饰，其余透明。把两者逐像素并排打印，
就能看出保留规则：保留处为 #，被抹掉处为空格。
"""
import os
import sys

from PIL import Image

CELL = 32


def main(argv):
    if len(argv) < 3:
        print("usage: ct_vs_frame.py <ct.png> <frame.png> <index>")
        return 1
    ct = Image.open(argv[0]).convert("RGBA")
    frame = Image.open(argv[1]).convert("RGBA")
    index = int(argv[2])
    cell = ct.size[0] // 4
    column, row = index % 4, index // 4
    got = ct.crop((column * cell, row * cell,
                   (column + 1) * cell, (row + 1) * cell))

    print(f"    ct 图层 ({os.path.basename(argv[0])} cell {index})     |     frame 原图")
    for y in range(cell):
        left = []
        right = []
        for x in range(cell):
            pixel = got.getpixel((x, y))
            left.append(" " if pixel[3] < 16 else "#")
            fp = frame.getpixel((x, y))
            right.append(" " if fp[3] < 16 else ("#" if max(fp[:3]) < 200 else "."))
        print(f"  {y:2d} {''.join(left)}   |   {''.join(right)}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
