"""Reconcile the block tag lists after the IO assemblies changed.

Removes the four retired assembly blocks and adds the general assemblies, both to the
shared port tag and to the pickaxe tag. Also drops them from `vacuum_cooler_casing`: that
structure is an early-game machine, and a tiered assembly is not one of the parts it takes.

Entries are appended in tier order so the file stays readable.
"""
import json
import os

ROOT = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\resources"

TIERS = ["ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv",
         "uhv", "uev", "uiv", "umv", "uxv", "max"]

RETIRED = [
    "goldentweaks:input_bus",
    "goldentweaks:auto_input_bus",
    "goldentweaks:output_bus",
    "goldentweaks:input_catalyst",
]


def new_assemblies():
    ids = []
    for mode in ("input", "output"):
        for tier in TIERS:
            ids.append("goldentweaks:gt_port_general_%s_%s" % (mode, tier))
    return ids


def load(path):
    with open(path, encoding="utf-8") as handle:
        return json.load(handle)


def save(path, data):
    with open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def main():
    # The shared port tag gains the assemblies.
    path = os.path.join(ROOT, r"data\goldentweaks\tags\block\multiblock\ports.json")
    data = load(path)
    values = [v for v in data["values"] if v not in RETIRED]
    values.extend(new_assemblies())
    data["values"] = values
    save(path, data)
    print("ports.json:", len(values), "values")

    # The pickaxe tag only has to know how to mine them.
    path = os.path.join(ROOT, r"data\minecraft\tags\block\mineable\pickaxe.json")
    data = load(path)
    values = [v for v in data["values"] if v not in RETIRED]
    values.extend(new_assemblies())
    data["values"] = values
    save(path, data)
    print("pickaxe.json:", len(values), "values")

    # The vacuum cooler takes no tiered assembly, so this list only loses the retired adds.
    path = os.path.join(ROOT, r"data\goldentweaks\tags\block\multiblock\vacuum_cooler_casing.json")
    data = load(path)
    values = [v for v in data["values"] if v not in RETIRED]
    data["values"] = values
    save(path, data)
    print("vacuum_cooler_casing.json:", len(values), "values")


if __name__ == "__main__":
    main()
