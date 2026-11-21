import json, io
d = json.load(io.open(r"src/main/resources/assets/goldentweaks/blockstates/chemical_plant_controller.json", encoding="utf-8"))
seen = {}
for k, v in d["variants"].items():
    state = tuple(p for p in k.split(",") if p.startswith(("running", "formed")))
    seen.setdefault(state, set()).add(v["model"].split("/")[-1])
for s, m in sorted(seen.items()):
    print(s, "->", sorted(m))
