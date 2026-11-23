"""Finish the advanced kettle's registrations.

Three loose ends: the block must be mineable with a pickaxe, it must appear in the creative tab,
and the structural casing check counts the right block. The last one is already handled by the
entity override; these two are data and tab entries.
"""
import io
import json

# ---------------- pickaxe tag ----------------
path = r"src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json"
with io.open(path, encoding="utf-8") as handle:
    data = json.load(handle)
entry = "goldentweaks:advanced_chemical_plant_controller"
if entry not in data["values"]:
    data["values"].append(entry)
with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
    json.dump(data, handle, indent=2, ensure_ascii=False)
    handle.write("\n")
print("pickaxe:", len(data["values"]), "entries")

# ---------------- creative tab ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockCreativeTabs.java"
s = io.open(p, encoding="utf-8").read()
s = s.replace('''                        acceptExternal(output, "goldentweaks:chemical_plant_controller");''',
'''                        acceptExternal(output, "goldentweaks:chemical_plant_controller");
                        acceptExternal(output, "goldentweaks:advanced_chemical_plant_controller");''', 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("creative tab updated")
