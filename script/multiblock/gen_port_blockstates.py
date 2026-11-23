"""Regenerate the port blockstates with a casing index, keeping each one's own model.

The blockstate name and the model name are not the same: `gt_port_item_input_lv` is backed by
`gt_port_item_input_t1`. Deriving the model from the blockstate's file name therefore pointed every
port at a model that does not exist, and Minecraft substituted its missing-model placeholder — the
black-and-magenta checker. Hopper and canister ports happened to have matching names, which is why they
looked correct while every bus and hatch did not.

The existing model reference is read from the blockstate itself and carried through. Only the casing
index is added.

    python script/multiblock/gen_port_blockstates.py 2
"""
import json
import os
import sys

STATES = "src/main/resources/assets/goldentweaks/blockstates"

FACINGS = ("north", "east", "south", "west", "up", "down")

ROTATION = {
    "north": {},
    "east": {"y": 90},
    "south": {"y": 180},
    "west": {"y": 270},
    "up": {"x": 270},
    "down": {"x": 90},
}


def model_of(variants):
    """The model a port's blockstate points at, taken from any of its variants."""
    for entry in variants.values():
        model = entry.get("model")
        if model:
            return model
    return None


def main(argv):
    casings = int(argv[0]) if argv else 2
    if casings < 1:
        print("casing count must be at least 1", file=sys.stderr)
        return 1

    ports = sorted(
        name[:-len(".json")]
        for name in os.listdir(STATES)
        if name.startswith("gt_port_") and name.endswith(".json")
    )
    if not ports:
        print("no port blockstates found", file=sys.stderr)
        return 1

    written = 0
    skipped = []
    for port in ports:
        path = os.path.join(STATES, port + ".json")
        with open(path, encoding="utf-8") as handle:
            existing = json.load(handle).get("variants", {})

        model = model_of(existing)
        if model is None:
            skipped.append(port)
            continue

        variants = {}
        for casing in range(casings):
            for facing in FACINGS:
                entry = {"model": model}
                entry.update(ROTATION[facing])
                variants[f"casing={casing},facing={facing}"] = entry

        with open(path, "w", encoding="utf-8", newline="\n") as handle:
            json.dump({"variants": variants}, handle, indent=2, ensure_ascii=False)
            handle.write("\n")
        written += 1

    print(f"blockstates written: {written}, casings: {casings}")
    if skipped:
        print(f"  skipped (no model reference): {skipped}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
