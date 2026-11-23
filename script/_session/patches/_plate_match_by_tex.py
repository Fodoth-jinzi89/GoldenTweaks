"""Match the plate by its authored texture name, not a renamed sprite.

The base model names `goldentweaks:block/shell_pe` for its shell, and that name is what the model
loader records on the quads. Renaming the sprite would have meant editing the model and every
future artwork change, so the wrapper recognises the casing texture by id instead — the model stays
exactly as authored.

Anything naming a casing block's own texture is treated as the plate. That is the one rule that
holds for every casing: they are, by definition, the textures casings use.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s = io.open(p, encoding="utf-8").read()

old = '''    /**
     * The plate's sprite as authored.
     *
     * <p>The plate is found by sprite name rather than by element index: an index breaks the moment
     * an element is added to the model, and this has to survive the artwork changing.</p>
     */
    private static final String PLATE_SPRITE = "shell_plate";'''
new = '''    /**
     * Names that identify the plate among a port's quads.
     *
     * <p>Matched by texture rather than by element index: an index breaks the moment an element is
     * added, and this has to survive the artwork changing. Every casing texture lives under this
     * prefix, so the rule holds for casings this class has never seen.</p>
     */
    private static final String CASING_TEXTURE_PREFIX = "block/shell_";'''
assert old in s
s = s.replace(old, new, 1)

old2 = '''    private static boolean isPlate(BakedQuad quad) {
        TextureAtlasSprite sprite = quad.getSprite();
        return sprite != null && sprite.contents().name().getPath().endsWith(PLATE_SPRITE);
    }'''
new2 = '''    private static boolean isPlate(BakedQuad quad) {
        TextureAtlasSprite sprite = quad.getSprite();
        return sprite != null
                && sprite.contents().name().getPath().contains(CASING_TEXTURE_PREFIX);
    }'''
assert old2 in s
s = s.replace(old2, new2, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("plate matched by casing texture prefix")
