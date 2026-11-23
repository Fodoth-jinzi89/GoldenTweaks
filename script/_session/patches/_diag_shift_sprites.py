"""Report what sprite the shift entry actually resolves to.

A shift entry is built by looking its two textures up in the block atlas. If either is not stitched, the
lookup yields the missing sprite and every shifted quad is drawn with the black-and-magenta checker.
Printing the resolved names is the difference between knowing and guessing.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingBehaviour.java"
s = io.open(p, encoding="utf-8").read()

old = '''    private static CTSpriteShiftEntry shiftFor(Block casing) {
        ResourceLocation sheet = sheetOf(casing);
        if (sheet == null) {
            return null;
        }
        return SHIFTS.computeIfAbsent(sheet, key -> {
            // Create's CT sheets follow `<namespace>:block/<name>` for the base and
            // `<namespace>:block/<name>_ctm` for the connected one, which is the layout its
            // CTSpriteShifter expects.
            ResourceLocation connected = ResourceLocation.fromNamespaceAndPath(
                    key.getNamespace(), key.getPath() + "_ctm");
            return CTSpriteShifter.getCT(TYPE, key, connected);
        });
    }'''
new = '''    private static CTSpriteShiftEntry shiftFor(Block casing) {
        ResourceLocation sheet = sheetOf(casing);
        if (sheet == null) {
            return null;
        }
        return SHIFTS.computeIfAbsent(sheet, GTPortCasingBehaviour::buildShift);
    }

    /** Builds one shift entry, reporting what the atlas gave back for both sheets. */
    private static CTSpriteShiftEntry buildShift(ResourceLocation sheet) {
        ResourceLocation connected = ResourceLocation.fromNamespaceAndPath(
                sheet.getNamespace(), sheet.getPath() + "_ctm");
        CTSpriteShiftEntry entry = CTSpriteShifter.getCT(TYPE, sheet, connected);

        if (DIAG.getAndIncrement() < 4) {
            GoldenTweaks.LOGGER.info(
                    "[GT/diag] ct shift from={} -> {}   to={} -> {}",
                    sheet, entry.getOriginal() == null ? "NULL" : entry.getOriginal().contents().name(),
                    connected, entry.getTarget() == null ? "NULL" : entry.getTarget().contents().name());
        }
        return entry;
    }'''
assert old in s
s = s.replace(old, new, 1)

if "DIAG" not in s.split("buildShift")[0]:
    s = s.replace('''    private static final Map<ResourceLocation, CTSpriteShiftEntry> SHIFTS = new ConcurrentHashMap<>();''',
'''    private static final Map<ResourceLocation, CTSpriteShiftEntry> SHIFTS = new ConcurrentHashMap<>();

    /** Throttles the diagnostic that reports what the atlas resolved. */
    private static final java.util.concurrent.atomic.AtomicInteger DIAG =
            new java.util.concurrent.atomic.AtomicInteger();''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("shift resolution diagnostic added")
