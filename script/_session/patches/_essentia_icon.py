"""Draw essentia with Thaumcraft's own aspect sprite.

Thaumcraft ships one 16x16 icon per aspect at `thaumcraft:textures/aspects/<tag>.png`, so no
asset of ours is needed — the id is already the tag. The draw is a plain blit followed by the
same amount label the other kinds get.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    /** One stack: the icon through EMI, then its amount on the corner, as EMI draws a slot. */
    private void drawStack(GuiGraphics graphics, dev.emi.emi.runtime.EmiDrawContext context,
                           GTReactionStack stack, int x, int y,
                           @Nullable HolderLookup.Provider registries) {
        if (!ModList.get().isLoaded("emi")) {''',
'''    /**
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

    /** One stack: the icon through EMI, then its amount on the corner, as EMI draws a slot. */
    private void drawStack(GuiGraphics graphics, dev.emi.emi.runtime.EmiDrawContext context,
                           GTReactionStack stack, int x, int y,
                           @Nullable HolderLookup.Provider registries) {
        // Essentia has no EMI stack at all, so its icon is drawn directly and the count label is
        // added here rather than going through EMI's amount helper.
        if (stack.kind() == GTReactionStack.Kind.ESSENTIA) {
            ResourceLocation sprite = essentiaSprite(stack);
            if (sprite != null) {
                graphics.blit(sprite, x, y, 0, 0, 16, 16, 16, 16);
            }
            dev.emi.emi.EmiRenderHelper.renderAmount(context, x, y,
                    Component.literal(GTPortAmounts.number(stack.amount(), false)));
            return;
        }

        if (!ModList.get().isLoaded("emi")) {''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("essentia draws its aspect sprite")
