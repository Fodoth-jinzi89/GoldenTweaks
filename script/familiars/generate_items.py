from pathlib import Path
from PIL import Image, ImageDraw
import colorsys
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

ARMOR_TIERS = range(1, 11)
COMMON_TIERS = range(1, 11)
ANIMATED_TIER = 10
ANIMATION_FRAMES = 16
ANIMATION_FRAMETIME = 2

# tier -> unified mid-tone material color
TIER_COLORS = {
    1: (145, 93, 48),     # vanilla wood
    2: (198, 111, 63),    # vanilla copper
    3: (210, 209, 199),   # vanilla iron
    4: (245, 205, 74),    # vanilla gold
    5: (83, 220, 216),    # vanilla diamond
    6: (75, 64, 73),      # vanilla netherite
    7: (180, 45, 58),     # crimson
    8: (55, 142, 255),    # azure
    9: (150, 75, 220),    # purple
    10: (255, 230, 80),   # rainbow highlight base
}

RAINBOW_RAMP = [
    (230, 60, 70),
    (255, 155, 55),
    (255, 225, 75),
    (85, 210, 95),
    (70, 180, 255),
    (150, 85, 225),
]

ESSENCE_SYMBOL_COLORS = {
    "magic_power": (170, 95, 255),
    "magic_resist": (70, 185, 255),
}

def shift_hsv(color, hue_shift=0.0, sat_mul=1.0, val_mul=1.0):
    r, g, b = [channel / 255 for channel in color]
    h, s, v = colorsys.rgb_to_hsv(r, g, b)
    h = (h + hue_shift) % 1.0
    s = max(0.0, min(1.0, s * sat_mul))
    v = max(0.0, min(1.0, v * val_mul))
    return tuple(round(channel * 255) for channel in colorsys.hsv_to_rgb(h, s, v))


def create_palette(base_color):
    return {
        "outline": shift_hsv(base_color, hue_shift=-0.03, sat_mul=1.05, val_mul=0.34),
        "dark": shift_hsv(base_color, hue_shift=-0.02, sat_mul=1.08, val_mul=0.58),
        "mid": base_color,
        "light": shift_hsv(base_color, hue_shift=0.02, sat_mul=0.88, val_mul=1.22),
        "spark": shift_hsv(base_color, hue_shift=0.03, sat_mul=0.45, val_mul=1.45),
    }


def draw_pixel_run(draw, y, x0, x1, color):
    draw.line((x0, y, x1, y), fill=color)


def draw_tier_marks(draw, tier, color):
    for i in range(tier):
        x = 4 + (i % 5) * 2
        y = 13 if i < 5 else 12
        draw.point((x, y), fill=color)


def tier_palette(tier):
    return create_palette(TIER_COLORS[tier])


def rainbow_at(index):
    return RAINBOW_RAMP[index % len(RAINBOW_RAMP)]


def draw_rainbow_runs(draw, runs):
    for index, (y, x0, x1) in enumerate(runs):
        draw_pixel_run(draw, y, x0, x1, rainbow_at(index))


def animation_meta_path(path: Path):
    return Path(f"{path}.mcmeta")


def rainbow_gradient_frame(source: Image.Image, frame: int):
    frame_image = Image.new("RGBA", source.size, (0, 0, 0, 0))
    pixels = frame_image.load()
    source_pixels = source.load()

    for y in range(source.height):
        for x in range(source.width):
            r, g, b, a = source_pixels[x, y]
            if a == 0:
                continue

            value = max(r, g, b) / 255
            saturation = 0.78

            if value < 0.36:
                value = max(0.18, value * 0.72)
                saturation = 0.88
            elif value > 0.82:
                value = min(1.0, value * 1.08)
                saturation = 0.42

            hue = ((x * 0.055) + (y * 0.075) + (frame / ANIMATION_FRAMES)) % 1.0
            pixels[x, y] = tuple(
                round(channel * 255)
                for channel in colorsys.hsv_to_rgb(hue, saturation, value)
            ) + (a,)

    return frame_image


