import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# new field member
s = s.replace('''    private EditBox modeField;''',
'''    private EditBox nameField;
    private EditBox modeField;''', 1)

s = s.replace('''    private List<EditBox> fields = List.of();''',
'''    private List<EditBox> fields = List.of();''', 1)

# init: build the name box too, and register it as the first field
s = s.replace('''        this.modeField = makeField(GTPortBlockEntity.Field.MODE, this.menu.modeText());
        this.channelField = makeField(GTPortBlockEntity.Field.CHANNEL, this.menu.channelText());
        this.priorityField = makeField(GTPortBlockEntity.Field.PRIORITY, this.menu.priorityText());
        this.fields = List.of(this.modeField, this.channelField, this.priorityField);

        place(this.modeField, FIELD_MODE_Y);''',
'''        this.nameField = makeNameField();
        this.modeField = makeField(GTPortBlockEntity.Field.MODE, this.menu.modeText());
        this.channelField = makeField(GTPortBlockEntity.Field.CHANNEL, this.menu.channelText());
        this.priorityField = makeField(GTPortBlockEntity.Field.PRIORITY, this.menu.priorityText());
        this.fields = List.of(this.nameField, this.modeField, this.channelField, this.priorityField);

        placeNameField();
        place(this.modeField, FIELD_MODE_Y);''', 1)

# the name box is a different width and position from the three counters
s = s.replace('''    private void place(EditBox box, int y) {
        box.setX(this.leftPos + FIELD_X);
        box.setY(this.topPos + y);
        addRenderableWidget(box);
    }''',
'''    /**
     * The renaming box, laid over the name strip.
     *
     * <p>Same styling as the three counters but a different footprint: it spans the strip the title
     * used to be drawn in, so renaming replaces the label rather than sitting beside it.</p>
     */
    private EditBox makeNameField() {
        EditBox box = new EditBox(this.font, 0, 0, NAME_WIDTH, this.font.lineHeight,
                Component.translatable("gui.goldentweaks.port.name"));
        box.setMaxLength(GTPortBlockEntity.TEXT_LIMIT);
        box.setBordered(false);
        box.setTextColor(TEXT_COLOUR);
        box.setTextColorUneditable(INACTIVE_TEXT_COLOUR);
        box.setValue(this.menu.port() == null ? "" : this.menu.port().getDisplayName().getString());
        box.setResponder(value -> {
        });
        return box;
    }

    private void placeNameField() {
        this.nameField.setX(this.leftPos + NAME_X);
        this.nameField.setY(this.topPos + NAME_Y);
        addRenderableWidget(this.nameField);
    }

    private void place(EditBox box, int y) {
        box.setX(this.leftPos + FIELD_X);
        box.setY(this.topPos + y);
        addRenderableWidget(box);
    }''', 1)

# commit the name as well
s = s.replace('''    private void commit() {
        // Guarded because the boxes only exist after init(): a screen that is closed without ever
        // being initialised would otherwise reach for them here.
        if (this.modeField == null) {
            return;
        }''',
'''    private void commit() {
        // Guarded because the boxes only exist after init(): a screen that is closed without ever
        // being initialised would otherwise reach for them here.
        if (this.modeField == null) {
            return;
        }
        commitName();''', 1)

s = s.replace('''    private void send(GTPortBlockEntity.Field field, String value) {''',
'''    /**
     * Sends the name if it differs from what the port currently reports.
     *
     * <p>Compared against the entity's display name rather than a stored string: an empty box means
     * "no custom name", which reads back as the default wording, so the comparison has to be made
     * against that same wording.</p>
     */
    private void commitName() {
        GTPortBlockEntity port = this.menu.port();
        if (port == null) {
            return;
        }
        String typed = this.nameField.getValue();
        String shown = port.getDisplayName().getString();
        boolean clearing = typed.isBlank() && port.customName() != null;
        boolean renaming = !typed.isBlank() && !typed.equals(shown);
        if (clearing || renaming) {
            PacketDistributor.sendToServer(
                    new C2SPortTextPacket(port.getBlockPos(), GTPortBlockEntity.Field.NAME,
                            clearing ? "" : typed));
        }
    }

    private void send(GTPortBlockEntity.Field field, String value) {''', 1)

# keep the name box showing the stored value, like the channel echo
s = s.replace('''        String stored = port.channelText();
        if (!stored.equals(this.channelField.getValue())) {
            this.channelField.setValue(stored);
        }''',
'''        String stored = port.channelText();
        if (!stored.equals(this.channelField.getValue())) {
            this.channelField.setValue(stored);
        }

        if (this.nameField != null && !this.nameField.isFocused()) {
            String shown = port.getDisplayName().getString();
            if (!shown.equals(this.nameField.getValue())) {
                this.nameField.setValue(shown);
            }
        }''', 1)

# the title is now an editable box, so stop drawing it as a label
s = s.replace('''        drawMarquee(graphics, this.title, NAME_X, NAME_Y, NAME_WIDTH);
        if (this.menu.kind() != GTPortKind.ENERGY) {''',
'''        if (this.menu.kind() != GTPortKind.ENERGY) {''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
