"""Build the shift entries only once the block atlas exists.

The diagnostic settled it: both sheets are in the atlas, yet `CTSpriteShifter.getCT` returned entries
whose sprites are both `null`. The entry resolves its textures by looking them up in the block atlas,
and it was being built on first render — which for a machine can be before the atlas finished
stitching. A `null` lookup is baked into the entry, so it stays null for the rest of the session, and
every shifted quad draws the missing sprite.

The entries are therefore built from `TextureAtlasStitchedEvent`, where the atlas is complete, and the
build is repeated on every stitch so a resource reload picks up new artwork.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingBehaviour.java"
s = io.open(p, encoding="utf-8").read()

old = '''    private static CTSpriteShiftEntry shiftFor(Block casing) {
        ResourceLocation sheet = sheetOf(casing);
        if (sheet == null) {
            return null;
        }
        return SHIFTS.computeIfAbsent(sheet, GTPortCasingBehaviour::buildShift);
    }'''
new = '''    private static CTSpriteShiftEntry shiftFor(Block casing) {
        ResourceLocation sheet = sheetOf(casing);
        return sheet == null ? null : SHIFTS.get(sheet);
    }

    /**
     * Rebuilds every casing's shift entry now that the block atlas is complete.
     *
     * <p>Called from the atlas stitch event rather than lazily on first render: the entry resolves its
     * textures by id against the atlas, and a lookup made before stitching yields {@code null}, which
     * the entry then keeps. Building here means the sprites are always the real ones.</p>
     *
     * <p>Run on every stitch, so a resource reload replaces the entries instead of keeping the ones
     * built from the previous pack.</p>
     */
    public static void rebuildShifts() {
        for (var casing : GTMultiblockBlocks.CASINGS) {
            ResourceLocation sheet = sheetOf(casing.get());
            if (sheet != null) {
                SHIFTS.put(sheet, buildShift(sheet));
            }
        }
    }'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("shifts are built from the stitch event")

# ---- the stitch hook -------------------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingDiagnostics.java"
s2 = io.open(p2, encoding="utf-8").read()

old2 = '''    @SubscribeEvent
    public static void onStitched(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location() != TextureAtlas.LOCATION_BLOCKS) {
            return;
        }
        report(event.getAtlas(), SHEET);
        report(event.getAtlas(), CT);
    }'''
new2 = '''    @SubscribeEvent
    public static void onStitched(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location() != TextureAtlas.LOCATION_BLOCKS) {
            return;
        }
        // The atlas is complete here, which is the only moment a texture lookup is guaranteed to find
        // its sprite. Building the shifts lazily on first render produced entries with null sprites.
        GTPortCasingBehaviour.rebuildShifts();
    }'''
assert old2 in s2
s2 = s2.replace(old2, new2, 1)

# the per-sheet report and its constants go with it
start = s2.find('''    private static final ResourceLocation SHEET =''')
end = s2.find('''    private GTPortCasingDiagnostics() {''')
s2 = s2[:start] + s2[end:]

start2 = s2.find('''    private static void report(TextureAtlas atlas, ResourceLocation id) {''')
end2 = s2.rindex("}")
s2 = s2[:start2] + "}\n"

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("stitch hook now rebuilds the shifts")
