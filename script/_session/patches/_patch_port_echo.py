import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- 1. pending record for the three counters ----
s = s.replace('''    @Nullable
    private String pendingName;''',
'''    @Nullable
    private String pendingName;

    /**
     * Values sent but not yet echoed back, keyed by field.
     *
     * <p>Same problem as the name: the port entity only reports a field once the packet lands, so
     * until then the echo read the old text and wrote it straight back into the box — the edit
     * appeared to bounce for a frame or two. Holding each sent value here keeps the box steady
     * until the server catches up, and it also covers a value the server normalises on save.</p>
     */
    private final Map<GTPortBlockEntity.Field, String> pendingFields =
            new EnumMap<>(GTPortBlockEntity.Field.class);''', 1)

# ---- 2. record what was sent ----
s = s.replace('''        if (!current.equals(value)) {
            PacketDistributor.sendToServer(new C2SPortTextPacket(port.getBlockPos(), field, value));
        }
    }''',
'''        if (!current.equals(value)) {
            // Compared against the in-flight value as well: after a rejected entry the box and the
            // port disagree, and comparing only with the port would treat the correction as
            // "unchanged" and send nothing.
            String baseline = this.pendingFields.containsKey(field)
                    ? this.pendingFields.get(field) : current;
            if (baseline.equals(value)) {
                return;
            }
            this.pendingFields.put(field, value);
            PacketDistributor.sendToServer(new C2SPortTextPacket(port.getBlockPos(), field, value));
        }
    }''', 1)

# ---- 3. echo through a helper that honours what is in flight ----
s = s.replace('''    private void syncChannelEcho() {
        if (this.channelField == null || this.channelField.isFocused()) {
            return;
        }
        GTPortBlockEntity port = this.menu.port();
        if (port == null) {
            return;
        }
        String stored = port.channelText();
        if (!stored.equals(this.channelField.getValue())) {
            this.channelField.setValue(stored);
        }

        if (this.nameField != null && !this.nameField.isFocused()) {''',
'''    private void syncChannelEcho() {
        if (this.channelField == null || this.channelField.isFocused()) {
            return;
        }
        GTPortBlockEntity port = this.menu.port();
        if (port == null) {
            return;
        }
        echo(this.modeField, GTPortBlockEntity.Field.MODE, port.modeText());
        echo(this.channelField, GTPortBlockEntity.Field.CHANNEL, port.channelText());
        echo(this.priorityField, GTPortBlockEntity.Field.PRIORITY, port.priorityText());

        if (this.nameField != null && !this.nameField.isFocused()) {''', 1)

# ---- 4. the helper ----
s = s.replace('''    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {''',
'''    /**
     * Writes a field's stored value into its box, unless a value is still in flight.
     *
     * <p>While a send is outstanding the box keeps the text the player typed: the port still
     * reports the old value, and writing that back is exactly what made it flash. Once the port
     * reports what was sent the pending record clears and ordinary echoing resumes — including a
     * normalised value, which simply differs from the sent one and so lands as the correction.</p>
     */
    private void echo(@Nullable EditBox box, GTPortBlockEntity.Field field, String stored) {
        if (box == null || box.isFocused()) {
            return;
        }
        if (this.pendingFields.containsKey(field)) {
            if (!stored.equals(this.pendingFields.get(field))) {
                return;
            }
            this.pendingFields.remove(field);
        }
        if (!stored.equals(box.getValue())) {
            box.setValue(stored);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {''', 1)

# ---- 5. imports ----
if 'import java.util.EnumMap;' not in s:
    s = s.replace('import java.util.List;', 'import java.util.EnumMap;\nimport java.util.List;', 1)
if 'import java.util.Map;' not in s:
    s = s.replace('import java.util.List;', 'import java.util.List;\nimport java.util.Map;', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('imports:', 'EnumMap' in s, 'Map;' in s)
