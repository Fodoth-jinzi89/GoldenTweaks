import io, json, os

TIERS = [
    ("ulv", "ULV", 1), ("lv", "LV", 2), ("mv", "MV", 3),
    ("hv", "HV", 4), ("ev", "EV", 5), ("iv", "IV", 6),
    ("luv", "LuV", 7), ("zpm", "ZPM", 8), ("uv", "UV", 9),
]
KINDS = ["item", "fluid", "energy"]
MODES = ["input", "output"]

HEAD = r"src/main/resources/assets/goldentweaks"
DATA = r"src/main/resources/data/goldentweaks"

for kind in KINDS:
    for mode in MODES:
        old = f"gt_port_{kind}_{mode}"
        # The tier-agnostic blockstate/model/loot are replaced by per-tier files.
        for path, suffix in [
            (f"{HEAD}/blockstates/{old}.json", "blockstate"),
            (f"{HEAD}/models/item/{old}.json", "item model"),
            (f"{DATA}/loot_table/blocks/{old}.json", "loot"),
        ]:
            if os.path.exists(path):
                os.remove(path)

        for tier_id, code, level in TIERS:
            name = f"gt_port_{kind}_{mode}_{tier_id}"
            base_model = f"goldentweaks:block/multiblock/{old}_t{level}"

            # --- blockstate: facing only, one model per tier ---
            variants = {}
            for facing, rot in [
                ("north", None), ("east", {"y": 90}), ("south", {"y": 180}),
                ("west", {"y": 270}), ("up", {"x": 270}), ("down", {"x": 90}),
            ]:
                entry = {"model": base_model}
                if rot:
                    entry.update(rot)
                variants[f"facing={facing}"] = entry
            with io.open(f"{HEAD}/blockstates/{name}.json", "w", encoding="utf-8", newline="\n") as h:
                json.dump({"variants": variants}, h, indent=2, ensure_ascii=False)
                h.write("\n")

            # --- item model: inherits the tier's block model ---
            with io.open(f"{HEAD}/models/item/{name}.json", "w", encoding="utf-8", newline="\n") as h:
                json.dump({"parent": base_model}, h, indent=2, ensure_ascii=False)
                h.write("\n")

            # --- loot table ---
            loot = {
                "type": "minecraft:block",
                "pools": [{
                    "rolls": 1,
                    "bonus_rolls": 0,
                    "entries": [{"type": "minecraft:item", "name": f"goldentweaks:{name}"}],
                    "conditions": [{"condition": "minecraft:survives_explosion"}],
                }],
            }
            os.makedirs(f"{DATA}/loot_table/blocks", exist_ok=True)
            with io.open(f"{DATA}/loot_table/blocks/{name}.json", "w", encoding="utf-8", newline="\n") as h:
                json.dump(loot, h, indent=2, ensure_ascii=False)
                h.write("\n")

print("generated", len(KINDS) * len(MODES) * len(TIERS), "resource sets")
