import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- 1. the flag ----
s = s.replace('''    private boolean running;''',
'''    private boolean running;

    /**
     * True when this entity was just read back from disk and has not yet re-scanned its shape.
     *
     * <p>The scan that runs after a load looks exactly like the scan after a player finishes
     * building the machine, but the two must not do the same thing: a machine switched off by hand
     * has to stay off. This flag is what tells them apart — set in {@link #loadAdditional}, cleared
     * the first time {@link #formStructure} succeeds.</p>
     */
    private boolean restoredFromDisk;''', 1)

# ---- 2. formStructure honours it ----
s = s.replace('''        // Assembling switches the machine on — but only when the player just built it. A chunk
        // reload re-runs this same scan for a machine that was already formed and switched off by
        // hand, and setting the flag unconditionally would quietly turn it back on every time the
        // world loads. The block's formed property records what the machine was before this scan,
        // so an already-formed shape keeps whatever the switch said.
        boolean wasFormed = getBlockState().getValue(GTChemicalReactorMachineBlock.FORMED);
        if (!wasFormed) {
            this.running = true;
        }''',
'''        // Assembling switches the machine on — but only when the player just built it. The scan
        // that runs after a chunk load is indistinguishable from the one after the last block is
        // placed, so `restoredFromDisk` is what separates them: a machine read back from disk keeps
        // whatever the switch said, including "off", instead of switching itself on at every load.
        if (this.restoredFromDisk) {
            this.restoredFromDisk = false;
        } else {
            this.running = true;
        }''', 1)

# ---- 3. set it on load ----
s = s.replace('''        this.running = tag.getBoolean(TAG_RUNNING);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {''',
'''        this.running = tag.getBoolean(TAG_RUNNING);
        // The shape has to be re-resolved after a load, and that scan must not switch the machine on.
        this.restoredFromDisk = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('flag uses:', s.count('restoredFromDisk'))
