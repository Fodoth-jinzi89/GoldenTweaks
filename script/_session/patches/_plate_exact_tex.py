"""Identify the plate by comparing against the model's own shell texture.

A prefix match would also match the icon or the ring if either ever used a casing sheet, and it
silently depends on every casing being named `shell_*`. Comparing the quad's sprite against the
wrapped model's shell texture asks the exact question instead: "is this quad drawn with the plate
texture?"

That also decouples the wrapper from naming entirely. A casing renamed, or a port whose plate uses
a different sheet, needs no change here.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s = io.open(p, encoding="utf-8").read()

old = '''    /**
     * Names that identify the plate among a port's quads.
     *
     * <p>Matched by texture rather than by element index: an index breaks the moment an element is
     * added, and this has to survive the artwork changing. Every casing texture lives under this
     * prefix, so the rule holds for casings this class has never seen.</p>
     */
    private static final String CASING_TEXTURE_PREFIX = "block/shell_";'''
new = '''    /**
     * The sheet the base model uses for its plate.
     *
     * <p>Compares against the model's own texture instead of a name pattern: the question is "is
     * this quad drawn with the plate sheet", and only the authored model knows that. Naming a
     * prefix would also match the icon if it ever used a casing sheet, and would tie this class to
     * the current file names.</p>
     */
    private static final ResourceLocation PLATE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "block/shell_pe");'''
assert old in s
s = s.replace(old, new, 1)

old2 = '''    private static boolean isPlate(BakedQuad quad) {
        TextureAtlasSprite sprite = quad.getSprite();
        return sprite != null
                && sprite.contents().name().getPath().contains(CASING_TEXTURE_PREFIX);
    }'''
new2 = '''    private static boolean isPlate(BakedQuad quad) {
        TextureAtlasSprite sprite = quad.getSprite();
        return sprite != null && sprite.contents().name().equals(PLATE_TEXTURE);
    }'''
assert old2 in s
s = s.replace(old2, new2, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("plate identified by the authored shell texture")
