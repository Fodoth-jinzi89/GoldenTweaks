"""The MAX ring is animated, so its tint must not colour it.

A block tint multiplies the texture. MAX's texture already carries its colour per frame, so
any tint but white would darken the animation — and calling GTPortTier.colour() there would
also bake one frozen moment of the spectrum into the chunk mesh.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTMultiblockClientExtensions.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    /** The tier's colour with full alpha, which is what the block renderer wants. */
    private static int tintOf(GTPortTier tier) {
        return 0xFF000000 | tier.colour();
    }''',
'''    /**
     * The tint the block renderer multiplies the ring by.
     *
     * <p>MAX answers white because its ring is an animated texture that already carries its own
     * colour: a tint would both darken it and freeze one moment of the spectrum into the mesh,
     * which is built once and then reused.</p>
     */
    private static int tintOf(GTPortTier tier) {
        if (tier == GTPortTier.MAX) {
            return 0xFFFFFFFF;
        }
        return 0xFF000000 | tier.colour();
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("MAX tint is white")
