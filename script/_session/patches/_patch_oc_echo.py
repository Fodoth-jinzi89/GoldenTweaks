import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- 1. pending echo value ----
s = s.replace('''    /** The overclock ceiling's input box. */
    private EditBox overclockField;''',
'''    /** The overclock ceiling's input box. */
    private EditBox overclockField;

    /**
     * What was sent to the server and has not been read back yet, or {@code null}.
     *
     * <p>The round trip takes a few ticks. Without this the screen keeps re-reading the old stored
     * value meanwhile and overwrites the box with it, which looks like the edit bounced back before
     * landing. Same shape as the port window's pending name.</p>
     */
    private Integer pendingOverclock;''', 1)

# ---- 2. commit: record what was sent, and always send ----
s = s.replace('''        if (typed != this.menu.getOverclockLimit()) {
            PacketDistributor.sendToServer(
                    new C2SMachineOverclockPacket(this.menu.getMachinePos(), typed));
        }
    }''',
'''        // Compared against the pending value as well as the stored one. A rejected entry leaves the
        // box and the machine disagreeing, and comparing only with the machine would then treat the
        // corrected text as "unchanged" and send nothing — the box would look dead until the window
        // was reopened.
        int baseline = this.pendingOverclock != null
                ? this.pendingOverclock : this.menu.getOverclockLimit();
        if (typed != baseline) {
            this.pendingOverclock = typed;
            PacketDistributor.sendToServer(
                    new C2SMachineOverclockPacket(this.menu.getMachinePos(), typed));
        }
    }''', 1)

# ---- 3. echo: hold the typed value until the server reports it ----
s = s.replace('''        if (this.overclockField != null && !this.overclockField.isFocused()) {
            String shown = String.valueOf(this.menu.getOverclockLimit());
            if (!shown.equals(this.overclockField.getValue())) {
                this.overclockField.setValue(shown);
            }
        }''',
'''        syncOverclockEcho();''', 1)

# ---- 4. the echo helper, next to syncOverclockEcho's siblings ----
s = s.replace('''    /** Sends the typed name if it differs from what the machine reports. */''',
'''    /**
     * Keeps the ceiling box showing what the machine will end up storing.
     *
     * <p>While a value is in flight the box keeps the typed text: the stored value is still the old
     * one for a few ticks, and writing it back would show the previous number again. Once the
     * machine reports the value that was sent, the pending flag clears and normal echoing resumes —
     * including for a rejected entry, which comes back as the {@code -1} the server clamped it to.</p>
     */
    private void syncOverclockEcho() {
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
    }

    /** Sends the typed name if it differs from what the machine reports. */''', 1)

# ---- 5. also echo on close, so a reopen shows the truth ----
s = s.replace('''    private void commitOverclock() {
        if (this.overclockField == null) {
            return;
        }''',
'''    private void commitOverclock() {
        if (this.overclockField == null) {
            return;
        }
        // Nothing to resolve when the server never disagreed in the first place.
        if (this.pendingOverclock == null
                && this.overclockField.getValue().trim().isEmpty()) {
            return;
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
