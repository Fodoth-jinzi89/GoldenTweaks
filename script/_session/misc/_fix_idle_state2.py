import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''     * <p>{@code RUNNING} means "a reaction is being processed", not "the switch is on". Those are
     * different questions and the texture answers the first one: an assembled, switched-on machine
     * with nothing to craft shows the idle look, which is what tells the player at a glance whether
     * work is happening. The switch itself is drawn by the window's own button.</p>
     */
    private void applyRunningState() {
        if (this.level == null) {
            return;
        }
        boolean formed = isFormed();
        // Only a craft in flight counts. A switched-off machine keeps the look while its last batch
        // finishes, because work really is still happening.
        boolean running = formed && this.activeRecipe != null;
''',
'''     * <p>{@code RUNNING} mirrors the switch, not the craft. Those are three different states and the
     * block state only has room for two of them, so the texture reports what a player glancing over
     * wants to know first: whether the machine is working again. Assembled and switched on shows the
     * idle look, assembled and switched off shows the off look, and the craft in progress is the
     * window's own business — its status line and progress bar already say so.</p>
     *
     * <p>A switched-off machine keeps the running look while its last batch finishes, because work
     * really is still happening.</p>
     */
    private void applyRunningState() {
        if (this.level == null) {
            return;
        }
        boolean formed = isFormed();
        // The switch, plus a craft still in flight after the switch went off.
        boolean running = formed && (this.running || this.activeRecipe != null);
''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')

# The blockstate mapping was already right for this reading: formed+on -> idle, formed+off -> off.
