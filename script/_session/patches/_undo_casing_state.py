"""Undo the per-casing model variants, and drop the state property that existed for them.

Registering a model per casing is 150 blocks per casing. Create's `CTModel` solves the same problem
with one model: a behaviour object decides connectivity per face at render time, so the casing does
not have to be enumerable in the block state at all.

The casing returns to the block entity, where it can name anything.
"""
import io, json, os

STATES = "src/main/resources/assets/goldentweaks/blockstates"

# ---- 1. plain blockstates ----------------------------------------------------------------------
ROTATION = {
    "north": {},
    "east": {"y": 90},
    "south": {"y": 180},
    "west": {"y": 270},
    "up": {"x": 270},
    "down": {"x": 90},
}

written = 0
for name in sorted(os.listdir(STATES)):
    if not (name.startswith("gt_port_") and name.endswith(".json")):
        continue
    port = name[:-len(".json")]
    variants = {}
    for facing, rotation in ROTATION.items():
        entry = {"model": f"goldentweaks:block/multiblock/{port}"}
        entry.update(rotation)
        variants[f"facing={facing}"] = entry

    with open(os.path.join(STATES, name), "w", encoding="utf-8", newline="\n") as out:
        json.dump({"variants": variants}, out, indent=2, ensure_ascii=False)
        out.write("\n")
    written += 1
print("blockstates reverted:", written)

# ---- 2. delete the variant registration --------------------------------------------------------
removed = os.path.join(r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client",
                       "GTPortModelWrapping.java")
if os.path.exists(removed):
    os.remove(removed)
    print("model-wrapping class removed")

# ---- 3. the block: drop the casing state property ------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java"
s = io.open(p, encoding="utf-8").read()

start = s.find("    /**\n     * Which casing this port's plate is drawn with,")
if start >= 0:
    end = s.index("    private final GTPortKind kind;")
    s = s[:start] + s[end:]

s = s.replace('''        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CASING, 0));''',
'''        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));''', 1)

s = s.replace("        builder.add(FACING, CASING);", "        builder.add(FACING);", 1)

start2 = s.find("    /**\n     * The casing block a state's plate is drawn with.")
if start2 >= 0:
    end2 = s.index("    /** The voltage tier this block belongs to. */")
    s = s[:start2] + s[end2:]

s = s.replace("import net.minecraft.world.level.block.state.properties.IntegerProperty;\n", "", 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("block casing property removed")
