import io

# ---------------- 1. a third state bit ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlock.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    public static final BooleanProperty RUNNING = BooleanProperty.create("running");''',
'''    public static final BooleanProperty RUNNING = BooleanProperty.create("running");

    /**
     * Whether a reaction is being processed right now.
     *
     * <p>A third bit, because the machine has three distinct states and the front overlay has to
     * tell them apart: assembled-and-switched-on, assembled-and-crafting, assembled-and-switched-
     * off. {@link #RUNNING} alone answers the first and the third, so this one answers the second —
     * a player glancing at the kettle wants to know whether work is happening, not merely whether
     * the switch is up.</p>
     */
    public static final BooleanProperty CRAFTING = BooleanProperty.create("crafting");''', 1)

s = s.replace('''                .setValue(RUNNING, false)''',
'''                .setValue(RUNNING, false)
                .setValue(CRAFTING, false)''', 1)

s = s.replace('''        builder.add(FACING, MIRRORED, RUNNING, ROLL, FORMED);''',
'''        builder.add(FACING, MIRRORED, RUNNING, CRAFTING, ROLL, FORMED);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('block:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- 2. write it from the entity ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

s2 = s2.replace('''        BlockState desired = state
                .setValue(GTChemicalReactorMachineBlock.FORMED, formed)
                .setValue(GTChemicalReactorMachineBlock.RUNNING, running);''',
'''        BlockState desired = state
                .setValue(GTChemicalReactorMachineBlock.FORMED, formed)
                .setValue(GTChemicalReactorMachineBlock.RUNNING, running)
                // Crafting is the state the texture's middle look is for: assembled and switched on
                // but with nothing to do reads as idle, and only a live reaction reads as working.
                .setValue(GTChemicalReactorMachineBlock.CRAFTING,
                        formed && this.activeRecipe != null);''', 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('entity:', 'changed' if s2 != orig2 else 'NO CHANGE')
