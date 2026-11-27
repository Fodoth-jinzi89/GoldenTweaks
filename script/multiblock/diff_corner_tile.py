"""把正确的角标图与当前生成的结果逐像素比对，并打印两者的形状。

只看 alpha：角标图关心的是「哪些位置有标记」，颜色另说。
"""
import os
import sys

from PIL import Image

SIZE = 32


def dump(path):
    image = Image.open(path).convert("RGBA")
    rows = []
    for y in range(SIZE):
        row = ""
        for x in range(SIZE):
            alpha = image.getpixel((x, y))[3]
            row += " " if alpha < 16 else "#"
        rows.append(row)
    return image, rows


def main(argv):
    good_path, mine_path = argv[0], argv[1]
    good, good_rows = dump(good_path)
    mine, mine_rows = dump(mine_path)

    diff = 0
    for y in range(SIZE):
        for x in range(SIZE):
            if good.getpixel((x, y))[3] > 16 != (mine.getpixel((x, y))[3] > 16):
                diff += 1
    alpha_diff = 0
    for y in range(SIZE):
        for x in range(SIZE):
            a = good.getpixel((x, y))
            b = mine.getpixel((x, y))
            if (a[3] > 16) != (b[3] > 16):
                alpha_diff += 1

    print(f"  正确 {os.path.basename(good_path)}  |  当前 {os.path.basename(mine_path)}")
    for y in range(SIZE):
        mark = "" if good_rows[y] == mine_rows[y] else "  <-- 不同"
        print(f"  {y:2d} {good_rows[y]} | {mine_rows[y]}{mark}")
    print(f"  形状不同像素: {alpha_diff}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
