import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- field ----
s = s.replace('''    /** The overclock ceiling's input box. */
    private EditBox overclockField;''',
'''    /** The overclock ceiling's input box. */
    private EditBox overclockField;

    /**
     * The value sent to the server and not yet read back, or {@code null}.
     *
     * <p>The round trip takes a few ticks, during which the machine still reports the old number.
     * Echoing that back would make the box flash the previous value before the edit lands, so it is
     * held at the sent value until the machine agrees.</p>
     */
    private Integer pendingOverclock;''', 1)

# ---- replace the inline echo with a helper call ----
s = s.replace('''        if (this.overclockField != null && !this.overclockField.isFocused()) {
            String shown = String.valueOf(this.menu.getOverclockLimit());
            if (!shown.equals(this.overclockField.getValue())) {
                this.overclockField.setValue(shown);
            }
        }''',
'''        syncOverclockEcho();''', 1)

# ---- the helper ----
s = s.replace('''    /** Sends the typed name if it differs from what the machine reports. */''',
'''    /**
     * Keeps the ceiling box showing what the machine will end up storing.
     *
     * <p>While a value is in flight the box keeps the sent text, because the machine still reports
     * the previous one for a few ticks and echoing it back is what made the box flash. Once the
     * machine agrees the pending flag clears and normal echoing resumes.</p>
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

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('pendingOverclock uses:', s.count('pendingOverclock'))
