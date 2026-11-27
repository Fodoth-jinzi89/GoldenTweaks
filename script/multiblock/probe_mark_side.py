"""在已验收的 omni 成品里，量出「需要左上角标」的格子，标记实际落在格子的哪个角。

这是判断锚点方向的唯一办法：不看代码怎么写的，看正确的图里像素在哪。
做法：对每个需要某个角标的状态，把该格与它的源 ctm 格相减，差异像素所在的象限就是标记落点。
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from PIL import Image

from ctm import (CELL, OMNI_GRID, RECT_GRID, build_omni_table, corner_needs,
                 idx_omni, idx_rectangle, contexts)

QUADRANT_OF = (("topLeft", 0, 0), ("topRight", 1, 0),
               ("bottomLeft", 0, 1), ("bottomRight", 1, 1))


def main(argv):
    omni_path, ctm_path = argv[0], argv[1]
    omni = Image.open(omni_path).convert("RGBA")
    ctm = Image.open(ctm_path).convert("RGBA")
    cell = omni.size[0] // OMNI_GRID
    rcell = ctm.size[0] // RECT_GRID

    # 找每个「只缺一个角」的状态，看差异落在哪。
    seen = {}
    for ctx in contexts():
        needs = corner_needs(ctx)
        if len(needs) != 1:
            continue
        wanted = next(iter(needs))
        index = idx_omni(ctx)
        if index in seen:
            continue
        seen[index] = wanted

    print(f"  在 {os.path.basename(omni_path)} 中检查 {len(seen)} 个单角格")
    for index, wanted in sorted(seen.items()):
        source = idx_rectangle(contexts()[[idx_omni(c) for c in contexts()].index(index)])
        # 该格的 omni 内容
        oc = omni.crop(((index % OMNI_GRID) * cell, (index // OMNI_GRID) * cell,
                        (index % OMNI_GRID) * cell + cell,
                        (index // OMNI_GRID) * cell + cell))
        # 源 ctm 格
        sc = ctm.crop(((source % RECT_GRID) * rcell, (source // RECT_GRID) * rcell,
                       (source % RECT_GRID) * rcell + rcell,
                       (source // RECT_GRID) * rcell + rcell))

        # 差异像素落在哪个象限
        counts = {}
        for name, qx, qy in QUADRANT_OF:
            x0, y0 = qx * (cell // 2), qy * (cell // 2)
            n = 0
            for y in range(y0, y0 + cell // 2):
                for x in range(x0, x0 + cell // 2):
                    if oc.getpixel((x, y)) != sc.getpixel((x, y)):
                        n += 1
            counts[name] = n

        worst = max(counts, key=counts.get)
        verdict = "一致" if worst == wanted else "反向"
        print(f"    idx {index:2d} 需要 {wanted:11s} 差异集中在 {worst:11s}  {verdict}"
              f"   {counts}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
