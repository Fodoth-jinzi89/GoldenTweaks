"""Generate the advanced kettle's resources.

Mirrors the basic kettle's layout exactly, substituting the advanced art and the PP casing:

- block models for the four states, both mirrors, four rolls;
- the blockstate that maps them;
- the GUI sheet;
- its own structure file, whose "x" key is the PP casing;
- language entries for the block and the item.
"""
import io
import json
import os
import shutil

ROOT = r"src/main/resources"
ARTS = r"arts/multiblock/acp"
BASE = r"src/main/resources/assets/goldentweaks/models/block/multiblock"
GUI = r"src/main/resources/assets/goldentweaks/textures/gui"

MACHINE = "advanced_chemical_plant_controller"
MODEL_PREFIX = "advanced_controller"

# The four looks, matching the basic kettle's naming.
STATES = ["idle", "off", "working", "unconstructed"]

# Which art file backs each state.
ART = {
    "idle": "advanced_chemical_plant_idle.png",
    "off": "advanced_chemical_plant_off.png",
    "working": "advanced_chemical_plant_working.png",
    "unconstructed": "advanced_chemical_plant_unconstructed.png",
}

ROTATIONS = {
    "north": {}, "east": {"y": 90}, "south": {"y": 180},
    "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90},
}


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def main():
    # ---- block textures ----
    textures = os.path.join(ROOT, "assets/goldentweaks/textures/block/multiblock")
    os.makedirs(textures, exist_ok=True)
    for state, art in ART.items():
        target = ("%s.png" % MODEL_PREFIX
                  if state == "idle" else "%s_%s.png" % (MODEL_PREFIX, state))
        shutil.copyfile(os.path.join(ARTS, art), os.path.join(textures, target))
    # The nameplate is its own quad in the model, so it gets its own file name.
    shutil.copyfile(os.path.join(ARTS, "advanced_chemical_plant_name.png"),
                    os.path.join(textures, "%s_name.png" % MODEL_PREFIX))

    # ---- GUI sheet ----
    os.makedirs(GUI, exist_ok=True)
    shutil.copyfile(os.path.join(ARTS, "advanced_chemical_plant_screen.png"),
                    os.path.join(GUI, "advanced_chemical_plant_gui.png"))

    # ---- block models ----
    # The basic kettle's models are a full cube whose faces use the shell texture plus a front
    # overlay. Read one of them so the geometry is copied rather than retyped.
    with io.open(os.path.join(BASE, "controller_idle.json"), encoding="utf-8") as handle:
        template = json.load(handle)

    made = 0
    for state in STATES:
        # The state art goes on the overlay quad; the cube itself stays the PP casing, and the
        # nameplate keeps the machine's own label art.
        state_tex = ("goldentweaks:block/multiblock/%s" % MODEL_PREFIX
                     if state == "idle"
                     else "goldentweaks:block/multiblock/%s_%s" % (MODEL_PREFIX, state))
        for mirrored in (False, True):
            for roll in range(4):
                suffix = ""
                if mirrored:
                    suffix += "_mirrored"
                if roll:
                    suffix += "_roll%d" % roll
                model = json.loads(json.dumps(template))
                model["textures"]["shell"] = "goldentweaks:block/shell_pp"
                model["textures"]["particle"] = "goldentweaks:block/shell_pp"
                model["textures"]["overlay"] = state_tex
                model["textures"]["name"] = "goldentweaks:block/multiblock/%s_name" % MODEL_PREFIX
                write(os.path.join(BASE, "%s%s%s.json" % (MODEL_PREFIX,
                                                          "" if state == "idle" else "_" + state,
                                                          suffix)), model)
                made += 1

    # ---- blockstate ----
    variants = {}
    for facing, rotation in ROTATIONS.items():
        for mirrored in ("false", "true"):
            for roll in range(4):
                for running in ("false", "true"):
                    for crafting in ("false", "true"):
                        for formed in ("false", "true"):
                            if formed == "false":
                                base = "unconstructed"
                            elif crafting == "true":
                                base = "working"
                            elif running == "true":
                                base = "idle"
                            else:
                                base = "off"
                            name = MODEL_PREFIX if base == "idle" else "%s_%s" % (MODEL_PREFIX, base)
                            if mirrored == "true":
                                name += "_mirrored"
                            if roll:
                                name += "_roll%d" % roll
                            entry = {"model": "goldentweaks:block/multiblock/" + name}
                            entry.update(rotation)
                            variants["facing=%s,mirrored=%s,running=%s,crafting=%s,roll=%d,formed=%s"
                                       % (facing, mirrored, running, crafting, roll, formed)] = entry

    write(os.path.join(ROOT, "assets/goldentweaks/blockstates/%s.json" % MACHINE),
          {"variants": variants})
    made += 1

    # ---- item model ----
    write(os.path.join(ROOT, "assets/goldentweaks/models/item/%s.json" % MACHINE),
          {"parent": "goldentweaks:block/multiblock/" + MODEL_PREFIX})
    made += 1

    # ---- structure ----
    with io.open(os.path.join(ROOT, "data/goldentweaks/multiblock/structures/chemical_reactor.json"),
                 encoding="utf-8") as handle:
        structure = json.load(handle)
    structure["keys"]["a"] = {"block": "goldentweaks:" + MACHINE}
    structure["keys"]["x"] = "#goldentweaks:multiblock/advanced_casing"
    write(os.path.join(ROOT, "data/goldentweaks/multiblock/structures/advanced_chemical_reactor.json"),
          structure)
    made += 1

    # ---- the casing tag this structure points at: PP only ----
    write(os.path.join(ROOT, "data/goldentweaks/tags/block/multiblock/advanced_casing.json"),
          {"replace": False,
           "values": ["#goldentweaks:multiblock/ports", "goldentweaks:shell_pp"]})
    made += 1

    # ---- language ----
    for name, text in (("zh_cn", "进阶化学反应釜"), ("en_us", "Advanced Chemical Reaction Kettle")):
        path = os.path.join(ROOT, "assets/goldentweaks/lang/%s.json" % name)
        with io.open(path, encoding="utf-8") as handle:
            data = json.load(handle)
        data["block.goldentweaks." + MACHINE] = text
        with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
            json.dump(data, handle, indent=2, ensure_ascii=False)
            handle.write("\n")

    print("wrote %d files" % made)


main()