def save_item_texture(path: Path, tier: int, image: Image.Image):
    meta_path = animation_meta_path(path)

    if tier != ANIMATED_TIER:
        image.save(path)
        meta_path.unlink(missing_ok=True)
        return

    strip = Image.new("RGBA", (image.width, image.height * ANIMATION_FRAMES), (0, 0, 0, 0))

    for frame in range(ANIMATION_FRAMES):
        strip.paste(rainbow_gradient_frame(image, frame), (0, image.height * frame))

    strip.save(path)

    meta = {
        "animation": {
            "frametime": ANIMATION_FRAMETIME,
            "interpolate": True
        }
    }

    with open(meta_path, "w", encoding="utf-8") as f:
        json.dump(meta, f, indent=2, ensure_ascii=False)


def create_armor_texture(path: Path, tier: int):
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)

    palette = tier_palette(tier)

    # Pixel-stepped silhouette: readable at 16x16 and free of smooth UI-style edges.
    outline = [
        (4, 1), (11, 1), (14, 4), (14, 10),
        (11, 14), (4, 14), (1, 10), (1, 4),
    ]
    body = [
        (5, 2), (10, 2), (13, 5), (13, 9),
        (10, 13), (5, 13), (2, 9), (2, 5),
    ]
    draw.polygon(outline, fill=palette["outline"])
    draw.polygon(body, fill=palette["mid"])

    if tier == 10:
        draw_rainbow_runs(draw, [
            (3, 5, 10), (4, 4, 11), (5, 3, 12),
            (6, 3, 12), (7, 2, 13), (8, 2, 13),
            (9, 3, 12),
        ])

    # Left-top light source, matching vanilla item readability.
    draw_pixel_run(draw, 3, 5, 10, palette["light"])
    draw_pixel_run(draw, 4, 4, 11, palette["light"])
    draw_pixel_run(draw, 5, 3, 5, palette["light"])
    draw.point((6, 6), fill=palette["spark"])
    draw.point((9, 3), fill=palette["spark"])

    # Lower-right planes carry the weight of the material without banding.
    draw_pixel_run(draw, 10, 5, 12, palette["dark"])
    draw_pixel_run(draw, 11, 6, 11, palette["dark"])
    draw_pixel_run(draw, 12, 7, 10, palette["dark"])
    draw.line((12, 5, 12, 9), fill=palette["dark"])

    # Small chips and rivets suggest forged armor plate, not a flat button.
    draw.point((4, 7), fill=palette["dark"])
    draw.point((8, 8), fill=palette["dark"])
    draw.point((10, 6), fill=palette["outline"])
    draw.point((3, 10), fill=palette["light"])
    draw.point((11, 11), fill=palette["outline"])

    # Compact tier marks. They stay inside the silhouette and use the same palette.
    draw_tier_marks(draw, max(0, tier - 3), palette["spark"])

    save_item_texture(path, tier, image)


def create_essence_texture(path: Path, tier: int, essence_type: str):
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)

    palette = tier_palette(tier)
    symbol = create_palette(ESSENCE_SYMBOL_COLORS[essence_type])

    outline = [
        (7, 1), (10, 2), (13, 5), (14, 8), (12, 12),
        (8, 14), (4, 13), (2, 10), (2, 6), (4, 3),
    ]
    body = [
        (7, 2), (10, 3), (12, 5), (13, 8), (11, 11),
        (8, 13), (5, 12), (3, 10), (3, 6), (5, 4),
    ]
    draw.polygon(outline, fill=palette["outline"])
    draw.polygon(body, fill=palette["mid"])

    if tier == 10:
        draw_rainbow_runs(draw, [
            (4, 6, 9), (5, 5, 10), (6, 4, 11),
            (7, 4, 12), (8, 3, 12), (9, 4, 11),
        ])

    draw_pixel_run(draw, 4, 6, 9, palette["light"])
    draw_pixel_run(draw, 5, 5, 10, palette["light"])
    draw.point((7, 3), fill=palette["spark"])
    draw.point((4, 7), fill=palette["spark"])
    draw_pixel_run(draw, 10, 6, 11, palette["dark"])
    draw_pixel_run(draw, 11, 7, 10, palette["dark"])

    if essence_type == "magic_power":
        draw.line((7, 5, 9, 7), fill=symbol["spark"])
        draw.line((9, 7, 6, 11), fill=symbol["outline"])
        draw.point((10, 4), fill=symbol["spark"])
    else:
        draw.line((5, 7, 8, 4), fill=symbol["spark"])
        draw.line((8, 4, 11, 7), fill=symbol["spark"])
        draw.line((5, 8, 8, 11), fill=symbol["outline"])
        draw.line((8, 11, 11, 8), fill=symbol["outline"])

    draw_tier_marks(draw, tier, palette["spark"])

    save_item_texture(path, tier, image)


