"""Draw essentia through Thaumcraft's own renderer.

Hand-rolling the blit was wrong in three ways the mod's renderer already handles:

- the sheet is chosen by `Aspect.image()`, not derived from the tag;
- the tint is `Aspect.color()` applied through a POSITION_TEX_COLOR quad, which is what lets an
  aspect declare its own blend mode;
- the cosmic aspects are not drawn from a sheet at all — they use a dedicated item model, and
  the mod already routes them through `GTAspectEntry.isCosmic`.

So this delegates, and only keeps the fallback for when the mod's classes cannot be reached.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java"
s = io.open(p, encoding="utf-8").read()

# ---------------- replace the hand-written sprite/tint helpers ----------------
s = s.replace('''    /**
     * The sprite Thaumcraft keeps for one aspect, or {@code null} for the other kinds.
     *
     * <p>The texture path is the aspect's own tag, which is why this needs no asset of ours. It is
     * only meaningful when Thaumcraft is installed, so the caller checks that first.</p>
     */
    @Nullable
    private static ResourceLocation essentiaSprite(GTReactionStack stack) {
        String tag = stack.essentiaTag();
        if (tag == null) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/aspects/" + tag + ".png");
    }

    /**
     * The colour an aspect is drawn in.
     *
     * <p>Thaumcraft's aspect sheets are white masks rather than coloured art — its own GUIs
     * multiply them by this value — so a plain blit shows a white shape and the hue has to come
     * from here. Guarded because {@code Aspect} is a Thaumcraft class.</p>
     */
    private static int essentiaColour(GTReactionStack stack, int fallback) {
        String tag = stack.essentiaTag();
        if (tag == null) {
            return fallback;
        }
        try {
            var aspect = thaumcraft.api.aspects.Aspect.getAspect(tag);
            return aspect == null ? fallback : 0xFF000000 | aspect.getColor();
        } catch (NoClassDefFoundError absent) {
            return fallback;
        }
    }''',
'''    /**
     * Draws one aspect, deferring to Thaumcraft's own renderer.
     *
     * <p>Not a plain blit: the sheet comes from {@code Aspect.image()}, the tint from
     * {@code Aspect.color()}, and the cosmic aspects are not sheet-based at all — they render as a
     * dedicated item model. All three of those live in {@code AspectGuiRenderer}, and the mod
     * already wraps the cosmic case in {@code GTAspectEntry.isCosmic}.</p>
     *
     * <p>Guarded because every class named here belongs to Thaumcraft: this screen is loaded on
     * packs that do not have it.</p>
     */
    private static void drawEssentia(GuiGraphics graphics, GTReactionStack stack, int x, int y) {
        String tag = stack.essentiaTag();
        if (tag == null) {
            return;
        }
        try {
            thaumcraft.api.aspects.Aspect aspect = thaumcraft.api.aspects.Aspect.getAspect(tag);
            if (aspect == null) {
                return;
            }
            if (net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry.isCosmic(tag)) {
                graphics.renderItem(
                        net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftAdditionalItems
                                .cosmicIconStack(), x, y);
            } else {
                thaumcraft.client.gui.AspectGuiRenderer.draw(graphics, aspect, x, y, 16, 1.0F);
            }
        } catch (NoClassDefFoundError absent) {
            // Nothing sensible to draw without the mod's renderer.
        }
    }''', 1)

# ---------------- use it in the essentia branch ----------------
s = s.replace('''            ResourceLocation sprite = essentiaSprite(stack);
            if (sprite != null) {
                // The sheet is a white mask, so the aspect's colour is applied here rather than
                // being baked into the texture.
                int tint = essentiaColour(stack, 0xFFFFFFFF);
                float red = ((tint >> 16) & 0xFF) / 255.0F;
                float green = ((tint >> 8) & 0xFF) / 255.0F;
                float blue = (tint & 0xFF) / 255.0F;
                graphics.setColor(red, green, blue, 1.0F);
                graphics.blit(sprite, x, y, 0, 0, 16, 16, 16, 16);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }''',
'''            drawEssentia(graphics, stack, x, y);''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("essentia drawn through TC's renderer")
