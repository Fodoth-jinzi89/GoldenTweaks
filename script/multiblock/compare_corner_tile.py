"""把 _r 的两种构造并列输出，判断哪一种与 PP 的已验收做法一致。

两种写法：
    A  切四角 -> 旋转 180（当前实现）
    B  切四角 -> 旋转 180 -> 再做一次中心对称（即水平+垂直翻转）

对 PP 而言 A 已通过验收，所以 A 是对的。若 PE 的形状必须用 B 才与 PP 同构，说明 PE 的角块
本身朝向就和 PP 相反，需要在切分时取另一侧的角。
"""
import sys

from PIL import Image

SIZE = 32


def print_tile(tile, label):
    print(f"    {label}:")
    for y in range(SIZE):
        row = ""
        for x in range(SIZE):
            alpha = tile.getpixel((x, y))[3]
            row += " " if alpha < 16 else "#"
        if "#" in row:
            print(f"      {row}")


def build(frame, border, extra_flip):
    width, height = frame.size
    cuts = (0, border, width - border, width)
    corners = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    for slot_x, slot_y in ((0, 0), (2, 0), (0, 2), (2, 2)):
        piece = frame.crop((cuts[slot_x], cuts[slot_y],
                            cuts[slot_x + 1], cuts[slot_y + 1]))
        corners.paste(piece, (cuts[slot_x], cuts[slot_y]))
    tile = corners.transpose(Image.Transpose.ROTATE_180)
    if extra_flip:
        tile = tile.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
        tile = tile.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
    return tile


def main(argv):
    path = argv[0]
    border = int(argv[1])
    frame = Image.open(path).convert("RGBA")

    a = build(frame, border, False)
    b = build(frame, border, True)
    print(f"  {path}  border={border}")
    print_tile(a, "A 切角+旋转180（当前）")
    print()
    print_tile(b, "B 切角+旋转180+中心对称")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
