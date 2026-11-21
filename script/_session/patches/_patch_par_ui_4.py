import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ============ init: build both boxes ============
s = s.replace('''        this.overclockField = new EditBox(this.font, 0, 0,
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
        addRenderableWidget(this.overclockField);''',
'''        this.overclockPane.field = makePaneField(
                "gui.goldentweaks.chemical_reactor.sidebar.max",
                this.menu.getOverclockLimit());
        this.parallelPane.field = makePaneField(
                "gui.goldentweaks.chemical_reactor.sidebar.parallel.max",
                this.menu.getParallelLimit());''', 1)

# ============ the factory ============
s = s.replace('''    /** Sends the typed name if it differs from what the machine reports. */''',
'''    /**
     * Builds a side pane's input box.
     *
     * <p>Unbordered: the artwork draws the bar the box sits in. Two text colours, matching the port
     * window — white while being edited, a dimmer grey otherwise.</p>
     */
    private EditBox makePaneField(String labelKey, int initial) {
        EditBox box = new EditBox(this.font, 0, 0,
                SIDEBAR_FIELD_WIDTH, SIDEBAR_FIELD_HEIGHT,
                Component.translatable(labelKey));
        box.setMaxLength(SIDEBAR_FIELD_LIMIT);
        box.setBordered(false);
        box.setTextColor(SIDEBAR_FIELD_TEXT_COLOUR);
        box.setTextColorUneditable(SIDEBAR_FIELD_INACTIVE_COLOUR);
        box.setValue(String.valueOf(initial));
        // Typing only updates the box; the server hears about it on commit.
        box.setResponder(value -> {
        });
        box.setVisible(false);
        addRenderableWidget(box);
        return box;
    }

    /** Sends the typed name if it differs from what the machine reports. */''', 1)

# ============ echo ============
s = s.replace('''    private void syncOverclockEcho() {
        if (this.overclockField == null || this.overclockField.isFocused()) {
            return;
        }
        int stored = this.menu.getOverclockLimit();
        if (this.pendingOverclock != null) {
            if (stored != this.pendingOverclock) {
                return;
            }
            this.pendingOverclock = null;
        }
        String shown = String.valueOf(stored);
        if (!shown.equals(this.overclockField.getValue())) {
            this.overclockField.setValue(shown);
        }
    }''',
'''    private void syncOverclockEcho() {
        echoPane(this.overclockPane, this.menu.getOverclockLimit());
        echoPane(this.parallelPane, this.menu.getParallelLimit());
    }

    /** Writes a pane's stored number into its box, holding any value still in flight. */
    private void echoPane(Pane pane, int stored) {
        if (pane.field == null || pane.field.isFocused()) {
            return;
        }
        if (pane.pending != null) {
            if (stored != pane.pending) {
                return;
            }
            pane.pending = null;
        }
        String shown = String.valueOf(stored);
        if (!shown.equals(pane.field.getValue())) {
            pane.field.setValue(shown);
        }
    }''', 1)

# ============ commit ============
s = s.replace('''    private void commitOverclock() {
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

        // The box is rewritten to what will actually be stored. Showing the rejected text while the
        // machine holds something else is what made the control look unresponsive: the next attempt
        // would compare against the already-clamped value and send nothing.
        String normalised = String.valueOf(typed);
        if (!normalised.equals(this.overclockField.getValue())) {
            this.overclockField.setValue(normalised);
        }

        // Always sent, and always recorded. Comparing with the stored value alone skips the very
        // case that matters — text that clamps to what the machine already has, or a correction made
        // after an earlier value was still in flight.
        this.pendingOverclock = typed;
        PacketDistributor.sendToServer(
                new C2SMachineOverclockPacket(this.menu.getMachinePos(), typed));
    }''',
'''    private void commitOverclock() {
        commitPane(this.overclockPane, this.menu.getOverclockLimit(), -1);
        commitPane(this.parallelPane, this.menu.getParallelLimit(), 0);
    }

    /**
     * Reads one pane's box and sends it.
     *
     * <p>{@code fallback} is the value a garbled entry becomes, and also the floor: anything below
     * it is clamped rather than rejected, so the box always shows what will actually be stored. A
     * value the box and the machine disagree on is exactly what made the control look dead.</p>
     */
    private void commitPane(Pane pane, int stored, int fallback) {
        if (pane.field == null) {
            return;
        }
        int typed;
        try {
            typed = Integer.parseInt(pane.field.getValue().trim());
        } catch (NumberFormatException unparseable) {
            typed = fallback;
        }
        if (typed < fallback) {
            typed = fallback;
        }

        String normalised = String.valueOf(typed);
        if (!normalised.equals(pane.field.getValue())) {
            pane.field.setValue(normalised);
        }

        // Always sent and always recorded: comparing with the stored value alone skips the case that
        // matters, where the text clamps to what the machine already has, or where a correction
        // follows a value that is still in flight.
        pane.pending = typed;
        PacketDistributor.sendToServer(pane == this.overclockPane
                ? new C2SMachineOverclockPacket(this.menu.getMachinePos(), typed)
                : new C2SMachineParallelPacket(this.menu.getMachinePos(), typed));
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
