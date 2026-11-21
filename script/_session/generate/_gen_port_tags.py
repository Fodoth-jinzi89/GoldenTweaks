import io, json, os

TIER_IDS = ["ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv"]

# Six tag files, one per kind and mode, all under the shared multiblock tag namespace.
for kind in ["item", "fluid", "energy"]:
    for mode in ["input", "output"]:
        path = f"src/main/resources/data/c/tags/block/multiblock/{mode}/{kind}.json"
        data = json.load(io.open(path, encoding="utf-8"))
        # Every tier's block now stands on its own, so the tag has to name all nine.
        data["values"] = [f"goldentweaks:gt_port_{kind}_{mode}_{t}" for t in TIER_IDS]
        with io.open(path, "w", encoding="utf-8", newline="\n") as h:
            json.dump(data, h, indent=2, ensure_ascii=False)
            h.write("\n")
        print("tag updated:", path, "->", len(data["values"]), "entries")
