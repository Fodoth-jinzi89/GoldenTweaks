"""把基准 omni 的每一格拆开，逐块在各候选图上找出处，用来反推它的生成方式。

候选包括 frame、完整方块贴图、base，以及它们的所有 16x16 取窗与 8 种变换。
一格是 2x2 个 16x16 小块，逐块求解能看出是「整块搬运」还是「拼接」。
"""
import os
import sys

from PIL import Image

SUB = 16
GRID = 8
CELL = 32

TRANSFORMS = {
    "id": None,
    "r180": Image.Transpose.ROTATE_180,
    "lr": Image.Transpose.FLIP_LEFT_RIGHT,
    "tb": Image.Transpose.FLIP_TOP_BOTTOM,
    "r90": Image.Transpose.ROTATE_90,
    "r270": Image.Transpose.ROTATE_270,
    "tr": Image.Transpose.TRANSPOSE,
    "tv": Image.Transpose.TRANSVERSE,
}


def build_pool(sources):
    pool = {}
    for name, image in sources:
        for x in range(image.size[0] - SUB + 1):
            for y in range(image.size[1] - SUB + 1):
                window = image.crop((x, y, x + SUB, y + SUB))
                for key, op in TRANSFORMS.items():
                    piece = window if op is None else window.transpose(op)
                    pool.setdefault(piece.tobytes(), f"{name}[{x},{y}]{key}")
    return pool


def main(argv):
    ref_path = argv[0]
    sources = []
    for path in argv[1:]:
        name = os.path.basename(path).rsplit(".", 1)[0]
        sources.append((name, Image.open(path).convert("RGBA")))

    ref = Image.open(ref_path).convert("RGBA")
    pool = build_pool(sources)
    print(f"  候选 {len(pool)} 个")

    hits = 0
    total = 0
    for gy in range(GRID):
        for gx in range(GRID):
            cell = ref.crop((gx * CELL, gy * CELL, (gx + 1) * CELL, (gy + 1) * CELL))
            blank = all(cell.getpixel((x, y))[3] < 16
                        for y in range(CELL) for x in range(CELL))
            if blank:
                continue
            total += 1
            parts = []
            for sy in range(2):
                for sx in range(2):
                    sub = cell.crop((sx * SUB, sy * SUB, (sx + 1) * SUB, (sy + 1) * SUB))
                    parts.append(pool.get(sub.tobytes()))

            index = gx + gy * GRID
            found = sum(1 for part in parts if part)
            if found == 4:
                hits += 1
                # 若四块同源，只报一次。
                if len(set(parts)) == 1:
                    print(f"  idx {index:2d}: 4 块同源 {parts[0]}")
                else:
                    print(f"  idx {index:2d}: {parts[0]} | {parts[1]} / {parts[2]} | {parts[3]}")
            elif found:
                print(f"  idx {index:2d}: {found}/4 块命中 -> {[p for p in parts if p]}")

    print()
    print(f"  完整命中 {hits}/{total} 格")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
