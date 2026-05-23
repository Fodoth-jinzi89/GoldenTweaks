from pathlib import Path
from PIL import Image, ImageDraw
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

MODEL_PATH = BASE_PATH / "assets" / MODID / "models" / "item"
TEXTURE_PATH = BASE_PATH / "assets" / MODID / "textures" / "item"

MODEL_PATH.mkdir(parents=True, exist_ok=True)
TEXTURE_PATH.mkdir(parents=True, exist_ok=True)

TIERS = range(4, 11)

# tier -> color
COLORS = {
    4: (120, 220, 120),
    5: (80, 170, 255),
    6: (180, 100, 255),
    7: (255, 120, 120),
    8: (255, 170, 60),
    9: (255, 80, 180),
    10: (255, 255, 120),
}


def create_texture(path: Path, tier: int):

    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)

    color = COLORS[tier]

    # 外框
    draw.rounded_rectangle(
        (2, 2, 13, 13),
        radius=2,
        fill=(40, 40, 40),
        outline=color,
        width=1
    )

    # 中心金属板
    draw.rectangle(
        (4, 4, 11, 11),
        fill=color
    )

    # 高光
    draw.rectangle(
        (4, 4, 7, 5),
        fill=(255, 255, 255, 120)
    )

    # tier数字点
    for i in range(min(tier, 10)):
        x = 1 + (i % 5) * 3
        y = 14 if i < 5 else 12

        draw.point((x, y), fill=color)

    image.save(path)


def create_model(path: Path, texture_name: str):

    model = {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": f"{MODID}:item/{texture_name}"
        }
    }

    with open(path, "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2, ensure_ascii=False)


for tier in TIERS:

    name = f"armor_plate_tier_{tier}"

    texture_file = TEXTURE_PATH / f"{name}.png"
    model_file = MODEL_PATH / f"{name}.json"

    create_texture(texture_file, tier)
    create_model(model_file, name)

    print(f"Generated: {name}")

print("Done.")