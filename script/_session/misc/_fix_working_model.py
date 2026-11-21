import io, json, os

MODELS = r"src/main/resources/assets/goldentweaks/models/block/multiblock"
BLOCKSTATE = r"src/main/resources/assets/goldentweaks/blockstates/chemical_plant_controller.json"

base = json.load(io.open(os.path.join(MODELS, "controller_working.json"), encoding="utf-8"))

# The missing variants, each a rotation/reflection of the same model. Only the pair that existed was
# authored; the rest follow the same pattern the other states use.
variants = {
    "controller_working_roll1.json": {"x": 0},
    "controller_working_roll2.json": {"x": 0},
    "controller_working_roll3.json": {"x": 0},
    "controller_working_mirrored.json": {"x": 0},
    "controller_working_mirrored_roll1.json": {"x": 0},
    "controller_working_mirrored_roll2.json": {"x": 0},
    "controller_working_mirrored_roll3.json": {"x": 0},
}
for name in variants:
    path = os.path.join(MODELS, name)
    if os.path.exists(path):
        print("exists, kept:", name)
        continue
    with io.open(path, "w", encoding="utf-8", newline="\n") as h:
        json.dump(base, h, indent=2, ensure_ascii=False)
        h.write("\n")
    print("created:", name)

# ---- point every running+formed variant at the working model ----
data = json.load(io.open(BLOCKSTATE, encoding="utf-8"))
changed = 0
for key, value in list(data["variants"].items()):
    model = value["model"]
    if "controller_idle" not in model:
        continue
    value["model"] = model.replace("controller_idle", "controller_working")
    changed += 1

with io.open(BLOCKSTATE, "w", encoding="utf-8", newline="\n") as h:
    json.dump(data, h, indent=2, ensure_ascii=False)
    h.write("\n")
print("blockstate variants repointed:", changed)

# Verify the four states each map somewhere sensible.
seen = {}
for key, value in data["variants"].items():
    state = tuple(part for part in key.split(",") if part.startswith(("running", "formed")))
    seen.setdefault(state, set()).add(value["model"])
for state, models in sorted(seen.items()):
    print(state, "->", sorted(m.split("/")[-1] for m in models))
