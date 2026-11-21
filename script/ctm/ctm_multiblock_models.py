#!/usr/bin/env python3
"""Emit block models and blockstates for the layered multiblock components.

Each component is a full cube whose six faces use the shared PE shell texture and
whose front face additionally carries a transparent overlay quad. Because the
shell layer references `goldentweaks:block/shell_pe`, the Continuity definition in
`optifine/ctm/shell_pe.properties` (matchTiles + connect=tile) joins these blocks
with the plain shell block into one continuous surface.

Run from the project root:
    python script/ctm_multiblock_models.py
"""

import json
import os

from PIL import Image

ASSETS = os.path.join("src", "main", "resources", "assets", "goldentweaks")
TEXTURE_DIR = os.path.join(ASSETS, "textures", "block", "multiblock")
SHELL = "goldentweaks:block/shell_pe"

# The north face of a model element sits at z = 0, so the overlay quad is pushed a
# hair further out (z slightly negative) to sit in front of the shell instead of
# z-fighting with it. Only the north face of this element is emitted.
OVERLAY_OFFSET = 0.01

# How far the screen readout moves when the face is mirrored, in texture pixels.
#
# The idle face bakes a screen at x=6..20 of a 32 px sheet, which is not centred (the face centre
# is 15.5). Mirroring the base layer therefore carries the screen to x=11..25, and the readout has
# to follow it: the glyph ink at x=8..18 lands at x=13..23, a pure translation of +5 px. The shift
# is baked into a generated texture rather than written as a UV offset, because a UV offset of that
# size would step outside the sprite and sample whatever neighbours it in the atlas.
READOUT_MIRROR_SHIFT_PX = 5

# A model whose front face is on NORTH. Matches the vanilla command_block
# convention (x=90 -> down, x=270 -> up).
ROTATIONS = {
    "north": {},
    "east": {"y": 90},
    "south": {"y": 180},
    "west": {"y": 270},
    "up": {"x": 270},
    "down": {"x": 90},
}


def shifted_readout(source_name, shift_px):
    """Return a texture name holding `source_name` moved `shift_px` to the right.

    Used for the mirrored readout: its position follows the mirrored screen while its glyphs stay
    upright, which a pure translation achieves and a UV flip would not.
    """
    source = os.path.join(TEXTURE_DIR, "%s.png" % source_name)
    target_name = "%s_mirrored" % source_name
    target = os.path.join(TEXTURE_DIR, "%s.png" % target_name)

    image = Image.open(source).convert("RGBA")
    moved = Image.new("RGBA", image.size, (0, 0, 0, 0))
    # Copy column by column so nothing wraps around, and clip whatever the shift pushes off-edge.
    for x in range(image.width):
        target_x = x + shift_px
        if 0 <= target_x < image.width:
            for y in range(image.height):
                moved.putpixel((target_x, y), image.getpixel((x, y)))
    moved.save(target)
    return "goldentweaks:block/multiblock/%s" % target_name


def cube_model(overlay, mirrored=False, roll=0, extras=()):
    # Mirroring is a horizontal flip of the overlay quad's UVs. The roll is a rotation of the
    # texture *inside* the front face, which vanilla expresses with the face's own "rotation"
    # field. Rotating the whole model instead would move the front to another side, which is what
    # `facing` already does.
    #
    # `extras` are further front-face layers drawn on top of the base overlay. On a mirrored
    # machine their *position* follows the reflection — the readout ends up on the mirrored side of
    # the screen — while their *content* stays upright. That is a translation, so a mirrored model
    # uses a pre-shifted copy of the sprite (see `shifted_readout`) instead of flipped UVs.
    overlay_uv = [16, 0, 0, 16] if mirrored else [0, 0, 16, 16]
    overlay_face = {"uv": overlay_uv, "texture": "#overlay"}
    if roll:
        overlay_face["rotation"] = (roll * 90) % 360

    elements = [
        {
            "from": [0, 0, 0],
            "to": [16, 16, 16],
            "faces": {
                "down": {"uv": [0, 0, 16, 16], "texture": "#shell", "cullface": "down"},
                "up": {"uv": [0, 16, 16, 0], "texture": "#shell", "cullface": "up"},
                "north": {"uv": [0, 0, 16, 16], "texture": "#shell", "cullface": "north"},
                "south": {"uv": [0, 0, 16, 16], "texture": "#shell", "cullface": "south"},
                "west": {"uv": [0, 0, 16, 16], "texture": "#shell", "cullface": "west"},
                "east": {"uv": [0, 0, 16, 16], "texture": "#shell", "cullface": "east"},
            },
        },
        {
            "from": [0, 0, -OVERLAY_OFFSET],
            "to": [16, 16, 0],
            "faces": {"north": overlay_face},
        },
    ]

    textures = {
        "shell": SHELL,
        "overlay": "goldentweaks:block/multiblock/%s" % overlay,
        "particle": SHELL,
    }

    # Each extra layer sits a hair further out than the previous one, so the stack never
    # z-fights: base, then the readout.
    for index, extra in enumerate(extras):
        offset = OVERLAY_OFFSET * (index + 2)
        # `extra` is the texture name verbatim (e.g. `controller_name`); only the material key is
        # shortened, so the model refers to the file that actually exists.
        texture_key = extra.replace("controller_", "")
        sprite = extra + ("_mirrored" if mirrored else "")
        textures[texture_key] = "goldentweaks:block/multiblock/%s" % sprite

        extra_face = {"uv": [0, 0, 16, 16], "texture": "#%s" % texture_key}
        if roll:
            extra_face["rotation"] = (roll * 90) % 360
        elements.append({
            "from": [0, 0, -offset],
            "to": [16, 16, -offset + OVERLAY_OFFSET],
            "faces": {"north": extra_face},
        })

    return {
        "parent": "minecraft:block/block",
        "render_type": "minecraft:cutout",
        "textures": textures,
        "elements": elements,
    }


