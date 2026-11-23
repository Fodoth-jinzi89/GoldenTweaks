"""Draw fluid and chemical icons without EMI.

EMI normally supplies these sprites, but the window has to work on a pack without it. Both
sources are plain textures the owning mod already ships:

- a fluid's still frame, at `minecraft:textures/block/<path>_still.png`;
- a chemical's own icon, which Mekanism exposes as `Chemical#getIcon()`.

Both are blitted at 16x16 the same way the aspect sprite already is.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java"
s = io.open(p, encoding="utf-8").read()

# ---------------- the sprite resolver ----------------
s = s.replace('''    /** One stack: the icon through EMI, then its amount on the corner, as EMI draws a slot. */''',
'''    /**
     * The still frame of a fluid, at the path vanilla lays them out on.
     *
     * <p>Fluids are not items, so there is no sprite to ask for; the texture name is derived from
     * the fluid's id the same way the vanilla block models do it.</p>
     */
    @Nullable
    private static ResourceLocation fluidSprite(GTReactionStack stack) {
        net.minecraft.world.level.material.Fluid fluid = stack.fluid();
        if (fluid == null) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        // Vanilla's flowing and still frames live under the namespace's own block textures.
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                "textures/block/" + id.getPath() + "_still.png");
    }

    /**
     * The icon Mekanism keeps for a chemical, or {@code null} when Mekanism is absent.
     *
     * <p>Read through the chemical rather than derived from the id: Mekanism lets a chemical name
     * its own icon, so the registry entry is the only thing that knows the right path.</p>
     */
    @Nullable
    private static ResourceLocation chemicalSprite(GTReactionStack stack) {
        try {
            var holder = mekanism.api.MekanismAPI.CHEMICAL_REGISTRY.getHolder(stack.id()).orElse(null);
            if (holder == null) {
                return null;
            }
            ResourceLocation icon = holder.value().getIcon();
            if (icon == null) {
                return null;
            }
            // getIcon gives the texture path without an extension or the textures/ prefix.
            return ResourceLocation.fromNamespaceAndPath(icon.getNamespace(),
                    "textures/" + icon.getPath() + ".png");
        } catch (NoClassDefFoundError absent) {
            return null;
        }
    }

    /** One stack: the icon through EMI, then its amount on the corner, as EMI draws a slot. */''', 1)

# ---------------- use it in the no-EMI branch ----------------
s = s.replace('''        if (!ModList.get().isLoaded("emi")) {
            // Without EMI only items have a sprite this screen can draw on its own; fluids and
            // chemicals are left blank rather than shown as a wrong icon. Their amounts still get
            // a label so the entry is not invisible.
            if (stack.kind() == GTReactionStack.Kind.ITEM) {
                var item = stack.toItemStack();
                if (!item.isEmpty()) {
                    graphics.renderItem(item, x, y);
                    graphics.renderItemDecorations(this.font, item, x, y);
                }
            } else {
                drawAmount(graphics, stack.amount(), x, y);
            }
            return;
        }''',
'''        if (!ModList.get().isLoaded("emi")) {
            // Without EMI the sprite is resolved here from whatever the owning mod ships: an item
            // stack, a fluid's still frame, or Mekanism's own chemical icon.
            switch (stack.kind()) {
                case ITEM -> {
                    var item = stack.toItemStack();
                    if (!item.isEmpty()) {
                        graphics.renderItem(item, x, y);
                        graphics.renderItemDecorations(this.font, item, x, y);
                        return;
                    }
                }
                case FLUID -> {
                    ResourceLocation sprite = fluidSprite(stack);
                    if (sprite != null) {
                        graphics.blit(sprite, x, y, 0, 0, 16, 16, 16, 16);
                    }
                }
                case CHEMICAL -> {
                    ResourceLocation sprite = chemicalSprite(stack);
                    if (sprite != null) {
                        graphics.blit(sprite, x, y, 0, 0, 16, 16, 16, 16);
                    }
                }
                default -> {
                }
            }
            drawAmount(graphics, stack.amount(), x, y);
            return;
        }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("icons drawn without EMI")
