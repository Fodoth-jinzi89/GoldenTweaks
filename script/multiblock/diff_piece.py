"""打印参考小格与 frame 取窗的逐像素差异，定位那约 100px 差在哪里。

只输出有差异的行，并标出差异像素的坐标范围，用来判断是整条边不同、还是零散的点不同。
"""
import sys

from PIL import Image

SUB = 16


def main(argv):
    ct = Image.open(argv[0]).convert("RGBA")
    frame = Image.open(argv[1]).convert("RGBA")
    letter = argv[2] if len(argv) > 2 else "C"
    gx, gy = int(argv[3]), int(argv[4])
    fx, fy = int(argv[5]), int(argv[6])

    tile = ct.crop((gx * SUB, gy * SUB, (gx + 1) * SUB, (gy + 1) * SUB))
    window = frame.crop((fx, fy, fx + SUB, fy + SUB))

    print(f"  参考格 ({gx},{gy})  vs  frame 取窗 [{fx},{fy}]")
    rows = []
    xs, ys = [], []
    for y in range(SUB):
        marks = ""
        for x in range(SUB):
            differs = tile.getpixel((x, y)) != window.getpixel((x, y))
            marks += "#" if differs else "."
            if differs:
                xs.append(x)
                ys.append(y)
        rows.append((y, marks))
    for y, marks in rows:
        print(f"  {y:2d} {marks}")
    if xs:
        print(f"  差异 {len(xs)} 个，x 范围 {min(xs)}..{max(xs)}，y 范围 {min(ys)}..{max(ys)}")
        print()
        print("  抽样：位置 | 参考 RGBA | frame RGBA")
        for index in range(0, min(len(xs), 10)):
            x, y = xs[index], ys[index]
            print(f"    ({x:2d},{y:2d}) | {tile.getpixel((x, y))} | {window.getpixel((x, y))}")
    else:
        print("  完全一致")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
