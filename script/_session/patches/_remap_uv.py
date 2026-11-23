"""Re-map the plate's UVs along with its sprite.

Swapping only the sprite reference changes nothing visible. A baked quad's vertices hold UVs in
atlas space, computed from the original sprite's rectangle when the model was baked; a quad still
carrying `shell_pe`'s coordinates, now labelled `shell_pp`, keeps sampling the polyethylene pixels.

The UVs have to be carried across — each vertex's position relative to the old sprite, placed inside
the new one. GregTech does exactly this with `Mth.map` in its `setSprite` transformer, which is why
their retexture shows and a bare sprite swap does not.

`IQuadTransformer`'s own STRIDE and UV0 constants are used rather than hard-coded numbers: they
describe the baked vertex layout, and reading them from the API keeps this correct if that layout
ever changes.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s = io.open(p, encoding="utf-8").read()

old_start = s.index('''    /**
     * The same quad, drawn with another sprite.''')
old_end = s.index("}\n", s.index("return new BakedQuad(vertices, quad.getTintIndex()", old_start)) + 2

new_block = '''    /**
     * The same quad, drawn with another sprite.
     *
     * <p>Changing the sprite alone is not enough. A baked quad's vertices hold UVs in atlas space —
     * they were computed from the original sprite's rectangle when the model was baked — so a quad
     * still carrying {@code shell_pe}'s coordinates, relabelled {@code shell_pp}, keeps sampling the
     * polyethylene pixels. The UVs are therefore carried across: each vertex's position relative to
     * the old sprite, placed inside the new one. This is the same remap GregTech performs in its
     * sprite transformer.</p>
     */
    private static BakedQuad withSprite(BakedQuad quad, TextureAtlasSprite sprite) {
        TextureAtlasSprite from = quad.getSprite();
        if (from == null || from == sprite) {
            return quad;
        }

        int[] vertices = quad.getVertices().clone();
        for (int vertex = 0; vertex < VERTICES_PER_QUAD; vertex++) {
            int base = vertex * IQuadTransformer.STRIDE + IQuadTransformer.UV0;

            float u = Float.intBitsToFloat(vertices[base]);
            float v = Float.intBitsToFloat(vertices[base + 1]);

            vertices[base] = Float.floatToRawIntBits(
                    Mth.map(u, from.getU0(), from.getU1(), sprite.getU0(), sprite.getU1()));
            vertices[base + 1] = Float.floatToRawIntBits(
                    Mth.map(v, from.getV0(), from.getV1(), sprite.getV0(), sprite.getV1()));
        }

        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(),
                sprite, quad.isShade(), quad.hasAmbientOcclusion());
    }
'''

s = s[:old_start] + new_block + s[old_end:]

s = s.replace('''    /** Throttles the diagnostics: one report per step is enough to tell what is happening. */''',
'''    /** A block quad always has four vertices. */
    private static final int VERTICES_PER_QUAD = 4;

    /** Throttles the diagnostics: one report per step is enough to tell what is happening. */''', 1)

for imp in ("import net.minecraft.util.Mth;",
            "import net.neoforged.neoforge.client.model.IQuadTransformer;"):
    if imp not in s:
        s = s.replace("import net.minecraft.util.RandomSource;", imp + "\nimport net.minecraft.util.RandomSource;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("UV remap written, using IQuadTransformer's layout constants")
