"""Point every port model's ring at the shared grey sheet.

The icons are left alone on purpose: item ports keep AMI's item icon, fluid ports its
fluid icon, and energy ports its energy icon. The chemical and essentia sheets are for
port kinds that do not exist yet, so nothing references them.
"""
import io
import json
import os
import re

ROOT = r"src/main/resources/assets/goldentweaks/models/block/multiblock"
RING = "goldentweaks:block/multiblock/port/level/{mode}"
NAME = re.compile(r"^gt_port_(item|fluid|energy)_(input|output)_t\d+\.json$")


def main():
    changed = 0
    for name in sorted(os.listdir(ROOT)):
        match = NAME.match(name)
        if not match:
            continue
        mode = match.group(2)
        path = os.path.join(ROOT, name)

        with io.open(path, encoding="utf-8") as handle:
            model = json.load(handle)

        textures = model.setdefault("textures", {})
        textures["ring"] = RING.format(mode=mode)

        with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
            json.dump(model, handle, indent=2, ensure_ascii=False)
            handle.write("\n")
        changed += 1

    print("models repointed:", changed)


main()