def variant(model, rotation):
    entry = {"model": model}
    entry.update(rotation)
    return entry


def facing_variants(model):
    return {
        "facing=%s" % facing: variant(model, rotation)
        for facing, rotation in ROTATIONS.items()
    }


def write_json(path, payload):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(payload, handle, indent=2)
        handle.write("\n")
    print("wrote %s" % path)


def main():
    model_dir = os.path.join(ASSETS, "models", "block", "multiblock")
    state_dir = os.path.join(ASSETS, "blockstates")

    # Simple components: one model, one facing property.
    simple = {
        "input_bus": "input_bus",
        "auto_input_bus": "auto_input_bus",
        "output_bus": "output_bus",
        "input_catalyst": "input_catalyst",
    }
    for block, overlay in simple.items():
        model_id = "goldentweaks:block/multiblock/%s" % overlay
        write_json(os.path.join(model_dir, "%s.json" % overlay), cube_model(overlay))
        write_json(os.path.join(state_dir, "%s.json" % block),
                   {"variants": facing_variants(model_id)})

    # Controller: it is an avaritia_more_items multiblock host, so its blockstate carries the
    # framework's `formed` flag instead of the old four-way `state`.
    #
    # The screen contents are split across layers: the state texture carries the machine face, and
    # the readout that belongs to that state is a separate sprite on top. Each state shows exactly
    # one of them: an idle machine displays its name, a working one displays the progress arrow.
    # They must never be drawn together, since the two sprites share pixels in the readout area.
    SCREEN_LAYERS = {
        "idle": ("controller_name",),
        "working": ("controller_arrow",),
    }

    # The readout sprites need a shifted copy for mirrored machines, generated up front so the
    # models can just reference it.
    for source in ("controller_name", "controller_arrow"):
        print("wrote %s" % shifted_readout(source, READOUT_MIRROR_SHIFT_PX))

    states = ["unconstructed", "off", "idle", "working"]
    for state in states:
        overlay = "controller_%s" % state
        extras = SCREEN_LAYERS.get(state, ())
        for mirrored in (False, True):
            suffix = "_mirrored" if mirrored else ""
            model_name = "%s%s" % (overlay, suffix)
            write_json(os.path.join(model_dir, "%s.json" % model_name),
                       cube_model(overlay, mirrored, extras=extras))

    # The overlay is decided by (formed, running) alone:
    #   not formed          -> unconstructed, whatever the switch says
    #   formed and running  -> idle
    #   formed and stopped  -> off
    #
    # `roll` spins the front texture in place, so it is a property of the model, not of the
    # blockstate rotation: every roll needs its own model file, and the variant only adds the
    # facing rotation. Rotating the whole model would move the front to another side instead.
    variants = {}
    for formed, running, overlay in (
        (False, False, "controller_unconstructed"),
        (False, True, "controller_unconstructed"),
        (True, True, "controller_idle"),
        (True, False, "controller_off"),
    ):
        for mirrored in (False, True):
            mirrored_suffix = "_mirrored" if mirrored else ""
            for roll in range(4):
                roll_suffix = "_roll%d" % roll if roll else ""
                model_name = "%s%s%s" % (overlay, mirrored_suffix, roll_suffix)
                extras = SCREEN_LAYERS.get(overlay.replace("controller_", ""), ())
                write_json(os.path.join(model_dir, "%s.json" % model_name),
                           cube_model(overlay, mirrored, roll, extras=extras))
                model_id = "goldentweaks:block/multiblock/%s" % model_name
                for facing, rotation in ROTATIONS.items():
                    key = "facing=%s,mirrored=%s,running=%s,roll=%d,formed=%s" % (
                        facing, str(mirrored).lower(), str(running).lower(), roll,
                        str(formed).lower())
                    variants[key] = variant(model_id, rotation)

    write_json(os.path.join(state_dir, "chemical_plant_controller.json"),
               {"variants": variants})


if __name__ == "__main__":
    main()
