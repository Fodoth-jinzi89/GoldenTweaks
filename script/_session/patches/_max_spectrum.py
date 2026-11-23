"""MAX's colour cycles through the spectrum instead of staying white.

Two different mechanisms are needed because the two consumers differ:

- the item name and the Jade line are text, rebuilt every time they are drawn, so a hue
  computed from the clock gives a genuinely moving colour;
- the block's ring is baked into vertex colours when the chunk mesh is built, so a tint
  cannot animate. There the model points at an animated texture instead, and the tint
  answers white so it does not fight the frames.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortTier.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    /** The tier's colour as {@code 0xRRGGBB}, without an alpha channel. */
    public int colour() {
        return this.colour;
    }''',
'''    /**
     * The tier's colour as {@code 0xRRGGBB}, without an alpha channel.
     *
     * <p>MAX is the exception: it walks the spectrum, so what comes back depends on the moment.
     * That is only useful for text, which is rebuilt each frame — a block's colour is baked into
     * its mesh, so the MAX ring animates through a texture instead and asks for white here.</p>
     */
    public int colour() {
        if (this == MAX) {
            return spectrum();
        }
        return this.colour;
    }

    /**
     * A hue that advances with the clock, roughly one full turn every few seconds.
     *
     * <p>Written by hand rather than through {@code java.awt.Color}: that class drags AWT in, which
     * a dedicated server has no business loading just to pick a colour.</p>
     */
    private static int spectrum() {
        float turn = (System.currentTimeMillis() % SPECTRUM_PERIOD_MS) / (float) SPECTRUM_PERIOD_MS;
        return hsvToRgb(turn, 1.0F, 1.0F);
    }

    /** One full cycle of the spectrum, in milliseconds. */
    private static final long SPECTRUM_PERIOD_MS = 4000L;

    /** {@code h, s, v} in {@code 0..1} to {@code 0xRRGGBB}. */
    private static int hsvToRgb(float h, float s, float v) {
        float scaled = (h - (float) Math.floor(h)) * 6.0F;
        int sector = (int) scaled;
        float fraction = scaled - sector;
        float p = v * (1.0F - s);
        float q = v * (1.0F - s * fraction);
        float t = v * (1.0F - s * (1.0F - fraction));
        float r;
        float g;
        float b;
        switch (sector % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return (Math.round(r * 255.0F) << 16)
                | (Math.round(g * 255.0F) << 8)
                | Math.round(b * 255.0F);
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("MAX colour is now a spectrum")
