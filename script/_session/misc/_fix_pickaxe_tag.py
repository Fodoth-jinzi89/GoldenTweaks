import io, json

TIER_IDS = ["ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv"]
KINDS = ["item", "fluid", "energy"]
MODES = ["input", "output"]

path = "src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json"
data = json.load(io.open(path, encoding="utf-8"))
values = data["values"]

# Drop the six tier-agnostic port ids: those blocks no longer exist.
values = [v for v in values
          if not (v.startswith("goldentweaks:gt_port_")
                  and not any(v.endswith("_" + t) for t in TIER_IDS))]

# Add all nine tiers of every kind and mode, in a stable order.
ports = [f"goldentweaks:gt_port_{kind}_{mode}_{tier}"
         for kind in KINDS for mode in MODES for tier in TIER_IDS]

# Keep the existing layout: golden blocks first, then the ports, then the borrowed compat ids.
head = [v for v in values if v.startswith("goldentweaks:") and "gt_port_" not in v]
tail = [v for v in values if not v.startswith("goldentweaks:")]

data["values"] = head + ports + tail
with io.open(path, "w", encoding="utf-8", newline="\n") as h:
    json.dump(data, h, indent=2, ensure_ascii=False)
    h.write("\n")

print("goldentweaks entries:", len(head), "ports:", len(ports), "compat:", len(tail))
for line in data["values"][:20]:
    print(" ", line)
