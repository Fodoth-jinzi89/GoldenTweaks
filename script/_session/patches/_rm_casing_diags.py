"""Remove the diagnostics now that every link is confirmed working.

The chain is verified end to end: the entity carries the casing, the model receives it, the plate
sprite is swapped and its UVs are carried across. With the stamp now running on a timer, leaving the
log line in would print once every ten seconds.
"""
import io, re

# ---- the machine: drop the stamp log -----------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()
s = s.replace('''
        GoldenTweaks.LOGGER.info("[GT/diag] applyCasingToPorts casing={} written={}",
                casingId, written);
''', "\n")
s = s.replace('''        int written = 0;
''', "", 1)
s = s.replace('''            entity.setCasing(casingId);
            this.restyledPorts.add(pos.immutable());
            written++;''',
'''            entity.setCasing(casingId);
            this.restyledPorts.add(pos.immutable());''', 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("machine diagnostics removed")

# ---- the entity: drop the field log ------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = re.sub(
    r"\n        if \(DIAG\.getAndIncrement\(\) < 8\) \{\n"
    r".*?this\.worldPosition, this\.casing\);\n"
    r"        \}\n", "\n", s2, flags=re.S)
s2 = s2.replace('''
    /** Throttles the diagnostics while the casing chain is being traced. */
    private static final java.util.concurrent.atomic.AtomicInteger DIAG =
            new java.util.concurrent.atomic.AtomicInteger();
''', "")
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("entity diagnostics removed")

# ---- the model: drop the per-quad log -----------------------------------------------------------
p3 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s3 = io.open(p3, encoding="utf-8").read()
s3 = re.sub(
    r"\n        if \(DIAG\.getAndIncrement\(\) < 8\) \{\n"
    r".*?before, after\);\n        \}\n", "\n", s3, flags=re.S)
s3 = s3.replace('''
    /** Throttles the diagnostics: one report per step is enough to tell what is happening. */
    private static final java.util.concurrent.atomic.AtomicInteger DIAG =
            new java.util.concurrent.atomic.AtomicInteger();
''', "")
s3 = s3.replace("''', \"\"\"", "'''")  # guard against stray quoting
io.open(p3, "w", encoding="utf-8", newline="\n").write(s3)
print("model diagnostics removed")

# ---- the baking hook: drop its log ---------------------------------------------------------------
p4 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortModelWrapping.java"
s4 = io.open(p4, encoding="utf-8").read()
s4 = re.sub(r"\n        java\.util\.List<String> samples = new java\.util\.ArrayList<>\(\);.*?models\.size\(\), wrapped, samples\);\n", "\n", s4, flags=re.S)
io.open(p4, "w", encoding="utf-8", newline="\n").write(s4)
print("baking diagnostics removed")
