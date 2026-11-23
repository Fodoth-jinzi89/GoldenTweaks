"""Keep shell_pp out of the reactor's casing tag.

The tag decides whether the shape assembles; the machine then counts real PE casings and
refuses the build if there are fewer than eight. A tag that accepts PP would let a PP-only
frame assemble and then fail that count, which reads to the player as a broken recipe rather
than as an unsupported material.

So PP sits outside the tag until the reactor is meant to accept it. It is still a normal
block with a normal drop; it simply does not take part in this machine yet.
"""
import io
import json

path = r"src/main/resources/data/goldentweaks/tags/block/multiblock/casing.json"
with io.open(path, encoding="utf-8") as handle:
    data = json.load(handle)

before = list(data["values"])
data["values"] = [v for v in data["values"] if v != "goldentweaks:shell_pp"]
after = data["values"]

with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
    json.dump(data, handle, indent=2, ensure_ascii=False)
    handle.write("\n")

print("casing tag:", before, "->", after)
