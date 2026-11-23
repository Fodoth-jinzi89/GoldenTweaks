"""Generate shell_pp's blockstate, models, loot table, texture and language entry."""
import io
import json
import os
import shutil

ROOT = r"src/main/resources"
ARTS = r"arts/multiblock"

# The plain texture: the CTM definition matches on this path, so the block model points here and
# Continuity substitutes the connected tiles at render time.
shutil.copyfile(os.path.join(ARTS, "shell_pp.png"),
                os.path.join(ROOT, "assets/goldentweaks/textures/block/shell_pp.png"))

def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")

write(os.path.join(ROOT, "assets/goldentweaks/blockstates/shell_pp.json"),
      {"variants": {"": {"model": "goldentweaks:block/shell_pp"}}})

write(os.path.join(ROOT, "assets/goldentweaks/models/block/shell_pp.json"),
      {"parent": "minecraft:block/cube_all",
       "textures": {"all": "goldentweaks:block/shell_pp"}})

write(os.path.join(ROOT, "assets/goldentweaks/models/item/shell_pp.json"),
      {"parent": "goldentweaks:block/shell_pp"})

write(os.path.join(ROOT, "data/goldentweaks/loot_table/blocks/shell_pp.json"),
      {"type": "minecraft:block",
       "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
                  "entries": [{"type": "minecraft:item", "name": "goldentweaks:shell_pp"}],
                  "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

# Language: the PE entry calls it a casing, so the PP one follows the same wording.
for name, text in (("zh_cn", "聚丙烯机壳"), ("en_us", "PP Casing")):
    path = os.path.join(ROOT, "assets/goldentweaks/lang/%s.json" % name)
    with io.open(path, encoding="utf-8") as handle:
        data = json.load(handle)
    data["block.goldentweaks.shell_pp"] = text
    with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")

# Tags: the pickaxe tag decides the drop, and the casing tag lets the structure use it.
for name, entry in (("minecraft/tags/block/mineable/pickaxe.json", "goldentweaks:shell_pp"),
                    ("goldentweaks/tags/block/multiblock/casing.json", "goldentweaks:shell_pp")):
    path = os.path.join(ROOT, "data/%s" % name)
    with io.open(path, encoding="utf-8") as handle:
        data = json.load(handle)
    if entry not in data["values"]:
        data["values"].append(entry)
    with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")

print("shell_pp resources written")
