"""Find which link of the casing chain is empty.

The wrapper now runs (1050 models wrapped), but every `getQuads` reports `casing=null`. Three
things could be empty: the entity's field, what `getModelData` returns, or what the model sees in
the data it is handed. Printing all three at the same moment settles it.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''    @Override
    public ModelData getModelData() {
        if (this.casing == null) {
            return ModelData.EMPTY;
        }'''
new = '''    @Override
    public ModelData getModelData() {
        if (DIAG.getAndIncrement() < 8) {
            GoldenTweaks.LOGGER.info("[GT/diag] BE.getModelData at {} field={}",
                    this.worldPosition, this.casing);
        }
        if (this.casing == null) {
            return ModelData.EMPTY;
        }'''
assert old in s
s = s.replace(old, new, 1)

# a counter and the logger import
s = s.replace('''    private static final String TAG_CASING = "Casing";''',
'''    private static final String TAG_CASING = "Casing";

    /** Throttles the diagnostics while the casing chain is being traced. */
    private static final java.util.concurrent.atomic.AtomicInteger DIAG =
            new java.util.concurrent.atomic.AtomicInteger();''', 1)

if "import net.fodoth.skina.goldentweaks.GoldenTweaks;" not in s:
    s = s.replace("package net.fodoth.skina.goldentweaks.content.multiblock;",
                  "package net.fodoth.skina.goldentweaks.content.multiblock;\n\n"
                  "import net.fodoth.skina.goldentweaks.GoldenTweaks;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("entity diagnostic added")

# ---- and the machine side: is it even stamping? ------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

old2 = '''        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!(this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity)) {
                continue;
            }
            if (casingId.equals(entity.casing())) {
                continue;
            }'''
new2 = '''        GoldenTweaks.LOGGER.info("[GT/diag] applyCasingToPorts casing={} ports={}",
                casingId, this.structure.getPorts().size());
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!(this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity)) {
                GoldenTweaks.LOGGER.info("[GT/diag]  no port entity at {}", port.pos());
                continue;
            }
            if (casingId.equals(entity.casing())) {
                continue;
            }'''
assert old2 in s2
s2 = s2.replace(old2, new2, 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("machine diagnostic added")
