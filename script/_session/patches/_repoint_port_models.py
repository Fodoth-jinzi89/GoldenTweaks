"""Point every port model at our own grey ring and icon sheets.

Previously each model named one of AMI's eighteen pre-coloured ring textures
(`multiblock_port/level/input/1` … `9`). The new art ships a single grey ring per
direction plus a grey icon per kind, and the voltage colour is applied at render
time from the block's tier — so one ring texture serves all nine tiers.
"""
import io
import json
import os
import re

ROOT = r"src/main/resources/assets/goldentweaks/models/block/multiblock"

# Our own sheets. The ring differs by direction only; the icon says what the port carries.
RING = "goldentweaks:block/multiblock/port/level/{mode}"
ICON = "goldentweaks:block/multiblock/port/{mode}_{kind}"

# Model file names look like gt_port_<kind>_<mode>_t<n>.json.
NAME = re.compile(r"^gt_port_(item|fluid|energy)_(input|output)_t\d+\.json$")

# The icons we have art for. Anything else keeps pointing at AMI's texture rather than
# at a file that does not exist.
DRAWN = {("item", "input"), ("fluid", "input"), ("item", "output"), ("fluid", "output")}


def main():
    changed = 0
    kept = []
    for name in sorted(os.listdir(ROOT)):
        match = NAME.match(name)
        if not match:
            continue
        kind, mode = match.group(1), match.group(2)
        path = os.path.join(ROOT, name)

        with io.open(path, encoding="utf-8") as handle:
            model = json.load(handle)

        textures = model.setdefault("textures", {})
        textures["ring"] = RING.format(mode=mode)

        if (kind, mode) in DRAWN:
            textures["icon"] = ICON.format(mode=mode, kind=kind)
        else:
            kept.append(name)

        with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
            json.dump(model, handle, indent=2, ensure_ascii=False)
            handle.write("\n")
        changed += 1

    print("models repointed:", changed)
    print("still using AMI icon:", len(kept), kept[:6])


main()
