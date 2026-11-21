import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ============ tick: step every pane ============
s = s.replace('''    /**
     * Advances the side pane's reveal by one frame.
     *
     * <p>Same shape as the reference implementation: the open flag asks for a size and the size
     * walks toward it at a fixed rate, so the pane slides open and shut instead of snapping. Both
     * dimensions move together, because the artwork grows in both.</p>
     */
    private void updateSidebarSize() {
        if (this.sidebarOpen) {
            this.sidebarWidth = Math.min(SIDEBAR_MAX_WIDTH,
                    this.sidebarWidth + SIDEBAR_EXPAND_SPEED);
            this.sidebarHeight = Math.min(SIDEBAR_MAX_HEIGHT,
                    this.sidebarHeight + SIDEBAR_EXPAND_SPEED);
        } else {
            this.sidebarWidth = Math.max(SIDEBAR_MIN_WIDTH,
                    this.sidebarWidth - SIDEBAR_EXPAND_SPEED);
            this.sidebarHeight = Math.max(SIDEBAR_MIN_HEIGHT,
                    this.sidebarHeight - SIDEBAR_EXPAND_SPEED);
        }
        this.sidebarFullyOpen = this.sidebarOpen
                && this.sidebarWidth == SIDEBAR_MAX_WIDTH
                && this.sidebarHeight == SIDEBAR_MAX_HEIGHT;
    }''',
'''    /** Advances every pane's reveal by one frame. */
    private void updateSidebarSize() {
        for (Pane pane : this.panes) {
            pane.step();
        }
    }''', 1)

# ============ rendering: one pane at a time, stacked ============
s = s.replace('''    private void renderSidebar(GuiGraphics graphics) {
        int x = this.leftPos + SIDEBAR_X;
        int y = this.topPos + SIDEBAR_Y;

        // The box lives on the artwork's third band, so it only appears once the pane has grown
        // far enough to reveal that band. Any earlier and it would sit on the collapsed tab.
        boolean bandVisible = this.sidebarWidth >= SIDEBAR_FIELD_X + SIDEBAR_FIELD_WIDTH
                && this.sidebarHeight >= SIDEBAR_FIELD_Y + SIDEBAR_FIELD_HEIGHT;
        if (this.overclockField != null) {
            this.overclockField.setVisible(bandVisible);
            if (bandVisible) {
                this.overclockField.setX(x + SIDEBAR_FIELD_X);
                this.overclockField.setY(y + SIDEBAR_FIELD_Y);
            }
        }

        if (this.sidebarWidth == SIDEBAR_MIN_WIDTH
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
            renderSidebarReadout(graphics, x, y);
        }
    }''',
'''    private void renderSidebar(GuiGraphics graphics) {
        for (int index = 0; index < this.panes.size(); index++) {
            renderPane(graphics, this.panes.get(index), index);
        }
    }

    /** Top edge of a pane, in panel coordinates: panes stack downward from the top right. */
    private static int paneTop(int index) {
        return SIDEBAR_Y + index * SIDEBAR_STACK_STEP;
    }

    private void renderPane(GuiGraphics graphics, Pane pane, int index) {
        int x = this.leftPos + SIDEBAR_X;
        int y = this.topPos + paneTop(index);

        // The box lives on the artwork's third band, so it only appears once the pane has grown far
        // enough to reveal that band. Any earlier and it would sit on the collapsed tab.
        boolean bandVisible = pane.width >= SIDEBAR_FIELD_X + SIDEBAR_FIELD_WIDTH
                && pane.height >= SIDEBAR_FIELD_Y + SIDEBAR_FIELD_HEIGHT;
        if (pane.field != null) {
            pane.field.setVisible(bandVisible);
            if (bandVisible) {
                pane.field.setX(x + SIDEBAR_FIELD_X);
                pane.field.setY(y + SIDEBAR_FIELD_Y);
            }
        }

        if (pane.collapsed()) {
            // Settled shut: draw the collapsed tab from its own corner of the sheet, so it looks
            // exactly like the reference rather than like a crop of the expanded one.
            graphics.blit(SIDEBAR_TEXTURE, x, y, 0, 0,
                    SIDEBAR_MIN_WIDTH, SIDEBAR_MIN_HEIGHT);
            renderPaneIcon(graphics, pane, x + SIDEBAR_SLOT_COLLAPSED_X,
                    y + SIDEBAR_SLOT_COLLAPSED_Y);
            return;
        }
        // Anywhere in between, reveal the expanded artwork up to the animated size.
        graphics.blit(SIDEBAR_TEXTURE, x, y, 0, SIDEBAR_EXPANDED_V, pane.width, pane.height);
        // The expanded slot only exists once the artwork has grown past its corner; drawing it
        // earlier would put the icon over the collapsed tab's own slot.
        if (pane.width >= SIDEBAR_SLOT_EXPANDED_X + SIDEBAR_SLOT_SIZE
                && pane.height >= SIDEBAR_SLOT_EXPANDED_Y + SIDEBAR_SLOT_SIZE) {
            renderPaneIcon(graphics, pane, x + SIDEBAR_SLOT_EXPANDED_X,
                    y + SIDEBAR_SLOT_EXPANDED_Y);
            renderPaneReadout(graphics, pane, x, y);
        }
    }

    /** The pane's own icon: the overclock circuit or the parallel one. */
    private void renderPaneIcon(GuiGraphics graphics, Pane pane, int x, int y) {
        if (pane == this.overclockPane) {
            graphics.renderItem(overclockIcon(), x, y);
            return;
        }
        // A plain sprite rather than an item, so it is blitted at the slot's own size.
        graphics.blit(PARALLEL_ICON_TEXTURE, x, y, 0, 0,
                PARALLEL_ICON_SIZE, PARALLEL_ICON_SIZE,
                PARALLEL_ICON_SIZE, PARALLEL_ICON_SIZE);
    }''', 1)

