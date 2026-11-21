import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- constants: slot offsets in both states ----
s = s.replace('''    /** Pixels per frame, matching the reference panel's own expansion speed. */
    private static final int SIDEBAR_EXPAND_SPEED = 8;''',
'''    /** Pixels per frame, matching the reference panel's own expansion speed. */
    private static final int SIDEBAR_EXPAND_SPEED = 8;
    /**
     * The drawn slot inside the pane, where the projector's icon goes.
     *
     * <p>Both states carry their own slot and they do not sit at the same offset: the collapsed tab
     * puts it at (1, 5) and the expanded panel at (3, 6). Rendering to each state's own offset is
     * what keeps the icon inside the frame while the pane is opening, rather than sliding in from
     * the corner as the artwork is revealed.</p>
     */
    private static final int SIDEBAR_SLOT_COLLAPSED_X = 1;
    private static final int SIDEBAR_SLOT_COLLAPSED_Y = 5;
    private static final int SIDEBAR_SLOT_EXPANDED_X = 3;
    private static final int SIDEBAR_SLOT_EXPANDED_Y = 6;
    private static final int SIDEBAR_SLOT_SIZE = 16;''', 1)

# ---- draw the icon ----
s = s.replace('''        if (this.sidebarWidth == SIDEBAR_MIN_WIDTH
                && this.sidebarHeight == SIDEBAR_MIN_HEIGHT) {
            // Settled shut: draw the collapsed tab from its own corner of the sheet, so it looks
            // exactly like the reference rather than like a crop of the expanded one.
            graphics.blit(SIDEBAR_TEXTURE, x, y, 0, 0,
                    SIDEBAR_MIN_WIDTH, SIDEBAR_MIN_HEIGHT);
            return;
        }
        // Anywhere in between, reveal the expanded artwork up to the animated size.
        graphics.blit(SIDEBAR_TEXTURE, x, y, 0, SIDEBAR_EXPANDED_V,
                this.sidebarWidth, this.sidebarHeight);
    }''',
'''        if (this.sidebarWidth == SIDEBAR_MIN_WIDTH
                && this.sidebarHeight == SIDEBAR_MIN_HEIGHT) {
            // Settled shut: draw the collapsed tab from its own corner of the sheet, so it looks
            // exactly like the reference rather than like a crop of the expanded one.
            graphics.blit(SIDEBAR_TEXTURE, x, y, 0, 0,
                    SIDEBAR_MIN_WIDTH, SIDEBAR_MIN_HEIGHT);
            renderSidebarSlot(graphics, x + SIDEBAR_SLOT_COLLAPSED_X,
                    y + SIDEBAR_SLOT_COLLAPSED_Y);
            return;
        }
        // Anywhere in between, reveal the expanded artwork up to the animated size.
        graphics.blit(SIDEBAR_TEXTURE, x, y, 0, SIDEBAR_EXPANDED_V,
                this.sidebarWidth, this.sidebarHeight);
        // The expanded slot only exists once the artwork has grown past its corner; drawing it
        // earlier would put the icon over the collapsed tab's own slot.
        if (this.sidebarWidth >= SIDEBAR_SLOT_EXPANDED_X + SIDEBAR_SLOT_SIZE
                && this.sidebarHeight >= SIDEBAR_SLOT_EXPANDED_Y + SIDEBAR_SLOT_SIZE) {
            renderSidebarSlot(graphics, x + SIDEBAR_SLOT_EXPANDED_X,
                    y + SIDEBAR_SLOT_EXPANDED_Y);
        }
    }

    /** The projector's icon, in the pane's drawn slot. */
    private void renderSidebarSlot(GuiGraphics graphics, int x, int y) {
        graphics.renderItem(projectorIcon(), x, y);
    }

    /** A stand-in stack for the projector item, with its own renderer so no real stack is needed. */
    private static ItemStack projectorIcon() {
        return new ItemStack(GTMultiblockBlocks.PROJECTOR.get());
    }''', 1)

# ---- the item icon needs the item in hand for a tooltip; add one ----
s = s.replace('''        if (sidebarContains(mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.translatable(this.sidebarOpen
                            ? "gui.goldentweaks.chemical_reactor.sidebar.close"
                            : "gui.goldentweaks.chemical_reactor.sidebar.open"),
                    mouseX, mouseY);
            return;
        }''',
'''        if (sidebarContains(mouseX, mouseY)) {
            // Over the slot, name the item; elsewhere, say what clicking the pane will do.
            if (sidebarSlotContains(mouseX, mouseY)) {
                graphics.renderTooltip(this.font, projectorIcon(), mouseX, mouseY);
            } else {
                graphics.renderTooltip(this.font,
                        Component.translatable(this.sidebarOpen
                                ? "gui.goldentweaks.chemical_reactor.sidebar.close"
                                : "gui.goldentweaks.chemical_reactor.sidebar.open"),
                        mouseX, mouseY);
            }
            return;
        }''', 1)

# ---- slot hit test follows the same offsets ----
s = s.replace('''    /** Whether a point is over the side pane, in its current revealed size. */''',
'''    /** Whether a point is over the pane's drawn slot, in the state currently showing. */
    private boolean sidebarSlotContains(double mouseX, double mouseY) {
        double x = mouseX - this.leftPos;
        double y = mouseY - this.topPos;
        boolean collapsed = this.sidebarWidth == SIDEBAR_MIN_WIDTH
                && this.sidebarHeight == SIDEBAR_MIN_HEIGHT;
        int slotX = collapsed ? SIDEBAR_SLOT_COLLAPSED_X : SIDEBAR_SLOT_EXPANDED_X;
        int slotY = collapsed ? SIDEBAR_SLOT_COLLAPSED_Y : SIDEBAR_SLOT_EXPANDED_Y;
        if (!collapsed
                && (this.sidebarWidth < slotX + SIDEBAR_SLOT_SIZE
                || this.sidebarHeight < slotY + SIDEBAR_SLOT_SIZE)) {
            return false;
        }
        return inside(x, y, SIDEBAR_X + slotX, SIDEBAR_Y + slotY,
                SIDEBAR_SLOT_SIZE, SIDEBAR_SLOT_SIZE);
    }

    /** Whether a point is over the side pane, in its current revealed size. */''', 1)

# ---- import ----
if 'import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;' not in s:
    s = s.replace('import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.',
                  'import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;\nimport net.fodoth.skina.goldentweaks.compat.avaritia_more_items.', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
