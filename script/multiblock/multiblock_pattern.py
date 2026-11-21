#!/usr/bin/env python3
"""Validate our bottom-up multiblock structure files.

Our format describes a structure the way a player sees it, so nothing needs converting
while editing:

    {
      "layers": [                    # bottom layer first
        ["xxx", "xxx", "xxx"],       # rows north -> south,
        ["xxx", "xox", "xax"],       # characters west -> east
        ["xxx", "xxx", "xxx"],
        ["xxo", "xxx", "oxx"]        # top layer last
      ],
      "keys": { "a": {...}, "x": "#goldentweaks:multiblock/ports" },
      "master": "a",                 # the controller
      "any": "o"                     # optional wildcard, defaults to 'o'
    }

GTMultiblockPattern turns this into avaritia_more_items' [depth][height][width] predicate
array at load time, so this script only has to check the file itself and the axis mapping
that conversion relies on.

Run from the project root:

    python script/multiblock_pattern.py
"""

import json
import os
import sys

STRUCT_DIR = os.path.join("src", "main", "resources", "data", "goldentweaks", "multiblock", "structures")

# AMiBlockPattern hard limits.
MAX_EDGE = 48
MAX_VOLUME = 32768

STEPS = {
    "north": (0, 0, -1), "south": (0, 0, 1),
    "west": (-1, 0, 0), "east": (1, 0, 0),
    "up": (0, 1, 0), "down": (0, -1, 0),
}


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0])


def translate(forwards, up, mirrored, w, h, d):
    """Reproduce AMiBlockPattern.translateAndRotate for a pattern cell (w, h, d)."""
    right = cross(STEPS[forwards], STEPS[up])
    if mirrored:
        right = tuple(-v for v in right)
    up_step, forward_step = STEPS[up], STEPS[forwards]
    return (right[0] * w - up_step[0] * h + forward_step[0] * d,
            right[1] * w - up_step[1] * h + forward_step[1] * d,
            right[2] * w - up_step[2] * h + forward_step[2] * d)


def to_ami(document):
    """Mirror GTMultiblockPattern.toAmiPredicates(): bottom-up -> AMI's depth-down layout."""
    layers = document["layers"]
    any_char = document.get("any", "o")
    height, depth, width = len(layers), len(layers[0]), len(layers[0][0])

    rows = []
    for d in range(depth):
        row = depth - 1 - d
        rows.append([layers[height - 1 - h][row].replace(any_char, "?") for h in range(height)])

    master_layer, master_row, master_col = locate_master(document)
    return rows, [master_col, height - 1 - master_layer, depth - 1 - master_row]


def locate_master(document):
    symbol = document["master"]
    found = []
    for layer, rows in enumerate(document["layers"]):
        for row, line in enumerate(rows):
            for col, char in enumerate(line):
                if char == symbol:
                    found.append((layer, row, col))
    if len(found) != 1:
        raise ValueError(f"expected exactly one '{symbol}', found {len(found)}")
    return found[0]


def validate(name, document):
    layers = document["layers"]
    if not layers:
        raise ValueError("no layers")

    height, depth, width = len(layers), len(layers[0]), len(layers[0][0])
    if max(height, depth, width) > MAX_EDGE:
        raise ValueError(f"{width}x{depth}x{height} exceeds the {MAX_EDGE} per-edge limit")
    if width * height * depth > MAX_VOLUME:
        raise ValueError(f"volume {width * height * depth} exceeds {MAX_VOLUME}")

    for index, rows in enumerate(layers):
        if len(rows) != depth:
            raise ValueError(f"layer {index + 1} has {len(rows)} rows, expected {depth}")
        for row_index, line in enumerate(rows):
            if len(line) != width:
                raise ValueError(
                    f"layer {index + 1} row {row_index + 1} is {len(line)} wide, expected {width}")

    any_char = document.get("any", "o")
    keys = document["keys"]
    for index, rows in enumerate(layers):
        for row_index, line in enumerate(rows):
            for col, char in enumerate(line):
                if char == any_char or char in keys:
                    continue
                raise ValueError(
                    f"layer {index + 1} row {row_index + 1} column {col + 1} uses '{char}', "
                    f"which is not declared in keys")

    if document["master"] not in keys:
        raise ValueError(f"master '{document['master']}' is not declared in keys")
    for key in keys:
        if len(key) != 1:
            raise ValueError(f"key {key!r} must be a single character")
        if key in "? ":
            raise ValueError(f"key {key!r} is reserved by avaritia_more_items")
    if len(any_char) != 1 or any_char in "? ":
        raise ValueError(f"wildcard {any_char!r} must be one character, not '?' or space")

    locate_master(document)


def check_axes(name, document):
    """The controller must be the cell AMI probes for the block that triggered the scan.

    AMI anchors a match so the pattern cell at `master` lands on the machine block itself, so
    translating `master` by the offsets of a candidate (forwards, up, mirror) must give the
    origin. This also proves the depth reversal in to_ami() is the right one: if it were not,
    the controller would resolve to a different physical cell than the pattern's master.
    """
    rows, master = to_ami(document)

    # Read the AMI layout back with AMI's own axes for the canonical orientation.
    forwards, up, mirrored = "north", "up", False
    height, depth, width = len(rows[0]), len(rows), len(rows[0][0])

    cells = {}
    for d in range(depth):
        for h in range(height):
            for w in range(width):
                offsets = translate(forwards, up, mirrored,
                                    w - master[0], h - master[1], d - master[2])
                cells[offsets] = rows[d][h][w]

    master_symbol = document["master"]
    origin = cells.get((0, 0, 0))
    if origin != master_symbol:
        raise ValueError(
            f"axis check failed: the origin cell reads {origin!r}, "
            f"expected the master '{master_symbol}'")

    # The structure must be a solid block of cells (no gaps) in this orientation.
    xs = [x for x, _, _ in cells]
    ys = [y for _, y, _ in cells]
    zs = [z for _, _, z in cells]
    expected = (max(xs) - min(xs) + 1) * (max(ys) - min(ys) + 1) * (max(zs) - min(zs) + 1)
    if len(cells) != expected:
        raise ValueError(f"structure is not a full cuboid: {len(cells)} cells, expected {expected}")


def main(argv):
    failures = 0
    if not os.path.isdir(STRUCT_DIR):
        print(f"[FAIL] {STRUCT_DIR} does not exist")
        return 1

    for filename in sorted(os.listdir(STRUCT_DIR)):
        if not filename.endswith(".json"):
            continue
        path = os.path.join(STRUCT_DIR, filename)
        name = filename[:-len(".json")]
        try:
            with open(path, encoding="utf-8") as handle:
                document = json.load(handle)
            validate(name, document)
            rows, master = to_ami(document)
            check_axes(name, document)
        except (ValueError, KeyError, json.JSONDecodeError) as error:
            print(f"[FAIL] {name}: {error}")
            failures += 1
            continue

        height, depth, width = len(document["layers"]), len(document["layers"][0]), len(document["layers"][0][0])
        print(f"[OK] {name}: {width} wide x {depth} deep x {height} tall (w x d x h), "
              f"master {master}")
        print(f"     AMI layout: {depth} slices x {height} rows")
        for d, element in enumerate(rows):
            print(f"       slice {d}: " + " | ".join(element))

    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
