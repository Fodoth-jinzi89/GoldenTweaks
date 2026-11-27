"""追查参考 _ct 的每一格来自 frame 的哪个 16x16 区域。

参考 _ct 是 128x128，正合 8x8 个 16px 格；frame 是 32x32，四个象限也正好 16x16。
对每一格，把 frame 上所有 16x16 的取窗（含旋转与镜像）都试一遍，取差异最小的那个。
输出直接就是排布表，不用再猜。
"""
import sys
from itertools import product

from PIL import Image

FRAME = 32
SUB = 16
SIZE = 128
GRID = SIZE // SUB

TRANSFORMS = {
    "": None,
    "R90": Image.Transpose.ROTATE_90,
    "R180": Image.Transpose.ROTATE_180,
    "R270": Image.Transpose.ROTATE_270,
    "M": Image.Transpose.FLIP_LEFT_RIGHT,
    "MT": Image.Transpose.FLIP_TOP_BOTTOM,
}


def subframes(frame):
    """frame 上每一个 16x16 取窗，带变换名，作为候选来源。"""
    out = {}
    for ox in range(FRAME - SUB + 1):
        for oy in range(FRAME - SUB + 1):
            window = frame.crop((ox, oy, ox + SUB, oy + SUB))
            for name, op in TRANSFORMS.items():
                piece = window if op is None else window.transpose(op)
                out[(ox, oy, name)] = piece
    return out


def diff(a, b):
    return sum(1 for y in range(SUB) for x in range(SUB)
               if a.getpixel((x, y)) != b.getpixel((x, y)))


def main(argv):
    ct = Image.open(argv[0]).convert("RGBA")
    frame = Image.open(argv[1]).convert("RGBA")
    if ct.size != (SIZE, SIZE) or frame.size != (FRAME, FRAME):
        print(f"  尺寸不符: ct {ct.size}, frame {frame.size}")
        return 1

    candidates = subframes(frame)
    label = {key: f"{key[0]:2d},{key[1]:2d}{key[2] or '  '}" for key in candidates}
    symbol = {}

    print(f"  {argv[0]}: {GRID}x{GRID} 个 {SUB}px 格，逐个找来源")
    grid = []
    for gy in range(GRID):
        row = []
        for gx in range(GRID):
            cell = ct.crop((gx * SUB, gy * SUB, (gx + 1) * SUB, (gy + 1) * SUB))
            blank = all(cell.getpixel((x, y))[3] < 16 for y in range(SUB) for x in range(SUB))
            if blank:
                row.append(("blank", 0))
                continue
            best, best_diff = None, SUB * SUB + 1
            for key, piece in candidates.items():
                d = diff(piece, cell)
                if d < best_diff:
                    best, best_diff = key, d
            row.append((best, best_diff))
        grid.append(row)

    # 给每个不同来源一个字母，便于阅读。
    letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    for row in grid:
        for source, _ in row:
            if source == "blank":
                continue
            if source not in symbol:
                symbol[source] = letters[len(symbol) % len(letters)] if len(symbol) < 26 \
                    else f"z{len(symbol)}"

    print()
    for gy in range(GRID):
        line = ""
        for gx in range(GRID):
            source, d = grid[gy][gx]
            line += "  .  " if source == "blank" else f"  {symbol[source]}  "
        print("   " + line)

    print()
    print("  来源图例（frame 上的取窗 x,y 与变换）:")
    for source, letter in sorted(symbol.items(), key=lambda kv: kv[1]):
        exact = sum(1 for row in grid for s, d in row if s == source and d == 0)
        total = sum(1 for row in grid for s, d in row if s == source)
        print(f"    {letter}: {label[source]}   用于 {total} 格，其中 {exact} 格完全一致")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
