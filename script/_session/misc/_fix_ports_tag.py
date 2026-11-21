import io, json

path = "src/main/resources/data/goldentweaks/tags/block/multiblock/ports.json"
data = json.load(io.open(path, encoding="utf-8"))

TIER_IDS = ["ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv"]
# The tier-agnostic ids no longer name any block; the c: tags above already cover all nine tiers.
stale = {
    f"goldentweaks:gt_port_{kind}_{mode}"
    for kind in ("item", "fluid", "energy") for mode in ("input", "output")
}
data["values"] = [v for v in data["values"] if v not in stale]

with io.open(path, "w", encoding="utf-8", newline="\n") as h:
    json.dump(data, h, indent=2, ensure_ascii=False)
    h.write("\n")

print("remaining values:")
for v in data["values"]:
    print(" ", v)

# Verify the c: tags really do cover every tier, since this file now leans on them entirely.
import os
missing = []
for kind in ("item", "fluid", "energy"):
    for mode in ("input", "output"):
        tag = f"src/main/resources/data/c/tags/block/multiblock/{mode}/{kind}.json"
        entries = set(json.load(io.open(tag, encoding="utf-8"))["values"])
        for tier in TIER_IDS:
            if f"goldentweaks:gt_port_{kind}_{mode}_{tier}" not in entries:
                missing.append((tag, tier))
print("missing tier entries:", missing if missing else "none")
