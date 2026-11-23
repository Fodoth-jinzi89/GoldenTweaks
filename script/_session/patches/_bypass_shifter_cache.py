"""Build the shift entry directly, and rebuild it until the atlas can answer.

`CTSpriteShifter.getCT` caches its entry the first time it is called:

    if (ENTRY_CACHE.containsKey(key)) return ENTRY_CACHE.get(key);
    entry = new CTSpriteShiftEntry(type);
    if (isClient) entry.set(from, to);
    ENTRY_CACHE.put(key, entry);

The `set` that resolves the sprites therefore runs exactly once. Building too early — before
`Minecraft.getInstance().getTextureAtlas()` reports the stitched atlas, which is what `set` looks
through — bakes two null sprites into a cached entry, and every later call hands that same entry back.
That is why rebuilding on the stitch event changed nothing: it kept retrieving the poisoned instance.

The entry is now constructed here instead, and `set` is re-run until both sprites resolve. Once they
do, the entry is kept; until then the caller falls back to the authored plate, so a half-loaded atlas
never draws as the missing-sprite checker.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingBehaviour.java"
s = io.open(p, encoding="utf-8").read()

old = '''    private static CTSpriteShiftEntry shiftFor(Block casing) {
        ResourceLocation sheet = sheetOf(casing);
        return sheet == null ? null : SHIFTS.get(sheet);
    }'''
new = '''    /**
     * The shift entry for a casing, or {@code null} while the atlas cannot resolve it.
     *
     * <p>Built here rather than through {@code CTSpriteShifter.getCT}: that helper caches its entry on
     * the first call and never re-runs the texture lookup, so an entry created before the atlas was
     * readable keeps two null sprites for the rest of the session. Constructing the entry directly lets
     * the lookup be retried.</p>
     *
     * <p>A successful entry is kept; an unsuccessful one is not, so the next render tries again. The
     * caller treats {@code null} as "draw the authored plate", which is why a half-loaded atlas shows
     * the plain port instead of the missing-texture checker.</p>
     */
    private static CTSpriteShiftEntry shiftFor(Block casing) {
        ResourceLocation sheet = sheetOf(casing);
        if (sheet == null) {
            return null;
        }

        CTSpriteShiftEntry cached = SHIFTS.get(sheet);
        if (cached != null) {
            return cached;
        }

        CTSpriteShiftEntry entry = buildShift(sheet);
        if (entry == null) {
            return null;
        }
        SHIFTS.put(sheet, entry);
        return entry;
    }'''
assert old in s
s = s.replace(old, new, 1)

old2 = '''    /** Builds one shift entry, reporting what the atlas gave back for both sheets. */
    private static CTSpriteShiftEntry buildShift(ResourceLocation sheet) {
        ResourceLocation connected = ResourceLocation.fromNamespaceAndPath(
                sheet.getNamespace(), sheet.getPath() + "_ctm");
        CTSpriteShiftEntry entry = CTSpriteShifter.getCT(TYPE, sheet, connected);
        return entry;
    }'''
new2 = '''    /**
     * Builds one shift entry, or {@code null} when the atlas cannot resolve both sheets yet.
     *
     * <p>Bypasses {@code CTSpriteShifter.getCT}, whose cache makes a failed lookup permanent. See
     * {@link #shiftFor}.</p>
     */
    private static CTSpriteShiftEntry buildShift(ResourceLocation sheet) {
        ResourceLocation connected = ResourceLocation.fromNamespaceAndPath(
                sheet.getNamespace(), sheet.getPath() + "_ctm");

        CTSpriteShiftEntry entry = new CTSpriteShiftEntry(TYPE);
        entry.set(sheet, connected);

        return isUsable(entry) ? entry : null;
    }'''
assert old2 in s
s = s.replace(old2, new2, 1)

# rebuildShifts no longer needs its own reporting; the build now reports on success
old3 = s[s.find("    public static void rebuildShifts() {"):]
old3 = old3[:old3.index("\n    }\n") + len("\n    }\n")]
new3 = '''    public static void rebuildShifts() {
        for (var casing : GTMultiblockBlocks.CASINGS) {
            ResourceLocation sheet = sheetOf(casing.get());
            if (sheet != null) {
                SHIFTS.remove(sheet);
            }
        }
    }
'''
s = s.replace(old3, new3, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("shift entries are built directly and retried")
