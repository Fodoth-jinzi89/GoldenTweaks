import io, json, os

BLOCKSTATE = r"src/main/resources/assets/goldentweaks/blockstates/chemical_plant_controller.json"
MODELS = r"src/main/resources/assets/goldentweaks/models/block/multiblock"

data = json.load(io.open(BLOCKSTATE, encoding="utf-8"))

# `RUNNING` now means "a recipe is being processed", so:
#   running=true,  formed=true  -> working   (already correct)
#   running=false, formed=true  -> idle      (was pointing at off)
# and the "switched off" look no longer has a state of its own in this mapping, because the block
# state cannot tell idle from switched-off any more. The idle look is the honest one: the machine is
# assembled and not crafting, which is what the texture is asked to show.
#
# The off model stays on disk for the window's own switch icon; nothing here needs it.
changed = 0
for key, value in data["variants"].items():
    model = value["model"]
    if "controller_off" not in model:
        continue
    value["model"] = model.replace("controller_off", "controller_idle")
    changed += 1

with io.open(BLOCKSTATE, "w", encoding="utf-8", newline="\n") as h:
    json.dump(data, h, indent=2, ensure_ascii=False)
    h.write("\n")

print("repointed off -> idle:", changed, "variants")

seen = {}
for k, v in data["variants"].items():
    state = tuple(p for p in k.split(",") if p.startswith(("running", "formed")))
    seen.setdefault(state, set()).add(v["model"].split("/")[-1])
for s, m in sorted(seen.items()):
    print(s, "->", len(m), "models, e.g.", sorted(m)[0])