def create_life_fruit_texture(path: Path, tier: int):
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)

    palette = tier_palette(tier)
    leaf = create_palette((95, 190, 80))

    outline = [
        (7, 3), (11, 3), (13, 5), (14, 9), (12, 13),
        (9, 15), (5, 14), (2, 11), (2, 7), (4, 4),
    ]
    body = [
        (7, 4), (10, 4), (12, 6), (13, 9), (11, 12),
        (9, 14), (5, 13), (3, 11), (3, 7), (5, 5),
    ]
    draw.polygon(outline, fill=palette["outline"])
    draw.polygon(body, fill=palette["mid"])

    if tier == 10:
        draw_rainbow_runs(draw, [
            (5, 6, 10), (6, 5, 12), (7, 4, 12),
            (8, 3, 13), (9, 3, 12), (10, 4, 12),
        ])

    draw.line((7, 3, 8, 1), fill=palette["outline"])
    draw.polygon([(9, 2), (13, 1), (12, 4)], fill=leaf["outline"])
    draw.polygon([(10, 2), (12, 2), (12, 3)], fill=leaf["mid"])

    draw_pixel_run(draw, 5, 6, 9, palette["light"])
    draw.point((5, 6), fill=palette["spark"])
    draw.point((7, 7), fill=palette["spark"])
    draw_pixel_run(draw, 11, 5, 11, palette["dark"])
    draw_pixel_run(draw, 12, 6, 10, palette["dark"])
    draw.point((10, 8), fill=palette["outline"])

    if tier >= 2:
        draw.point((11, 5), fill=palette["spark"])
        draw.point((4, 10), fill=palette["light"])
    if tier >= 3:
        draw.point((8, 10), fill=palette["spark"])
        draw.point((12, 9), fill=palette["outline"])

    draw_tier_marks(draw, max(0, tier - 3), palette["spark"])

    save_item_texture(path, tier, image)


def create_model(path: Path, texture_name: str):
    model = {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": f"{MODID}:item/{texture_name}"
        }
    }

    with open(path, "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2, ensure_ascii=False)


for tier in ARMOR_TIERS:
    name = f"armor_plate_tier_{tier}"

    texture_file = TEXTURE_PATH / f"{name}.png"
    model_file = MODEL_PATH / f"{name}.json"

    create_armor_texture(texture_file, tier)
    create_model(model_file, name)

    print(f"Generated: {name}")

for essence_type in ESSENCE_SYMBOL_COLORS:
    for tier in COMMON_TIERS:
        name = f"{essence_type}_tier_{tier}"

        texture_file = TEXTURE_PATH / f"{name}.png"
        model_file = MODEL_PATH / f"{name}.json"

        create_essence_texture(texture_file, tier, essence_type)
        create_model(model_file, name)

        print(f"Generated: {name}")

for tier in COMMON_TIERS:
    name = f"life_fruit_tier_{tier}"

    texture_file = TEXTURE_PATH / f"{name}.png"
    model_file = MODEL_PATH / f"{name}.json"

    create_life_fruit_texture(texture_file, tier)
    create_model(model_file, name)

    print(f"Generated: {name}")

print("Done.")
