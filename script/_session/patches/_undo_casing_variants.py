"""Undo the per-casing model variants.

Registering one model per casing means 150 blocks per casing, which is the approach this was meant to
avoid. Create's `CTModel` solves the same problem without it: a single model, a behaviour object that
asks the world what it connects to, and a sprite shift chosen per face at render time.

The blockstate generator and the casing variant registration are both reverted; the blockstates go back
to one variant per facing.
"""
import json
import os

STATES = "src/main/resources/assets/goldentweaks/blockstates"

# Regenerate the plain blockstates: one variant per facing, no casing.
ROTATION = {
    "north": {},
    "east": {"y": 90},
    "south": {"y": 180},
    "west": {"y": 270},
    "up": {"x": 270},
    "down": {"x": 90},
}

written = 0
for name in sorted(os.listdir(STATES)):
    if not (name.startswith("gt_port_") and name.endswith(".json")):
        continue
    port = name[:-len(".json")]
    variants = {}
    for facing, rotation in ROTATION.items():
        entry = {"model": f"goldentweaks:block/multiblock/{port}"}
        entry.update(rotation)
        variants[f"facing={facing}"] = entry

    with open(os.path.join(STATES, name), "w", encoding="utf-8", newline="\n") as out:
        json.dump({"variants": variants}, out, indent=2, ensure_ascii=False)
        out.write("\n")
    written += 1

print("blockstates reverted:", written)
