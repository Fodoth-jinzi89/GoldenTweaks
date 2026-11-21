import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''     * is the player's only indication that the last batch is still being processed; it drops to the
     * off look once that batch completes.</p>
     */
    private void applyRunningState() {
        if (this.level == null) {
            return;
        }
        boolean formed = isFormed();
        // A stopped machine must not keep claiming it is running, and an unformed one has nothing
        // to run at all �?but a batch in flight runs to completion.
        boolean running = formed && (this.running || this.activeRecipe != null);
''',
'''     * is the player's only indication that the last batch is still being processed; it drops to the
     * off look once that batch completes.</p>
     *
     * <p>{@code RUNNING} means "a reaction is being processed", not "the switch is on". Those are
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
''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
