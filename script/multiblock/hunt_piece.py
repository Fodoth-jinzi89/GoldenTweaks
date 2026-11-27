"""在若干候选图里找出某一小格的精确来源（穷举取窗与 8 种二面体变换）。

用于回答「这一格到底是从哪张图的哪个位置来的」。参考 _ct 里有些格与 shell_pp_frame 差约 100px，
说明它们来自别的图（例如模板），这里一次性把所有候选图都试过。
"""
import os
import sys

from PIL import Image

SUB = 16

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


def main(argv):
    if len(argv) < 3:
        print("usage: hunt_piece.py <sheet.png> <gx> <gy> <candidate.png>...")
        return 1
    sheet = Image.open(argv[0]).convert("RGBA")
    gx, gy = int(argv[1]), int(argv[2])
    want = sheet.crop((gx * SUB, gy * SUB, (gx + 1) * SUB, (gy + 1) * SUB))
    want_bytes = want.tobytes()

    print(f"  目标：{os.path.basename(argv[0])} 格 ({gx},{gy})")

    hits = []
    near = []
    for path in argv[3:]:
        image = Image.open(path).convert("RGBA")
        name = os.path.basename(path)
        for x in range(image.size[0] - SUB + 1):
            for y in range(image.size[1] - SUB + 1):
                window = image.crop((x, y, x + SUB, y + SUB))
                for key, op in TRANSFORMS.items():
                    piece = window if op is None else window.transpose(op)
                    data = piece.tobytes()
                    if data == want_bytes:
                        hits.append((name, x, y, key))
                    else:
                        diff = sum(1 for i in range(0, len(data), 4)
                                   if data[i:i + 4] != want_bytes[i:i + 4])
                        if diff <= 4:
                            near.append((diff, name, x, y, key))

    if hits:
        print("  精确命中:")
        for name, x, y, key in hits:
            print(f"    {name} [{x},{y}] {key}")
    else:
        print("  无精确命中")
    if near:
        print("  近似（差异 <= 4px）:")
        for diff, name, x, y, key in sorted(near)[:8]:
            print(f"    {diff}px  {name} [{x},{y}] {key}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
