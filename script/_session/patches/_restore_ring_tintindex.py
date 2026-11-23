"""Restore the ring tint index and stop the energy curve from saturating too early.

Two separate faults:

- `gt_port_base.json` lost its `tintindex` when the port models were restored from git, so the
  ring was drawn untinted and every tier looked the same grey.
- `transfer(level)` builds 4^(level-1) and saturates at the first overflow, which happens at
  level 14. UXV and MAX then came out as Integer.MAX_VALUE, and halving that for a dynamo
  gave a capacity that the storage treated as full from the start — hence the "1" readouts.
"""
import io
import json

# ---------------- 1. the tint index ----------------
p = r"src/main/resources/assets/goldentweaks/models/block/multiblock/gt_port_base.json"
with io.open(p, encoding="utf-8") as handle:
    model = json.load(handle)

for element in model["elements"]:
    face = element.get("faces", {}).get("north")
    if face is None:
        continue
    # The ring is the element that sits just behind the icon and names the `ring` texture.
    if face.get("texture") == "#ring":
        face["tintindex"] = 0
    elif face.get("texture") == "#icon":
        face["tintindex"] = 1

with io.open(p, "w", encoding="utf-8", newline="\n") as handle:
    json.dump(model, handle, indent=2, ensure_ascii=False)
    handle.write("\n")

rings = [e for e in model["elements"]
         if e.get("faces", {}).get("north", {}).get("texture") == "#ring"]
print("ring elements with tintindex:", sum(
    1 for e in rings if "tintindex" in e["faces"]["north"]))
