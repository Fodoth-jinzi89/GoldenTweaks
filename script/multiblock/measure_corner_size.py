"""量出模板的「角切分点」——九宫格该在哪里切。

不能只看「最外几行铺满」：PP 的最外 2 行是满的，但它的角在 6px 处才结束。
真正决定切分点的是：从边缘向内，到哪一行开始出现横向的空白（mid-row 不再是实心带），
以及角块自身的尺寸。

另一种更可靠的口径：角块是「从 (0,0) 出发、保持实心」的最大正方形边长。
这里把两种口径都量出来对照。
"""
import sys

from PIL import Image

SIZE = 32


def solid_run_from_edge(frame):
    """从最外一行向内，连续有多少行是「整行不透明」的。"""
    pixels = frame.load()
    count = 0
    for y in range(SIZE):
        if all(pixels[x, y][3] > 16 for x in range(SIZE)):
            count += 1
        else:
            break
    return count


def corner_square(frame):
    """左上角实心正方形的边长：从 (0,0) 向右/向下能保持「该行该列都填满」的范围。"""
    pixels = frame.load()
    # 找第一行右侧变空的位置
    row_full = []
    for y in range(SIZE):
        filled = [x for x in range(SIZE) if pixels[x, y][3] > 16]
        row_full.append(max(filled) + 1 if filled else 0)

    # 角块的高度 = 连续「宽度 >= SIZE」的行数
    height = 0
    for y in range(SIZE):
        if row_full[y] >= SIZE:
            height += 1
        else:
            break

    # 角块的宽度 = 连续「高度 >= SIZE」的列数
    col_full = []
    for x in range(SIZE):
        filled = [y for y in range(SIZE) if pixels[x, y][3] > 16]
        col_full.append(max(filled) + 1 if filled else 0)

    width = 0
    for x in range(SIZE):
        if col_full[x] >= SIZE:
            width += 1
        else:
            break

    return width, height


def first_gap(frame):
    """从边缘向内，第一条「中间断开」的行距边缘多远。"""
    pixels = frame.load()
    for y in range(SIZE):
        middle = [pixels[x, y][3] for x in range(SIZE // 3, SIZE - SIZE // 3)]
        if any(alpha < 16 for alpha in middle):
            return y
    return SIZE


def main(argv):
    for path in argv:
        frame = Image.open(path).convert("RGBA")
        width, height = corner_square(frame)
        print(f"  {path}")
        print(f"    整行实心行数: {solid_run_from_edge(frame)}")
        print(f"    角块实心方形: {width}x{height}")
        print(f"    中部首次出现空隙的行: {first_gap(frame)}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
