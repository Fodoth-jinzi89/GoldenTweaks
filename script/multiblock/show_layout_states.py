"""Report the connection state each cell of the test casings stands for.

The test casings were cut from the octo-directional sheet by an earlier script. Checking them against
the current index formulas is how to tell art that is stale from a formula that is wrong — the two look
identical when you only watch the game draw.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from ctm import INDEX_OF, cell_states, contexts

SIDES = ("up", "down", "left", "right")


def describe(ctx):
    return " ".join(f"{side}={'Y' if ctx[side] else 'n'}" for side in SIDES)


def main(argv):
    name = (argv[0] if argv else "CROSS").upper()
    index_of = INDEX_OF[name]

    table = cell_states(name)
    print(f"  {name}: {len(table)} cells")

    # Two checks matter. First, is the index a bijection — does every combination get its own cell?
    seen = {}
    for ctx in contexts():
        seen.setdefault(index_of(ctx), []).append(ctx)
    collisions = {i: cs for i, cs in seen.items() if len(cs) > 1}
    print(f"  cells reached more than once: {len(collisions)}")

    limit = int(argv[1]) if len(argv) > 1 else len(table)
    for index in sorted(table)[:limit]:
        ctx = table[index]
        also = " (+%d more)" % (len(collisions[index]) - 1) if index in collisions else ""
        print(f"    {index:3d}: {describe(ctx)}{also}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
