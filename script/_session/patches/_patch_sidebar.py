import io, shutil, os

# ---- 1. copy the sprite into the mod's assets ----
src = r'multiblock\cc_1.png'
dst = r'src/main/resources/assets/goldentweaks/textures/gui/cc_1.png'
os.makedirs(os.path.dirname(dst), exist_ok=True)
shutil.copyfile(src, dst)
print('copied sprite ->', dst)

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- 2. constants ----
s = s.replace('''    private static final int WIDGET_LOCK = 0;''',
'''    /**
     * The side pane, drawn outside the machine panel at its top right.
     *
     * <p>One sprite holds both states: the collapsed tab is the top left 22 by 26, and the expanded
     * panel is the 62 by 41 block directly below it. Expanding therefore starts by revealing more of
     * the same artwork rather than swapping pictures, which is also how the reference panel behaves —
     * the collapsed tab and the expanded panel's corner line up exactly.</p>
     */
    private static final ResourceLocation SIDEBAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "textures/gui/cc_1.png");
    /** Origin: flush with the panel's right edge, at its top. Never overlaps the panel itself. */
    private static final int SIDEBAR_X = PANEL_WIDTH;
    private static final int SIDEBAR_Y = 0;
    private static final int SIDEBAR_MIN_WIDTH = 22;
    private static final int SIDEBAR_MIN_HEIGHT = 26;
    private static final int SIDEBAR_MAX_WIDTH = 62;
    private static final int SIDEBAR_MAX_HEIGHT = 41;
    /** Where the expanded artwork sits in the sheet. */
    private static final int SIDEBAR_EXPANDED_V = 26;
    /** Pixels per frame, matching the reference panel's own expansion speed. */
    private static final int SIDEBAR_EXPAND_SPEED = 8;

    private static final int WIDGET_LOCK = 0;''', 1)

# ---- 3. fields ----
s = s.replace('''    /** The dismantle button needs two clicks; the first one only arms the icon. */
    private boolean dismantleArmed;''',
'''    /** The dismantle button needs two clicks; the first one only arms the icon. */
    private boolean dismantleArmed;

    /** Whether the side pane is opened out, and whether it has finished opening. */
    private boolean sidebarOpen;
    private boolean sidebarFullyOpen;
    /** Current revealed size, animated between the collapsed and expanded bounds. */
    private int sidebarWidth = SIDEBAR_MIN_WIDTH;
    private int sidebarHeight = SIDEBAR_MIN_HEIGHT;''', 1)

# ---- 4. draw it, last so it sits on top ----
s = s.replace('''        renderEnergyTrough(graphics);
        renderAddonButtons(graphics);
        renderSideButtons(graphics);
    }''',
'''        renderEnergyTrough(graphics);
        renderAddonButtons(graphics);
        renderSideButtons(graphics);
        renderSidebar(graphics);
    }

    /**
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
    }

    private void renderSidebar(GuiGraphics graphics) {
        updateSidebarSize();
        int x = this.leftPos + SIDEBAR_X;
        int y = this.topPos + SIDEBAR_Y;

        if (this.sidebarWidth == SIDEBAR_MIN_WIDTH
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
    }

    /** Whether a point is over the side pane, in its current revealed size. */
    private boolean sidebarContains(double mouseX, double mouseY) {
        return inside(mouseX - this.leftPos, mouseY - this.topPos,
                SIDEBAR_X, SIDEBAR_Y, this.sidebarWidth, this.sidebarHeight);
    }''', 1)

# ---- 5. click toggles it ----
s = s.replace('''        if (button == 0) {
            int widget = widgetAt(mouseX, mouseY);''',
'''        if (button == 0 && sidebarContains(mouseX, mouseY)) {
            this.sidebarOpen = !this.sidebarOpen;
            return true;
        }
        if (button == 0) {
            int widget = widgetAt(mouseX, mouseY);''', 1)

# ---- 6. tooltip ----
s = s.replace('''        // The trough has no text of its own, so its numbers are only readable here.''',
'''        if (sidebarContains(mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.translatable(this.sidebarOpen
                            ? "gui.goldentweaks.chemical_reactor.sidebar.close"
                            : "gui.goldentweaks.chemical_reactor.sidebar.open"),
                    mouseX, mouseY);
            return;
        }

        // The trough has no text of its own, so its numbers are only readable here.''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('screen:', 'changed' if s != orig else 'NO CHANGE')
