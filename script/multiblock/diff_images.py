"""比较两张同尺寸贴图，报告 alpha 形状差异与完整 RGBA 差异。

形状一致但颜色不同时，说明位置对了、取错的只是取样来源；两者都一致才算完全复现。
"""
import os
import sys

from PIL import Image


def main(argv):
    a = Image.open(argv[0]).convert("RGBA")
    b = Image.open(argv[1]).convert("RGBA")
    if a.size != b.size:
        print(f"  尺寸不同 {a.size} vs {b.size}")
        return 1

    alpha_diff = 0
    rgba_diff = 0
    samples = []
    for y in range(a.size[1]):
        for x in range(a.size[0]):
            pa, pb = a.getpixel((x, y)), b.getpixel((x, y))
            if (pa[3] > 16) != (pb[3] > 16):
                alpha_diff += 1
            if pa != pb:
                rgba_diff += 1
                if len(samples) < 8 and (pa[3] > 16 or pb[3] > 16):
                    samples.append((x, y, pa, pb))

    print(f"  {os.path.basename(argv[0])} vs {os.path.basename(argv[1])}")
    print(f"  alpha 形状差异: {alpha_diff}")
    print(f"  完整 RGBA 差异: {rgba_diff}")
    for x, y, pa, pb in samples:
        print(f"    ({x:2d},{y:2d}) 正确={pa} 当前={pb}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
