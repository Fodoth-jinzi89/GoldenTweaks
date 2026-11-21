import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# pending-name field
s = s.replace('''    private double lastMouseX = -1;
    private double lastMouseY = -1;''',
'''    private double lastMouseX = -1;
    private double lastMouseY = -1;

    /**
     * A name that has been sent but not yet echoed back by the server.
     *
     * <p>Without this the box flickered: the packet takes a round trip, and until it lands the local
     * entity still reports the old name, so the echo below would write that old value straight back
     * into the field and the player would see their edit vanish for a frame or two. Holding the sent
     * value here lets the box ignore the stale reading until the server catches up.</p>
     */
    @Nullable
    private String pendingName;''', 1)

# record the sent value
s = s.replace('''        if (clearing || renaming) {
            PacketDistributor.sendToServer(
                    new C2SPortTextPacket(port.getBlockPos(), GTPortBlockEntity.Field.NAME,
                            clearing ? "" : typed));
        }''',
'''        if (clearing || renaming) {
            this.pendingName = clearing ? "" : typed;
            PacketDistributor.sendToServer(
                    new C2SPortTextPacket(port.getBlockPos(), GTPortBlockEntity.Field.NAME,
                            clearing ? "" : typed));
        }''', 1)

# respect the pending value in the echo
s = s.replace('''        if (this.nameField != null && !this.nameField.isFocused()) {
            String shown = port.getDisplayName().getString();
            if (!shown.equals(this.nameField.getValue())) {
                this.nameField.setValue(shown);
            }
        }''',
'''        if (this.nameField != null && !this.nameField.isFocused()) {
            String shown = port.getDisplayName().getString();
            if (this.pendingName != null) {
                // Ignore the entity until it reports what was sent. An empty pending value means the
                // name was cleared, which reads back as the block's default wording.
                boolean landed = this.pendingName.isEmpty()
                        ? port.customName() == null
                        : shown.equals(this.pendingName);
                if (landed) {
                    this.pendingName = null;
                }
                return;
            }
            if (!shown.equals(this.nameField.getValue())) {
                this.nameField.setValue(shown);
            }
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
