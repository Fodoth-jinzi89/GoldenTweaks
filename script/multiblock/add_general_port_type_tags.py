"""Add the general assemblies to the port-type tags the structure scan reads.

A port is recognised by AMI purely through these tags: `MultiblockPortType.matchesInput`
is `state.is(inputTag)` and nothing else. So a block that is not listed here is not a port
as far as the machine is concerned, however capable it is.

The assembly holds every kind, so it is listed under all three of the builtin kinds for
both directions. Chemical and essentia are left alone: those tags belong to Mekanism and
Thaumcraft, and their own port types are registered against them.
"""
import json
import os

ROOT = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\resources"
TAGS = os.path.join(ROOT, r"data\c\tags\block\multiblock")

TIERS = ["ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv",
         "uhv", "uev", "uiv", "umv", "uxv", "max"]

# The kinds the assembly can serve whose tags are ours to edit.
BUILTIN = ["item", "fluid", "energy"]


def main():
    for direction in ("input", "output"):
        assemblies = ["goldentweaks:gt_port_general_%s_%s" % (direction, tier)
                      for tier in TIERS]
        for kind in BUILTIN:
            path = os.path.join(TAGS, direction, kind + ".json")
            with open(path, encoding="utf-8") as handle:
                data = json.load(handle)
            values = data.get("values", [])
            for id_ in assemblies:
                if id_ not in values:
                    values.append(id_)
            data["values"] = values
            with open(path, "w", encoding="utf-8", newline="\n") as handle:
                json.dump(data, handle, indent=2, ensure_ascii=False)
                handle.write("\n")
            print("%s/%s.json: %d values" % (direction, kind, len(values)))


if __name__ == "__main__":
    main()
