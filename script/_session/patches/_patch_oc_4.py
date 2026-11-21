import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** The circuit this pane describes, as written on the item. */
    private static final String SIDEBAR_OVERCLOCK_RATIO = "4/2";''',
'''    /** The circuit this pane describes, as written on the item. */
    private static final String SIDEBAR_OVERCLOCK_RATIO = "4/2";

    /**
     * The ceiling's input box, drawn by the artwork on its third band.
     *
     * <p>The black bar in the sprite is the field itself, so nothing here draws a frame: the box is
     * placed inside it and left unbordered, exactly like the port window's own fields.</p>
     */
    private static final int SIDEBAR_FIELD_X = 4;
    private static final int SIDEBAR_FIELD_Y = 25;
    private static final int SIDEBAR_FIELD_WIDTH = 48;
    private static final int SIDEBAR_FIELD_HEIGHT = 9;
    private static final int SIDEBAR_FIELD_TEXT_COLOUR = 0xFFFFFF;
    /** Matches the port window: dimmer while the box is not the one being edited. */
    private static final int SIDEBAR_FIELD_INACTIVE_COLOUR = 0x8A8A8A;
    /** Longest accepted text; an integer never needs more than this. */
    private static final int SIDEBAR_FIELD_LIMIT = 11;''', 1)

# the readout must stay clear of the input band
s = s.replace('''    private static final int SIDEBAR_TEXT_Y = SIDEBAR_SLOT_EXPANDED_Y + 1;''',
'''    private static final int SIDEBAR_TEXT_Y = SIDEBAR_SLOT_EXPANDED_Y + 1;
    /** The two lines stop above the artwork's input band, which starts at {@link #SIDEBAR_FIELD_Y}. */''', 1)

# field declaration
s = s.replace('''    /** Current revealed size, animated between the collapsed and expanded bounds. */
    private int sidebarWidth = SIDEBAR_MIN_WIDTH;
    private int sidebarHeight = SIDEBAR_MIN_HEIGHT;''',
'''    /** Current revealed size, animated between the collapsed and expanded bounds. */
    private int sidebarWidth = SIDEBAR_MIN_WIDTH;
    private int sidebarHeight = SIDEBAR_MIN_HEIGHT;

    /** The overclock ceiling's input box. */
    private EditBox overclockField;''', 1)

# build in init
s = s.replace('''        this.nameField.setValue(this.menu.getDisplayName().getString());
        addRenderableWidget(this.nameField);''',
'''        this.nameField.setValue(this.menu.getDisplayName().getString());
        addRenderableWidget(this.nameField);

        this.overclockField = new EditBox(this.font, 0, 0,
                SIDEBAR_FIELD_WIDTH, SIDEBAR_FIELD_HEIGHT,
                Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.max"));
        this.overclockField.setMaxLength(SIDEBAR_FIELD_LIMIT);
        // No border: the artwork already draws the bar this box sits in.
        this.overclockField.setBordered(false);
        this.overclockField.setTextColor(SIDEBAR_FIELD_TEXT_COLOUR);
        this.overclockField.setTextColorUneditable(SIDEBAR_FIELD_INACTIVE_COLOUR);
        this.overclockField.setValue(String.valueOf(this.menu.getOverclockLimit()));
        this.overclockField.setResponder(value -> {
        });
        this.overclockField.setVisible(false);
        addRenderableWidget(this.overclockField);''', 1)

# commit
s = s.replace('''    @Override
    public void onClose() {
        commitName();
        super.onClose();
    }''',
'''    /**
     * Sends the typed ceiling, if it differs from what the machine reports.
     *
     * <p>Anything that does not parse as an integer is sent as {@code -1}, the unlimited value.
     * That is the field's own default, so a garbled entry leaves the machine as it was rather than
     * with a ceiling nobody meant.</p>
     */
    private void commitOverclock() {
        if (this.overclockField == null) {
            return;
        }
        int typed;
        try {
            typed = Integer.parseInt(this.overclockField.getValue().trim());
        } catch (NumberFormatException unparseable) {
            typed = -1;
        }
        if (typed < -1) {
            typed = -1;
        }
        if (typed != this.menu.getOverclockLimit()) {
            PacketDistributor.sendToServer(
                    new C2SMachineOverclockPacket(this.menu.getMachinePos(), typed));
        }
    }

    @Override
    public void onClose() {
        commitName();
        commitOverclock();
        super.onClose();
    }''', 1)

# place and reveal with the pane
s = s.replace('''    private void renderSidebar(GuiGraphics graphics) {
        int x = this.leftPos + SIDEBAR_X;
        int y = this.topPos + SIDEBAR_Y;''',
'''    private void renderSidebar(GuiGraphics graphics) {
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
        }''', 1)

# commit on click elsewhere / enter
s = s.replace('''        if (this.nameField != null && !this.nameField.isMouseOver(mouseX, mouseY)) {
            commitName();
        }''',
'''        if (this.nameField != null && !this.nameField.isMouseOver(mouseX, mouseY)) {
            commitName();
        }
        if (this.overclockField != null && this.overclockField.isVisible()
                && !this.overclockField.isMouseOver(mouseX, mouseY)) {
            commitOverclock();
        }''', 1)

s = s.replace('''        if ((keyCode == 257 || keyCode == 335)
                && this.nameField != null && this.nameField.isFocused()) {
            commitName();
            this.nameField.setFocused(false);
            return true;
        }''',
'''        if ((keyCode == 257 || keyCode == 335)
                && this.nameField != null && this.nameField.isFocused()) {
            commitName();
            this.nameField.setFocused(false);
            return true;
        }
        if ((keyCode == 257 || keyCode == 335)
                && this.overclockField != null && this.overclockField.isFocused()) {
            commitOverclock();
            this.overclockField.setFocused(false);
            return true;
        }''', 1)

# keep in step
s = s.replace('''        if (this.nameField != null && !this.nameField.isFocused()) {
            String shown = this.menu.getDisplayName().getString();
            if (!shown.equals(this.nameField.getValue())) {
                this.nameField.setValue(shown);
            }
        }''',
'''        if (this.nameField != null && !this.nameField.isFocused()) {
            String shown = this.menu.getDisplayName().getString();
            if (!shown.equals(this.nameField.getValue())) {
                this.nameField.setValue(shown);
            }
        }
        if (this.overclockField != null && !this.overclockField.isFocused()) {
            String shown = String.valueOf(this.menu.getOverclockLimit());
            if (!shown.equals(this.overclockField.getValue())) {
                this.overclockField.setValue(shown);
            }
        }''', 1)

# the ceiling line reads the stored value
s = s.replace('''        graphics.drawString(this.font,
                Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.max",
                        Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.infinite")),
                0, SIDEBAR_TEXT_LINE_HEIGHT, SIDEBAR_TEXT_COLOUR, false);''',
'''        // Read from the stored value, so the line and the box below it can never disagree.
        this.font.getClass();
        graphics.drawString(this.font,
                Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.max",
                        maxLabel(this.menu.getOverclockLimit())),
                0, SIDEBAR_TEXT_LINE_HEIGHT, SIDEBAR_TEXT_COLOUR, false);''', 1)

# helper
s = s.replace('''    /** The control circuit's icon, in the pane's slot. */''',
'''    /** The ceiling as the readout shows it: the number, or "unlimited" for the sentinel. */
    private static Component maxLabel(int limit) {
        return limit < 0
                ? Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.infinite")
                : Component.literal(String.valueOf(limit));
    }

    /** The control circuit's icon, in the pane's slot. */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
