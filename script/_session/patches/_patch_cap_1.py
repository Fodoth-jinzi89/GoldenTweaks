import io

# ---------- machine: a tier-only ceiling, distinct from the setting-aware one ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * The most steps this machine may apply, before a recipe has a say.
     *
     * <p>Answered by the tier and the player's setting alone: {@code -1} means "as far as the tier
     * goes", anything else is the player's own ceiling. What a given recipe can actually afford is
     * a separate question, answered per recipe by {@link #overclockSteps(int)} — a step quadruples
     * the bill, so a costly recipe may only reach a fraction of this number.</p>
     */
    public int reachableOverclock() {''',
'''    /**
     * The most steps the player may ask for, ignoring their current setting.
     *
     * <p>This is the number the input box is capped at. Deliberately not the setting-aware figure
     * below: capping a <em>setting</em> against a value derived from that same setting would make
     * the control bite its own tail — lowering it once would forbid raising it again.</p>
     */
    public int overclockCeiling() {
        return maxOverclock();
    }

    /**
     * The most steps this machine may apply, before a recipe has a say.
     *
     * <p>Answered by the tier and the player's setting alone: {@code -1} means "as far as the tier
     * goes", anything else is the player's own ceiling. What a given recipe can actually afford is
     * a separate question, answered per recipe by {@link #overclockSteps(int)} — a step quadruples
     * the bill, so a costly recipe may only reach a fraction of this number.</p>
     */
    public int reachableOverclock() {''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('machine:', 'changed' if s != orig else 'NO CHANGE')

# ---------- menu ----------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''    /** The highest overclock this machine's energy hatches can support at all. */
    public int getReachableOverclock() {
        return this.tile == null ? 0 : this.tile.reachableOverclock();
    }''',
'''    /** The highest overclock this machine's energy hatches can support at all. */
    public int getReachableOverclock() {
        return this.tile == null ? 0 : this.tile.reachableOverclock();
    }

    /** The cap on what the overclock box may hold, independent of the setting it edits. */
    public int getOverclockCeiling() {
        return this.tile == null ? 0 : this.tile.overclockCeiling();
    }''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('menu:', 'changed' if s2 != orig2 else 'NO CHANGE')

# ---------- screen: cap against the tier ceiling ----------
p3 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s3 = io.open(p3, encoding='utf-8').read()
orig3 = s3
s3 = s3.replace('''        if (pane == this.overclockPane && typed >= 0) {
            int reachable = this.menu.getReachableOverclock();
            if (typed > reachable) {
                typed = reachable;
            }
        }''',
'''        if (pane == this.overclockPane && typed >= 0) {
            int ceiling = this.menu.getOverclockCeiling();
            if (typed > ceiling) {
                typed = ceiling;
            }
        }''', 1)
io.open(p3, 'w', encoding='utf-8', newline='\n').write(s3)
print('screen:', 'changed' if s3 != orig3 else 'NO CHANGE')
