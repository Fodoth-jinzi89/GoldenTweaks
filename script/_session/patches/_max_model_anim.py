"""Point the MAX port models at the animated ring.

The tint cannot animate on a block — its colour is baked into the mesh — so the MAX models
name an animated texture and the tint answers white for them, letting the frames show
through unmodified.
"""
import io
import json
import os
import re

ROOT = r"src/main/resources/assets/goldentweaks/models/block/multiblock"
NAME = re.compile(r"^gt_port_(item|fluid|energy)_(input|output)_max\.json$")
RING = "goldentweaks:block/multiblock/port/level/max"


def main():
    changed = 0
    for name in sorted(os.listdir(ROOT)):
        if not NAME.match(name):
            continue
        path = os.path.join(ROOT, name)
        with io.open(path, encoding="utf-8") as handle:
            model = json.load(handle)
        model.setdefault("textures", {})["ring"] = RING
        with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
            json.dump(model, handle, indent=2, ensure_ascii=False)
            handle.write("\n")
        changed += 1
    print("MAX models repointed:", changed)


main()
