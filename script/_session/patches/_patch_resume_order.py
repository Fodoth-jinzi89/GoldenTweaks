import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# The gate has to sit above every other check, including `structure == null`: a machine that has
# just come back from disk can have a shape but no ports yet, and that is exactly the window in which
# it would otherwise match a recipe and take ingredients it is about to be told to give back.
s = s.replace('''    @Override
    protected void tickRecipe(Level level) {
        if (this.structure == null) {
            return;
        }
''',
'''    @Override
    protected void tickRecipe(Level level) {
        if (this.settleTicks > 0) {
            this.settleTicks--;
            return;
        }
        if (this.structure == null) {
            return;
        }
''', 1)

# The resume must run before the base tick ever gets a chance to start something, so it is pulled out
# of the formed branch and put ahead of the super call.
s = s.replace('''    protected void serverTick(Level level) {
        super.serverTick(level);
''',
'''    protected void serverTick(Level level) {
        // Before the base tick, not after it. AMI's tick is what starts recipes, so resuming only
        // afterwards meant the machine had already committed ingredients to a fresh craft by the time
        // the saved one was looked for — and that is the batch that went missing.
        resumePendingRecipe(level);
        super.serverTick(level);
''', 1)

s = s.replace('''        if (isFormed()) {
            // The shape exists now, so a craft parked by the last save can be looked up and resumed.
            resumePendingRecipe(level);
            return;
        }''',
'''        if (isFormed()) {
            return;
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
