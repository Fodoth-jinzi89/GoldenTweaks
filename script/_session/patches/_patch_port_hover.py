import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# imports for EMI interaction
s = s.replace('import net.neoforged.neoforge.network.PacketDistributor;',
'''import net.neoforged.neoforge.network.PacketDistributor;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;''', 1)

# index of the stack under the cursor, or -1
s = s.replace('''    // ---- scrolling ----''',
'''    // ---- hover and recipe lookup ----

    /**
     * Which entry of the storage grid the cursor is over, or {@code -1}.
     *
     * <p>The grid is drawn as plain icons rather than EMI widgets, so hover has to be resolved by
     * hand here: only entries that are actually drawn can be hovered, which is why the index is
     * bounded by the contents size and not by the cell count.</p>
     */
    private int hoveredStack(double mouseX, double mouseY) {
        if (this.menu.kind() == GTPortKind.ENERGY) {
            return -1;
        }
        for (int cell = 0; cell < GTPortMenu.STORAGE_CELLS; cell++) {
            int column = cell % GTPortMenu.STORAGE_COLUMNS;
            int row = cell / GTPortMenu.STORAGE_COLUMNS;
            int x = this.leftPos + GTPortMenu.STORAGE_X + column * GTPortMenu.SLOT_STEP;
            int y = this.topPos + GTPortMenu.STORAGE_Y + row * GTPortMenu.SLOT_STEP;
            if (mouseX >= x && mouseX < x + GTPortMenu.SLOT_SIZE
                    && mouseY >= y && mouseY < y + GTPortMenu.SLOT_SIZE) {
                int index = this.scrollRow * GTPortMenu.STORAGE_COLUMNS + cell;
                return index < contents().size() ? index : -1;
            }
        }
        return -1;
    }

    /** The EMI form of one entry, or {@code null} when EMI is absent or cannot build it. */
    @Nullable
    private EmiIngredient ingredientOf(GTReactionStack stack) {
        if (!ModList.get().isLoaded("emi")) {
            return null;
        }
        return GTReactionStacks.toEmiIngredient(stack, registries());
    }

    /**
     * Shows the stack's own tooltip, the same one EMI shows in a recipe page.
     *
     * <p>Built from the EMI stack rather than from the item, so a fluid or a chemical gets the
     * tooltip it would have there instead of a bare name.</p>
     */
    private void renderStackTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int index = hoveredStack(mouseX, mouseY);
        if (index < 0) {
            return;
        }
        GTReactionStack stack = contents().get(index);
        EmiIngredient ingredient = ingredientOf(stack);
        if (ingredient != null) {
            var lines = ingredient.getTooltip();
            if (!lines.isEmpty()) {
                graphics.renderTooltip(this.font, lines, mouseX, mouseY);
            }
            return;
        }
        // Without EMI only items can say anything at all.
        if (stack.kind() == GTReactionStack.Kind.ITEM) {
            var item = stack.toItemStack();
            if (!item.isEmpty()) {
                graphics.renderTooltip(this.font, item, mouseX, mouseY);
            }
        }
    }

    /**
     * Answers a click on a stack the way EMI does: left for recipes, right for uses.
     *
     * <p>Returns whether the click was consumed, so the window does not also treat it as a click on
     * the panel behind.</p>
     */
    private boolean clickStack(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) {
            return false;
        }
        int index = hoveredStack(mouseX, mouseY);
        if (index < 0) {
            return false;
        }
        EmiIngredient ingredient = ingredientOf(contents().get(index));
        if (ingredient == null) {
            return false;
        }
        if (button == 0) {
            EmiApi.displayRecipes(ingredient);
        } else {
            EmiApi.displayUses(ingredient);
        }
        return true;
    }

    // ---- scrolling ----''', 1)

# hook tooltip into render
s = s.replace('''        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }''',
'''        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderStackTooltip(graphics, mouseX, mouseY);
    }''', 1)

# consume stack clicks before the rest of the panel
s = s.replace('''        // Clicking the rail jumps to that position, the way a page scrollbar behaves.''',
'''        // A click on a stack belongs to EMI, not to the panel: left asks for recipes, right for uses.
        if (clickStack(mouseX, mouseY, button)) {
            return true;
        }

        // Clicking the rail jumps to that position, the way a page scrollbar behaves.''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
