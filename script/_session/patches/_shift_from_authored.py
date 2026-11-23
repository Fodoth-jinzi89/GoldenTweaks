"""The shift must start from the sprite the quad actually uses.

`CTModel` applies a shift only when the quad's own sprite is the entry's `original`:

    if (quad.getSprite() == shift.getOriginal()) { ... remap ... }

Every port model draws its plate with `shell_pe`, whatever casing it is set into — the casing is a
state value, not a texture the model names. So an entry built from the *casing's* sheet has
`original = shell_pp`, never matches the quad, and the plate is left alone.

The entry is therefore built from the sprite that is actually on the quad, and points at the casing's
sheet. That is the same relationship Create's own blocks rely on: author the plain texture, shift it
to the connected variant.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingBehaviour.java"
s = io.open(p, encoding="utf-8").read()

old = '''    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction,
                                       TextureAtlasSprite original) {
        if (!isPlate(original)) {
            return null;
        }
        CTSpriteShiftEntry entry = shiftFor(GTPortBlock.casingOf(state));
        return entry != null && isUsable(entry) ? entry : null;
    }'''
new = '''    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction,
                                       TextureAtlasSprite original) {
        if (!isPlate(original)) {
            return null;
        }
        // `CTModel` only applies a shift whose `original` is the quad's own sprite. The model draws
        // the plate with the default casing's sheet whatever casing it is set into, so the entry has
        // to start from that sheet and point at the casing's — not start from the casing's.
        Block casing = GTPortBlock.casingOf(state);
        if (casing == defaultCasing()) {
            // Already the authored plate; a shift onto itself would be a no-op.
            return null;
        }
        CTSpriteShiftEntry entry = shiftFor(casing);
        return entry != null && isUsable(entry) ? entry : null;
    }'''
assert old in s
s = s.replace(old, new, 1)

# entries are keyed by the casing they lead to, but built from the authored plate
old2 = '''    private static CTSpriteShiftEntry buildShift(ResourceLocation sheet) {
        ResourceLocation connected = ResourceLocation.fromNamespaceAndPath(
                sheet.getNamespace(), sheet.getPath() + "_ctm");'''
new2 = '''    private static CTSpriteShiftEntry buildShift(ResourceLocation sheet) {
        // Start from the sheet the model actually draws, and land on this casing's connected sheet.
        ResourceLocation authored = sheetOf(defaultCasing());
        ResourceLocation connected = ResourceLocation.fromNamespaceAndPath(
                sheet.getNamespace(), sheet.getPath() + "_ctm");'''
assert old2 in s
s = s.replace(old2, new2, 1)

s = s.replace('''        entry.set(sheet, connected);''', '''        entry.set(authored, connected);''', 1)
s = s.replace('''        if (!isUsable(entry)) {
            return null;
        }
        GoldenTweaks.LOGGER.info("[GT] ct shift {} -> {} / {}", sheet,
                entry.getOriginal().contents().name(), entry.getTarget().contents().name());''',
'''        if (!isUsable(entry)) {
            return null;
        }
        GoldenTweaks.LOGGER.info("[GT] ct shift {} : {} -> {}", sheet,
                entry.getOriginal().contents().name(), entry.getTarget().contents().name());''', 1)

# a helper for "the sheet the port models author"
anchor = '''    /** The block texture a casing draws, from its registered id. */'''
assert anchor in s
s = s.replace(anchor, '''    /** The casing whose sheet the port models author their plate with. */
    private static Block defaultCasing() {
        return GTMultiblockBlocks.CASINGS.getFirst().get();
    }

    /** The block texture a casing draws, from its registered id. */''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("shift now starts from the authored plate")
