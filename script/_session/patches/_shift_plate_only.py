"""Only the plate is shifted; the icon and the ring keep their own art.

`CTModel` asks `getShift` for every quad, passing that quad's own sprite. My version ignored the
argument and returned the casing's shift unconditionally, so the icon and the tier ring were remapped
into the casing sheet too — which is why hopper and canister ports, whose icons are their own
textures, came out wrong instead of changing colour.

The shift is now returned only for the quad that is actually the plate. Everything else answers
`null`, which `CTModel` treats as "draw this quad unchanged".
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingBehaviour.java"
s = io.open(p, encoding="utf-8").read()

old = '''    /**
     * The sheet this port's plate is drawn from.
     *
     * <p>The shift maps the authored plate's UVs onto the casing's 47-tile sheet, which is what makes
     * the plate line up with the wall it is set into. Built from the casing block's own texture, so a
     * new casing needs only its sheet.</p>
     */
    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction,
                                       TextureAtlasSprite original) {
        return shiftFor(GTPortBlock.casingOf(state));
    }'''

new = '''    /**
     * The sheet this port's plate is drawn from, or {@code null} for every other layer.
     *
     * <p>`CTModel` asks this for each quad and passes that quad's own sprite, so the question can be
     * answered per layer. Only the plate is re-skinned: the icon and the tier ring keep their own
     * artwork, and answering for them would remap them into the casing sheet.</p>
     *
     * <p>The plate is recognised by the sheet the port model authors for it, which is the casing the
     * block is registered with — the same texture the wall around it uses.</p>
     */
    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction,
                                       TextureAtlasSprite original) {
        if (!isPlate(original)) {
            return null;
        }
        return shiftFor(GTPortBlock.casingOf(state));
    }

    /**
     * Whether a sprite is the plate rather than the icon or the ring.
     *
     * <p>A port model's plate is drawn from the default casing's texture, so the test is an identity
     * comparison against that sheet. Icons and rings are drawn from their own files and never
     * match.</p>
     */
    private static boolean isPlate(TextureAtlasSprite sprite) {
        if (sprite == null) {
            return false;
        }
        ResourceLocation sheet = sheetOf(GTMultiblockBlocks.casingAt(0));
        return sheet != null && sprite.contents().name().equals(sheet);
    }'''

assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("shift now applies to the plate only")
