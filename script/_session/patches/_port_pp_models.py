"""A port set into the advanced kettle shows the polypropylene plate.

The two casings are different artwork, not two tints of one sheet — polyethylene is a fine grey
weave, polypropylene is a violet panel with rivets — so tinting cannot express the difference. The
base model is therefore duplicated with the other plate, and each blockstate picks between them on
the new `advanced_casing` property.

The alternative was a block entity renderer, which would have meant drawing every port by hand and
losing the model's culling. Duplicating one JSON and adding a variant key per facing is the cheaper
and less fragile answer.
"""
import json, io, os, glob, shutil

MODELS = r"src/main/resources/assets/goldentweaks/models/block/multiblock"
STATES = r"src/main/resources/assets/goldentweaks/blockstates"

# ---- 1. the polypropylene base -----------------------------------------------------------------
src = os.path.join(MODELS, "gt_port_base.json")
dst = os.path.join(MODELS, "gt_port_base_pp.json")

d = json.load(io.open(src, encoding="utf-8"))
d.setdefault("textures", {})["shell"] = "goldentweaks:block/shell_pp"
io.open(dst, "w", encoding="utf-8", newline="\n").write(
    json.dumps(d, indent=2, ensure_ascii=False) + "\n")
print("base variant written:", os.path.basename(dst))

# ---- 2. one model per port, over the pp plate --------------------------------------------------
# Each port model only names its icon and ring; the plate comes from the parent. Cloning it with a
# different parent is what keeps the icon/ring untouched.
clones = 0
for path in sorted(glob.glob(os.path.join(MODELS, "gt_port_*.json"))):
    base = os.path.basename(path)
    if base in ("gt_port_base.json", "gt_port_base_pp.json"):
        continue
    model = json.load(io.open(path, encoding="utf-8"))
    if model.get("parent") != "goldentweaks:block/multiblock/gt_port_base":
        continue
    model["parent"] = "goldentweaks:block/multiblock/gt_port_base_pp"
    out = os.path.join(MODELS, base.replace(".json", "_pp.json"))
    io.open(out, "w", encoding="utf-8", newline="\n").write(
        json.dumps(model, indent=2, ensure_ascii=False) + "\n")
    clones += 1
print("port models cloned over the pp plate:", clones)

# ---- 3. every blockstate gains the two branches ------------------------------------------------
changed = 0
for path in sorted(glob.glob(os.path.join(STATES, "gt_port_*.json"))):
    name = os.path.basename(path)[:-len(".json")]
    data = json.load(io.open(path, encoding="utf-8"))
    variants = data.get("variants")
    if not variants:
        continue

    rebuilt = {}
    for key, value in variants.items():
        # the existing entry keeps its exact shape; the pp one repeats it with the cloned model
        rebuilt[key.replace("facing=", "advanced_casing=false,facing=", 1)] = value

        pp_model = json.loads(json.dumps(value))
        pp_model["model"] = value["model"] + "_pp"
        rebuilt[key.replace("facing=", "advanced_casing=true,facing=", 1)] = pp_model

    data["variants"] = rebuilt
    io.open(path, "w", encoding="utf-8", newline="\n").write(
        json.dumps(data, indent=2, ensure_ascii=False) + "\n")
    changed += 1
print("blockstates given both branches:", changed)
