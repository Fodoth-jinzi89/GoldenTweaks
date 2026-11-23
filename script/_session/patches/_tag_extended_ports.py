"""Tag the new ports so the structure scan and the pickaxe recognise them.

Two tag families need them: `c:multiblock/<mode>/<kind>` decides which port the scan sees, and
`minecraft:mineable/pickaxe` decides whether the block drops at all.
"""
import io
import json
import os

ROOT = r"src/main/resources/data"
TIERS = ["ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv",
         "uhv", "uev", "uiv", "umv", "uxv", "max"]
KINDS = ["chemical", "essentia"]
MODES = ["input", "output"]


def load(path):
    # The two new kinds have no tag file yet; start from an empty list rather than failing.
    if not os.path.exists(path):
        return {"replace": False, "values": []}
    with io.open(path, encoding="utf-8") as handle:
        return json.load(handle)


def save(path, data):
    with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def main():
    for mode in MODES:
        for kind in KINDS:
            path = "%s/c/tags/block/multiblock/%s/%s.json" % (ROOT, mode, kind)
            data = load(path)
            values = data["values"]
            for tier in TIERS:
                entry = "goldentweaks:gt_port_%s_%s_%s" % (kind, mode, tier)
                if entry not in values:
                    values.append(entry)
            save(path, data)
            print("%s/%s: %d entries" % (mode, kind, len(values)))

    path = "%s/minecraft/tags/block/mineable/pickaxe.json" % ROOT
    data = load(path)
    values = data["values"]
    added = 0
    for kind in KINDS:
        for mode in MODES:
            for tier in TIERS:
                entry = "goldentweaks:gt_port_%s_%s_%s" % (kind, mode, tier)
                if entry not in values:
                    values.append(entry)
                    added += 1
    save(path, data)
    print("pickaxe: +%d, total %d" % (added, len(values)))


main()
