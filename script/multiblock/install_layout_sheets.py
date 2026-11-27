"""把各布局的连接贴图写进模组的 ct_test_* 资源位置。

每个测试方块的连接效果只读一张 `<name>_ctm.png`。方块模型不引用它——模型指向机壳自己的
`shell_<material>.png`，所以同一个材质的 8 个测试方块共用一张基础贴图，不再各存一份。

`_ctm` 必须显式登记到 atlases/blocks.json：模型不引用它，图集就不会自动收集，
缺失时连接效果会被静默跳过。
"""
import os
import sys

from PIL import Image

ASSETS = os.path.join("src", "main", "resources", "assets", "goldentweaks")
TEXTURES = os.path.join(ASSETS, "textures", "block")

LAYOUTS = ("horizontal", "horizontal_kryppers", "vertical",
           "roof", "roof_stair", "cross", "rectangle")

# 每种布局的贴图宽度：8 格是八向，4 格是四向，3 种是两格（64px）。
EXPECTED = {
    "horizontal": 64, "horizontal_kryppers": 64, "vertical": 64,
    "roof": 128, "roof_stair": 128, "cross": 128, "rectangle": 128,
}


def main(argv):
    if len(argv) < 1:
        print("usage: install_layout_sheets.py <art_dir>")
        return 1
    art_dir = argv[0]

    written = 0
    for material in ("pe", "pp"):
        for layout in LAYOUTS:
            source = os.path.join(art_dir, f"{material}_{layout}.png")
            if not os.path.exists(source):
                print(f"  缺失 {source}")
                continue

            image = Image.open(source).convert("RGBA")
            expected = EXPECTED[layout]
            if image.size[0] != expected:
                print(f"  {source}: 期望 {expected}px，实际 {image.size[0]}px")
                continue

            name = f"ct_test_{material}_{layout}"
            image.save(os.path.join(TEXTURES, f"{name}_ctm.png"))
            written += 1

    print(f"  写入 {written} 张连接贴图")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
