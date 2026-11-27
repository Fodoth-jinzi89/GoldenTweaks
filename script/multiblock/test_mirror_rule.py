"""精确验证 C/H 两格的重建规则：取 frame 窗口的中间 8 列，镜像拼成 16 列。

frame 的 [8,0] 窗口在 y=2 行是
    255 255 255 255  12 9 13 25  25 13 9 12  255 255 255 255
参考 _ct 的 C 格在 y=2 行是
    25 13 9 12  12 9 13 25  25 13 9 12  12 9 13 25
也就是：取中间 8 列（下标 4..11 = 12 9 13 25 25 13 9 12），再按 4 列一组镜像成 16 列。
等价于把 [4..11] 当作周期 8 的图样，前后各镜像延拓。

y=0/1 两行则是纯色，取自 frame 该行最左像素。
"""
import sys

from PIL import Image

SUB = 16


def mirror_middle(window):
    """取中间 8 列，镜像拼成 16 列。

    中间 8 列是 [4..11]。目标 16 列里，前 4 列是它的前 4 列反转（即 [7..4]），
    再 [4..11]，再 [4..7]。这样得到 4 + 8 + 4 = 16。
    """
    out = Image.new("RGBA", window.size, (0, 0, 0, 0))
    for y in range(SUB):
        row = [window.getpixel((x, y)) for x in range(SUB)]
        middle = row[4:12]
        rebuilt = middle[3::-1] + middle + middle[:4]
        for x, pixel in enumerate(rebuilt):
            out.putpixel((x, y), pixel)
    return out


def main(argv):
    ct = Image.open(argv[0]).convert("RGBA")
    frame = Image.open(argv[1]).convert("RGBA")

    for (gx, gy, fx, fy) in ((3, 0, 8, 0), (3, 5, 8, 16)):
        want = ct.crop((gx * SUB, gy * SUB, (gx + 1) * SUB, (gy + 1) * SUB))
        window = frame.crop((fx, fy, fx + SUB, fy + SUB))
        got = mirror_middle(window)

        diff = sum(1 for y in range(SUB) for x in range(SUB)
                   if got.getpixel((x, y)) != want.getpixel((x, y)))
        print(f"  格 ({gx},{gy}) 由 frame [{fx},{fy}] 镜像中间 8 列: {diff}/256 不同")
        if diff:
            for y in range(SUB):
                marks = "".join("#" if got.getpixel((x, y)) != want.getpixel((x, y)) else "."
                                for x in range(SUB))
                print(f"      {y:2d} {marks}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
