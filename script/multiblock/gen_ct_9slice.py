"""用用户口述的九宫格公式生成 _ct，并与参考产物逐像素比对。

公式（用户原话）：

    frame 按 6/20/6 切成
        1 2 3
        4 5 6
        7 8 9

    ct 的四行依次是：
        第 0 行：A B A G G G G B      ->  1 2 3 4 5 6 7 8  （待定）
        ...

用户给出的完整排布（8 列 x 8 行，每格 4px）：

    ABAGGGGB
    EFE0000F
    EFE0000F
    EFE0000F
    EFE0000F
    CDCHHHHD
    ABAGGGGB
    CDCHHHHD

其中：
    AB = 上左、上右（frame 的左上、右上象限）
    CD = 下左、下右
    EF = def（中部三格整行）
    GH = deh（中部三格的上/下半行）
    0  = e 的平铺

下面用 8x8 格、每格 4px 实现，输出 32x32。
"""
import os
import sys

from PIL import Image

CUTS = (0, 6, 26, 32)


def nine(frame):
    return {(c, r): frame.crop((CUTS[c], CUTS[r], CUTS[c + 1], CUTS[r + 1]))
            for r in range(3) for c in range(3)}


def build(frame):
    """按 8x8 的排布拼出 32x32 的 _ct。"""
    p = nine(frame)
    a, b, c = p[(0, 0)], p[(1, 0)], p[(2, 0)]
    d, e, f = p[(0, 1)], p[(1, 1)], p[(2, 1)]
    g, h, i = p[(0, 2)], p[(1, 2)], p[(2, 2)]

    # 每格 4px？不 — 九宫格块尺寸不一（角 6px、边 20x6、中心 20x20）。
    # 用户描述的是「整图」排布，所以这里按 32x32 直接切分：8 列 x 8 行，每格 4px。
    # 但九宫格块不是 4px 的整数倍，说明用户描述里的字母指的是 8 个「区域」而非 8 个像素格。
    #
    # 改为直接按区域拼：每行高度按九宫格的行高（6/20/6），每列宽按列宽（6/20/6）。
    rows = [
        # (高度, [来源序列])
        (6, [(0, 0), (1, 0), (2, 0), (1, 0), (0, 0)]),
    ]
    raise NotImplementedError


def main(argv):
    print("尚未实现")
    return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
