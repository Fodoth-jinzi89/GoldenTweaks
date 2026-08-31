from copy import deepcopy
from itertools import product
from pathlib import Path

import nbtlib
from nbtlib import Compound, String


# 在每个元组中填写该组允许使用的方块。生成数量为四组长度的乘积。
BLOCK_GROUPS = {
    "create:layered_deepslate": (
        "create:layered_deepslate",
        "minecraft:black_concrete"
    ),
    "stoneworks:deepslate_pavers": (
        "stoneworks:deepslate_pavers",
        "minecraft:black_concrete"
    ),
    "create:polished_cut_deepslate": (
        "create:polished_cut_deepslate",
        "minecraft:smooth_stone",
        "minecraft:white_concrete"
    ),
    "supplementaries:blackstone_lamp": (
        "supplementaries:blackstone_lamp",
        "supplementaries:stone_lamp",
        "silentgems:black_diamond_lamp_inverted_on",
        "minecraft:glowstone"
    ),
}

Z_AXIS_BLOCKS = {
    "supplementaries:blackstone_lamp",
    "supplementaries:stone_lamp",
}

SCRIPT_DIR = Path(__file__).resolve().parent
SOURCE = SCRIPT_DIR.parent.parent / "floor.nbt"


def block_state(name: str) -> Compound:
    state = Compound({"Name": String(name)})
    if name in Z_AXIS_BLOCKS:
        state["Properties"] = Compound({"axis": String("z")})
    return state


def main() -> None:
    if not SOURCE.exists():
        raise FileNotFoundError(f"找不到源文件：{SOURCE}")

    source = nbtlib.load(SOURCE)
    source_names = {str(entry["Name"]) for entry in source["palette"]}
    missing = set(BLOCK_GROUPS) - source_names
    if missing:
        raise ValueError(f"floor.nbt 缺少方块：{', '.join(sorted(missing))}")

    if any(not candidates for candidates in BLOCK_GROUPS.values()):
        raise ValueError("四个方块组都必须至少填写一个候选方块。")

    combinations = product(*BLOCK_GROUPS.values())
    for index, replacement in enumerate(combinations, 1):
        mapping = dict(zip(BLOCK_GROUPS, replacement))
        result = deepcopy(source)
        for entry in result["palette"]:
            name = str(entry["Name"])
            if name in mapping:
                entry.clear()
                entry.update(block_state(mapping[name]))

        suffix = "__".join(name.replace(":", "_") for name in replacement)
        output = SCRIPT_DIR / f"floor_{index:04d}__{suffix}.nbt"
        result.save(output, gzipped=True)

    print(f"已在 {SCRIPT_DIR} 生成 {index} 个 floor 变体。")


if __name__ == "__main__":
    main()
