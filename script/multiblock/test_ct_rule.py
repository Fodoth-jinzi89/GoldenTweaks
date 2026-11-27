"""精确重建 _ct：未连通的侧保留完整方块的外圈，连通的侧换成本方向的 base 外圈。

关键点：外圈不是整圈一起改，而是按侧分别处理。四条 4px 带各自独立，角上由后处理的轴覆盖，
这就是参考产物里角部像素归属的由来。
"""
import sys

from PIL import Image

CELL = 32


def state_of(index):
    """索引 -> 四边状态，取 Create 的 idx_rectangle 的反函数。

    idx_rectangle 里 x 编码左右、y 编码上下：
        x = 2 两侧都连 / 3 只连左 / 1 只连右 / 0 都不连
        y = 1 两侧都连 / 2 只连上 / 0 只连下 / 3 都不连
    实测参考图与之吻合（idx12 四边全断，idx6 四边全通）。
    """
    x, y = index % 4, index // 4
    return {
        "left": x in (2, 3),
        "right": x in (1, 2),
        "up": y in (1, 2),
        "down": y in (0, 1),
    }


def build(full, base, state, border):
    tile = full.copy()

    def lay(x, y):
        return base.getpixel((x % CELL, y % CELL))

    # 先竖后横，横读 tile，与 PP 的处理顺序一致。
    if state["up"]:
        for y in range(border):
            for x in range(CELL):
                tile.putpixel((x, y), lay(x, y))
    if state["down"]:
        for y in range(CELL - border, CELL):
            for x in range(CELL):
                tile.putpixel((x, y), lay(x, y))
    if state["left"]:
        for x in range(border):
            for y in range(CELL):
                tile.putpixel((x, y), lay(x, y))
    if state["right"]:
        for x in range(CELL - border, CELL):
            for y in range(CELL):
                tile.putpixel((x, y), lay(x, y))
    return tile


def main(argv):
    ref = Image.open(argv[0]).convert("RGBA")
    full = Image.open(argv[1]).convert("RGBA")
    base = Image.open(argv[2]).convert("RGBA")

    for border in (4, 6, 2):
        sheet = Image.new("RGBA", (CELL * 4, CELL * 4), (0, 0, 0, 0))
        for index in range(16):
            sheet.paste(build(full, base, state_of(index), border),
                        ((index % 4) * CELL, (index // 4) * CELL))
        diff = sum(1 for y in range(CELL * 4) for x in range(CELL * 4)
                   if sheet.getpixel((x, y)) != ref.getpixel((x, y)))
        print(f"  border={border}: {diff}/16384")
        if border == 4:
            for index in range(16):
                cx, cy = (index % 4) * CELL, (index // 4) * CELL
                d = sum(1 for y in range(CELL) for x in range(CELL)
                        if sheet.getpixel((cx + x, cy + y)) != ref.getpixel((cx + x, cy + y)))
                if d:
                    print(f"    idx {index:2d}: {d}/1024")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
