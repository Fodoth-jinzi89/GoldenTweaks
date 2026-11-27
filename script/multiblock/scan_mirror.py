"""扫描镜像取样参数，给不同边框厚度（角尺寸）的模板各找一个自洽设置。

已验证的规则是：取窗口中间 2q 个像素为 band，输出 = band 前 q 个反转 + band + band 前 q 个，
长度不足 4q 时继续按 band 平铺。PP 用 q=2（band 宽 4？）不对 —— PP 实测 q 使 4q=16，
所以 PP 是 q=4、band 宽 8。这里对 PE 扫 q，看哪个值能复现参考 _ct。
"""
import sys

from PIL import Image

SUB = 16


def build(piece, quarter, axis):
    width, height = piece.size
    length = width if axis == "x" else height
    out = Image.new("RGBA", piece.size, (0, 0, 0, 0))

    for index in range(height if axis == "x" else width):
        if axis == "x":
            line = [piece.getpixel((x, index)) for x in range(width)]
        else:
            line = [piece.getpixel((index, y)) for y in range(height)]

        band = line[quarter:quarter + 2 * quarter]
        if len(band) < 2 * quarter:
            return None
        head = band[:quarter][::-1]
        # 左右各按 band 平铺补满
        left = []
        while len(left) + len(band) + quarter < length:
            left = head + left
        row = (left + band + band * 4)[:length]

        for position in range(length):
            if axis == "x":
                out.putpixel((position, index), row[position])
            else:
                out.putpixel((index, position), row[position])
    return out


def main(argv):
    tests = [
        ("PP/C", argv[0], argv[1], (3, 0), (8, 0), "x"),
        ("PP/D", argv[0], argv[1], (0, 1), (0, 8), "y"),
        ("PE/C", argv[2], argv[3], (3, 0), (8, 0), "x"),
        ("PE/D", argv[2], argv[3], (0, 1), (0, 8), "y"),
    ]
    for label, ct_path, frame_path, (gx, gy), (fx, fy), axis in tests:
        ct = Image.open(ct_path).convert("RGBA")
        frame = Image.open(frame_path).convert("RGBA")
        want = ct.crop((gx * SUB, gy * SUB, (gx + 1) * SUB, (gy + 1) * SUB))
        window = frame.crop((fx, fy, fx + SUB, fy + SUB))

        best = None
        for quarter in range(1, 6):
            got = build(window, quarter, axis)
            if got is None:
                continue
            diff = sum(1 for y in range(SUB) for x in range(SUB)
                       if got.getpixel((x, y)) != want.getpixel((x, y)))
            mark = "  <==" if best is None or diff < best[0] else ""
            print(f"    {label} quarter={quarter}: {diff}/256{mark}")
            if best is None or diff < best[0]:
                best = (diff, quarter)
        print(f"  {label}: 最佳 quarter={best[1]} ({best[0]}/256)")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