# ============ the two lines, per pane ============
s = s.replace('''    private void renderSidebarReadout(GuiGraphics graphics, int originX, int originY) {
        int textX = originX + SIDEBAR_TEXT_X;
        int textY = originY + SIDEBAR_TEXT_Y;

        graphics.pose().pushPose();
        graphics.pose().translate(textX, textY, 0.0F);
        graphics.pose().scale(SIDEBAR_TEXT_SCALE, SIDEBAR_TEXT_SCALE, 1.0F);
        graphics.drawString(this.font,
                Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.ratio",
                        SIDEBAR_OVERCLOCK_RATIO),
                0, 0, SIDEBAR_TEXT_COLOUR, false);
        // A full line below the first: at this scale the font is nine units tall, so anything less
        // would overlap.
        // Fixed wording, not the stored value: the line names the setting, and the number beside it
        // is what the box below is for.
        graphics.drawString(this.font,
                Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.max",
                        Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.infinite")),
                0, SIDEBAR_TEXT_LINE_HEIGHT, SIDEBAR_TEXT_COLOUR, false);
        graphics.pose().popPose();
    }''',
'''    private void renderPaneReadout(GuiGraphics graphics, Pane pane, int originX, int originY) {
        int textX = originX + SIDEBAR_TEXT_X;
        int textY = originY + SIDEBAR_TEXT_Y;

        // Both panes are two fixed lines: a title, then a caption naming what the box edits. Neither
        // shows the stored number — the box beneath already does.
        boolean overclock = pane == this.overclockPane;
        Component first = Component.translatable(overclock
                ? "gui.goldentweaks.chemical_reactor.sidebar.ratio"
                : "gui.goldentweaks.chemical_reactor.sidebar.parallel.title");
        Component second = overclock
                ? Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.max",
                        Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.infinite"))
                : Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.parallel.max");

        graphics.pose().pushPose();
        graphics.pose().translate(textX, textY, 0.0F);
        graphics.pose().scale(SIDEBAR_TEXT_SCALE, SIDEBAR_TEXT_SCALE, 1.0F);
        graphics.drawString(this.font, first, 0, 0, SIDEBAR_TEXT_COLOUR, false);
        // A full line below the first: at this scale the font is nine units tall, so anything less
        // would overlap.
        graphics.drawString(this.font, second,
                0, SIDEBAR_TEXT_LINE_HEIGHT, SIDEBAR_TEXT_COLOUR, false);
        graphics.pose().popPose();
    }''', 1)

# ============ hit test per pane ============
s = s.replace('''    /** Whether a point is over the pane's drawn slot, in the state currently showing. */
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
    }''',
'''    /** The pane whose icon is under the cursor, or {@code null}. */
    @Nullable
    private Pane paneIconAt(double mouseX, double mouseY) {
        double x = mouseX - this.leftPos;
        double y = mouseY - this.topPos;
        for (int index = 0; index < this.panes.size(); index++) {
            Pane pane = this.panes.get(index);
            boolean collapsed = pane.collapsed();
            int slotX = collapsed ? SIDEBAR_SLOT_COLLAPSED_X : SIDEBAR_SLOT_EXPANDED_X;
            int slotY = collapsed ? SIDEBAR_SLOT_COLLAPSED_Y : SIDEBAR_SLOT_EXPANDED_Y;
            if (!collapsed
                    && (pane.width < slotX + SIDEBAR_SLOT_SIZE
                    || pane.height < slotY + SIDEBAR_SLOT_SIZE)) {
                continue;
            }
            if (inside(x, y, SIDEBAR_X + slotX, paneTop(index) + slotY,
                    SIDEBAR_SLOT_SIZE, SIDEBAR_SLOT_SIZE)) {
                return pane;
            }
        }
        return null;
    }

    /** The pane whose input box is under the cursor, or {@code null}. */
    @Nullable
    private Pane paneFieldAt(double mouseX, double mouseY) {
        for (Pane pane : this.panes) {
            if (pane.field != null && pane.field.isVisible()
                    && pane.field.isMouseOver(mouseX, mouseY)) {
                return pane;
            }
        }
        return null;
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
