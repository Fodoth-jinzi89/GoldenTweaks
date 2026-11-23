"""Add the new tiers to the tag files.

Three groups need them: the six port-kind tags (so the structure scan recognises the blocks
as ports), the pickaxe tag (so they drop when mined), and the creative tab already walks the
registry so it needs nothing.
"""
import io
import json

ROOT = r"src/main/resources/data"
KINDS = ["item", "fluid", "energy"]
MODES = ["input", "output"]
NEW = ["uhv", "uev", "uiv", "umv", "uxv", "max"]


def load(path):
    with io.open(path, encoding="utf-8") as handle:
        return json.load(handle)


def save(path, data):
    with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def main():
    # ---- the six multiblock port tags ----
    for mode in MODES:
        for kind in KINDS:
            path = "%s/c/tags/block/multiblock/%s/%s.json" % (ROOT, mode, kind)
            data = load(path)
            values = data["values"]
            for tier in NEW:
                entry = "goldentweaks:gt_port_%s_%s_%s" % (kind, mode, tier)
                if entry not in values:
                    values.append(entry)
            save(path, data)
            print("%s/%s: %d entries" % (mode, kind, len(values)))

    # ---- the pickaxe tag, so the new blocks actually drop ----
    path = "%s/minecraft/tags/block/mineable/pickaxe.json" % ROOT
    data = load(path)
    values = data["values"]
    added = 0
    for kind in KINDS:
        for mode in MODES:
            for tier in NEW:
                entry = "goldentweaks:gt_port_%s_%s_%s" % (kind, mode, tier)
                if entry not in values:
                    values.append(entry)
                    added += 1
    save(path, data)
    print("pickaxe: +%d, total %d" % (added, len(values)))


main()
