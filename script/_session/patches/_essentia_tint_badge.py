"""Colour the essentia sprite and badge it like an item.

Two faults, both in the draw path:

- the aspect sheets are white masks, so blitting them uncoloured gives a white blob. Thaumcraft
  keeps each aspect's own hue in `Aspect.color()`, which is what its own GUIs multiply by.
- essentia was passed to the formatter as a fluid, so a count of 8 printed "8 mB". Essentia is
  counted in whole units the way items are, so it asks for the item form.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    @Nullable
    private static ResourceLocation essentiaSprite(GTReactionStack stack) {
        String tag = stack.essentiaTag();
        if (tag == null) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/aspects/" + tag + ".png");
    }''',
'''    @Nullable
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
    }''', 1)

s = s.replace('''            ResourceLocation sprite = essentiaSprite(stack);
            if (sprite != null) {
                graphics.blit(sprite, x, y, 0, 0, 16, 16, 16, 16);
            }
            if (ModList.get().isLoaded("emi")) {
                dev.emi.emi.EmiRenderHelper.renderAmount(context, x, y,
                        Component.literal(GTPortAmounts.number(stack.amount(), false)));
            } else {
                drawAmount(graphics, stack.amount(), x, y);
            }
            return;''',
'''            ResourceLocation sprite = essentiaSprite(stack);
            if (sprite != null) {
                // The sheet is a white mask, so the aspect's colour is applied here rather than
                // being baked into the texture.
                graphics.setColor(0.0F, 0.0F, 0.0F, 1.0F);
                int tint = essentiaColour(stack, 0xFFFFFFFF);
                float red = ((tint >> 16) & 0xFF) / 255.0F;
                float green = ((tint >> 8) & 0xFF) / 255.0F;
                float blue = (tint & 0xFF) / 255.0F;
                graphics.setColor(red, green, blue, 1.0F);
                graphics.blit(sprite, x, y, 0, 0, 16, 16, 16, 16);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
            // Essentia counts in whole units like an item, so it gets the item-shaped badge
            // instead of the fluid one that would append "mB".
            if (ModList.get().isLoaded("emi")) {
                dev.emi.emi.EmiRenderHelper.renderAmount(context, x, y,
                        Component.literal(GTPortAmounts.number(stack.amount(), true)));
            } else {
                drawAmount(graphics, stack.amount(), true, x, y);
            }
            return;''', 1)

# The amount helper now needs to know which shape of badge to draw.
s = s.replace('''    private void drawAmount(GuiGraphics graphics, long amount, int x, int y) {
        Component text = Component.literal(GTPortAmounts.number(amount, false));
        int width = this.font.width(text);
        graphics.drawString(this.font, text, x + 17 - width, y + 9, 0xFFFFFFFF, true);
    }''',
'''    private void drawAmount(GuiGraphics graphics, long amount, boolean item, int x, int y) {
        Component text = Component.literal(GTPortAmounts.number(amount, item));
        int width = this.font.width(text);
        graphics.drawString(this.font, text, x + 17 - width, y + 9, 0xFFFFFFFF, true);
    }''', 1)

s = s.replace('''            drawAmount(graphics, stack.amount(), x, y);
            return;
        }''',
'''            drawAmount(graphics, stack.amount(),
                    stack.kind() == GTReactionStack.Kind.ITEM, x, y);
            return;
        }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("essentia is tinted and badged like an item")
