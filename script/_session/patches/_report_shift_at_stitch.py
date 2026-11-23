"""Report what the rebuilt shifts resolved to, once per stitch.

The previous run proved the sheets are in the atlas while the entries held null sprites, so the fix is
timing. One line at stitch time confirms the entries now hold the real sprites, and would show at once
if a future reload regressed.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingBehaviour.java"
s = io.open(p, encoding="utf-8").read()

old = '''    public static void rebuildShifts() {
        for (var casing : GTMultiblockBlocks.CASINGS) {
            ResourceLocation sheet = sheetOf(casing.get());
            if (sheet != null) {
                SHIFTS.put(sheet, buildShift(sheet));
            }
        }
    }'''
new = '''    public static void rebuildShifts() {
        for (var casing : GTMultiblockBlocks.CASINGS) {
            ResourceLocation sheet = sheetOf(casing.get());
            if (sheet == null) {
                continue;
            }
            CTSpriteShiftEntry entry = buildShift(sheet);
            SHIFTS.put(sheet, entry);
            GoldenTweaks.LOGGER.info("[GT] ct shift {} -> original={} target={}",
                    sheet,
                    entry.getOriginal() == null ? "NULL" : entry.getOriginal().contents().name(),
                    entry.getTarget() == null ? "NULL" : entry.getTarget().contents().name());
        }
    }'''
assert old in s
s = s.replace(old, new, 1)

# the one-off diagnostic inside buildShift is no longer needed
import re
s = re.sub(
    r"\n        if \(DIAG\.getAndIncrement\(\) < 4\) \{\n"
    r".*?entry\.getTarget\(\)\.contents\(\)\.name\(\)\);\n        \}\n", "\n", s, flags=re.S)

s = s.replace('''
    /** Throttles the diagnostic that reports what the atlas resolved. */
    private static final java.util.concurrent.atomic.AtomicInteger DIAG =
            new java.util.concurrent.atomic.AtomicInteger();
''', "")

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("stitch-time report added")
