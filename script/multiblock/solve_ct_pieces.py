"""把参考 _ct 的每个 16px 小格精确解出来：穷举 frame 上所有取窗与所有二面体变换。

之前只试了 6 种变换，A/B/D/E/F/G 都能逐像素吻合，C/H 不能。这里补齐 8 种（含两条对角翻转），
并把 base、r 也纳入搜索，同时对每个小格输出「最接近的来源 + 差异像素数」，用来判断是没有精确解
（需要另建规则）还是只是取窗位置没找对。
"""
import sys
from itertools import product

from PIL import Image

SUB = 16
GRID = 8
SIZE = GRID * SUB

TRANSFORMS = {
    "id": None,
    "r90": Image.Transpose.ROTATE_90,
    "r180": Image.Transpose.ROTATE_180,
    "r270": Image.Transpose.ROTATE_270,
    "fliplr": Image.Transpose.FLIP_LEFT_RIGHT,
    "fliptb": Image.Transpose.FLIP_TOP_BOTTOM,
    "transpose": Image.Transpose.TRANSPOSE,
    "transverse": Image.Transpose.TRANSVERSE,
}


def candidates(source, name):
    """source 上每个 16x16 取窗，套每一种变换，返回 {(name,x,y,tf): bytes}。"""
    out = {}
    width, height = source.size
    for x in range(width - SUB + 1):
        for y in range(height - SUB + 1):
            window = source.crop((x, y, x + SUB, y + SUB))
            for key, op in TRANSFORMS.items():
                piece = window if op is None else window.transpose(op)
                out[(name, x, y, key)] = piece.tobytes()
    return out


def main(argv):
    ct = Image.open(argv[0]).convert("RGBA")
    sources = []
    for path in argv[1:]:
        sources.append((path.split("\\")[-1].split("/")[-1].rsplit(".", 1)[0],
                        Image.open(path).convert("RGBA")))

    pool = {}
    for name, image in sources:
        pool.update(candidates(image, name))
    print(f"  候选数 {len(pool)}（{len(sources)} 张源图 x 取窗 x 8 变换）")

    cell_bytes = {}
    for gy in range(GRID):
        for gx in range(GRID):
            tile = ct.crop((gx * SUB, gy * SUB, (gx + 1) * SUB, (gy + 1) * SUB))
            cell_bytes[(gx, gy)] = tile

    # 把小格按内容分组，相同的只解一次。
    signature = {}
    for key, tile in cell_bytes.items():
        signature.setdefault(tile.tobytes(), []).append(key)
    print(f"  {GRID}x{GRID} 格中共 {len(signature)} 种不同内容")
    print()

    legend = {}
    for data, positions in signature.items():
        blank = all(data[i] == 0 for i in range(3, len(data), 4))
        if blank:
            legend["blank"] = ("(透明)", positions, 0)
            continue
        best = None
        for source_key, source_data in pool.items():
            if source_data == data:
                best = (source_key, 0)
                break
            # 只在没有精确解时，统计差异需要逐像素，太慢；先跳过。
        if best:
            legend[str(best[0])] = (best[0], positions, 0)
        else:
            legend[f"unknown{len(legend)}"] = (None, positions, None)

    # 精确解没找到的，逐个统计最小差异（只对这些做慢比较）。
    for key, (source_key, positions, _) in list(legend.items()):
        if source_key is not None:
            continue
        sample = cell_bytes[positions[0]]
        sample_bytes = sample.tobytes()
        best = None
        for candidate_key, candidate_data in pool.items():
            diff = 0
            for i in range(0, len(sample_bytes), 4):
                if sample_bytes[i:i + 4] != candidate_data[i:i + 4]:
                    diff += 1
                    if best is not None and diff >= best[1]:
                        break
            if best is None or diff < best[1]:
                best = (candidate_key, diff)
                if diff == 0:
                    break
        legend[key] = (best[0], positions, best[1])

    # 打印排布表。
    symbol_of = {}
    letters = iter("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
    for key, (source_key, positions, diff) in legend.items():
        symbol_of[key] = "." if source_key == "(透明)" else next(letters)

    print("  排布（同符号 = 内容完全相同）:")
    for gy in range(GRID):
        line = ""
        for gx in range(GRID):
            data = cell_bytes[(gx, gy)].tobytes()
            for key, (source_key, positions, diff) in legend.items():
                if positions[0] == (gx, gy):
                    line += f" {symbol_of[key]} "
                    break
        print("   " + line)

    print()
    print("  来源:")
    for key, (source_key, positions, diff) in legend.items():
        letter = symbol_of[key]
        where = ", ".join(f"({x},{y})" for x, y in positions)
        if source_key is None:
            print(f"    {letter}: 未解出 用于 {where}")
        elif source_key == "(透明)":
            print(f"    {letter}: 全透明 用于 {where}")
        else:
            name, x, y, tf = source_key
            exact = "精确" if diff == 0 else f"差 {diff}px"
            print(f"    {letter}: {name} [{x},{y}] {tf}  {exact}  用于 {where}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
