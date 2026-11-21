import io

# ---------------- 1. the run switch excludes a port from the craft, not from I/O ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** Whether a port may be used under the current selection. */
    private boolean portAllowed(MultiblockPortBinding port) {
        return portJoins(port, effectiveChannel());
    }''',
'''    /**
     * Whether a port takes part in the craft under the current selection.
     *
     * <p>Two independent filters, and both are necessary. The channel decides which production line
     * the port belongs to; the port's own run switch decides whether it is in use at all. A
     * switched-off port keeps its capability — a pipe may still fill or drain it — but the machine
     * neither draws from it nor delivers into it, which is what "taken out of service" means for a
     * part that sits in a shared wall.</p>
     */
    private boolean portAllowed(MultiblockPortBinding port) {
        if (this.level != null
                && this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity
                && !entity.isEnabled()) {
            return false;
        }
        return portJoins(port, effectiveChannel());
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('machine:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- 2. red icon back to u=65 ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''    private static final int ACTIVE_ICON_U_ACTIVE = 67;''',
'''    private static final int ACTIVE_ICON_U_ACTIVE = 65;''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('screen:', 'changed' if s2 != orig2 else 'NO CHANGE')
