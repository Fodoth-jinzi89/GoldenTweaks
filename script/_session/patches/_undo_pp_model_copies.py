"""Undo the per-casing model duplication.

Copying a model per casing does not scale past a handful: a hundred casings would mean a hundred
copies of every port model and a hundred blockstate branches, all regenerated whenever a port
changes. The plate is chosen at render time instead, through NeoForge's ModelData, so one model and
one blockstate cover every casing that will ever exist.
"""
import json, io, os, glob

MODELS = r"src/main/resources/assets/goldentweaks/models/block/multiblock"
STATES = r"src/main/resources/assets/goldentweaks/blockstates"

# ---- 1. drop the duplicated models ------------------------------------------------------------
removed = 0
for path in glob.glob(os.path.join(MODELS, "gt_port_*_pp.json")):
    os.remove(path)
    removed += 1
print("duplicated models removed:", removed)

# ---- 2. restore the blockstates to a single branch ---------------------------------------------
changed = 0
for path in sorted(glob.glob(os.path.join(STATES, "gt_port_*.json"))):
    data = json.load(io.open(path, encoding="utf-8"))
    variants = data.get("variants")
    if not variants:
        continue

    restored = {}
    for key, value in variants.items():
        if not key.startswith("advanced_casing=false,"):
            continue
        restored[key[len("advanced_casing=false,"):]] = value

    data["variants"] = restored
    io.open(path, "w", encoding="utf-8", newline="\n").write(
        json.dumps(data, indent=2, ensure_ascii=False) + "\n")
    changed += 1
print("blockstates restored:", changed)
