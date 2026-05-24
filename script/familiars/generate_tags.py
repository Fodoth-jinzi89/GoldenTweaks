from pathlib import Path
import json

MODID = "goldentweaks"

SCRIPT_DIR = Path(__file__).parent.resolve()
WORKSPACE_DIR = SCRIPT_DIR.parent.parent

BASE_PATH = (
    WORKSPACE_DIR
    / "src"
    / "main"
    / "resources"
)

TAG_PATH = (
    BASE_PATH
    / "data"
    / MODID
    / "tags"
    / "item"
)

TAG_PATH.mkdir(parents=True, exist_ok=True)

SERIES = {
    "armor_plate": 10,
    "life_fruit": 10,
    "magic_power": 10,
    "magic_resist": 10
}


def write_tag(path: Path, values: list[str]):
    path.parent.mkdir(parents=True, exist_ok=True)

    with open(path, "w", encoding="utf-8") as f:
        json.dump({
            "replace": False,
            "values": values
        }, f, ensure_ascii=False, indent=2)


# =========================================
# 收集每个tier的全部物品
# =========================================

all_tier_items: dict[int, list[str]] = {}

for tier in range(1, 11):

    all_tier_items[tier] = []

    for series in SERIES:
        all_tier_items[tier].append(
            f"{MODID}:{series}_tier_{tier}"
        )


# =========================================
# 1. 每个系列总tag
# goldentweaks:armor_plate
# =========================================

for series, max_tier in SERIES.items():

    values = [
        f"{MODID}:{series}_tier_{i}"
        for i in range(1, max_tier + 1)
    ]

    write_tag(
        TAG_PATH / f"{series}.json",
        values
    )


# =========================================
# 2. 每个tier全部物品tag
# goldentweaks:tiers/tier_1
# =========================================

for tier, values in all_tier_items.items():

    write_tag(
        TAG_PATH / "tiers" / f"tier_{tier}.json",
        values
    )


# =========================================
# 3. 分段tier tag
# =========================================

tier_groups = {
    "tier_1_3": range(1, 4),
    "tier_4_6": range(4, 7),
    "tier_7_9": range(7, 10),
    "tier_10": range(10, 11)
}

for name, tier_range in tier_groups.items():

    values = []

    for tier in tier_range:
        values.extend(all_tier_items[tier])

    write_tag(
        TAG_PATH / "groups" / f"{name}.json",
        values
    )


print("Item tags generated.")