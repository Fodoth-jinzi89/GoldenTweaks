"""Check that every port blockstate points at a model that exists.

A missing model is the one failure that shows up as an untextured block in game, so this
walks every `gt_port_*` blockstate, reads the models it names, and reports any that are not
on disk. Also checks the reverse: models nothing refers to.

This does not assume a naming scheme. The ports mix two of them -- the original nine-tier
files reuse `_t1.._t9`, while the extended ladder uses `_ulv.._max` -- so the only honest
check is to follow the reference.
"""
import json
import os
import re

ASSETS = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\resources\assets\goldentweaks"


def model_path(ref):
    """Turn a `namespace:block/multiblock/x` reference into the file it means."""
    namespace, _, path = ref.partition(":")
    return os.path.join(ASSETS, "models", path.replace("/", os.sep) + ".json"), namespace


def main():
    states_dir = os.path.join(ASSETS, "blockstates")
    missing = {}
    referenced = set()

    for name in sorted(os.listdir(states_dir)):
        if not name.startswith("gt_port_") or not name.endswith(".json"):
            continue
        with open(os.path.join(states_dir, name), encoding="utf-8") as handle:
            data = json.load(handle)
        for variant in data.get("variants", {}).values():
            ref = variant.get("model")
            if not ref:
                continue
            path, _ = model_path(ref)
            if not path.endswith(".json"):
                path += ".json"
            referenced.add(os.path.normpath(path))
            if not os.path.isfile(path):
                missing.setdefault(ref, []).append(name)

    if missing:
        print("MISSING MODELS (%d distinct):" % len(missing))
        for ref, users in sorted(missing.items()):
            print("  %-58s <- %s%s" % (ref, users[0],
                                       "" if len(users) == 1 else " (+%d more)" % (len(users) - 1)))
    else:
        print("every referenced model exists")

    # Models on disk that no blockstate names. Not an error, but worth knowing after a rename.
    models_dir = os.path.join(ASSETS, "models", "block", "multiblock")
    orphans = []
    for name in sorted(os.listdir(models_dir)):
        if not name.startswith("gt_port_") or not name.endswith(".json"):
            continue
        path = os.path.normpath(os.path.join(models_dir, name))
        if path not in referenced and name != "gt_port_base.json":
            orphans.append(name)

    if orphans:
        print("\nUNREFERENCED MODELS (%d):" % len(orphans))
        for name in orphans[:20]:
            print("  " + name)
        if len(orphans) > 20:
            print("  ... and %d more" % (len(orphans) - 20))

    # The general assemblies, checked explicitly since they are the new set.
    general = [n for n in os.listdir(states_dir) if n.startswith("gt_port_general_")]
    print("\ngeneral assembly blockstates: %d" % len(general))


if __name__ == "__main__":
    main()
