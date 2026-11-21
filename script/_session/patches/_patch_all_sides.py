import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * Whether {@code side} may use this port.
     *
     * <p>Only the front face is exposed to the world. A port sits embedded in the shell, so the
     * other five sides are inside the wall and must not offer a capability, or a pipe touching the
     * back of the machine would feed it.</p>
     *
     * <p>A {@code null} side means "no particular side", which is how the multiblock itself asks:
     * {@code MultiblockPortType.capability} resolves through {@code level.getCapability(cap, pos,
     * null)}. Rejecting null therefore made the machine see every port as empty — no inputs were
     * drawn, nothing was delivered, and no energy could be taken.</p>
     */
    public boolean isFront(@Nullable Direction side) {
        return side == null || side == getBlockState().getValue(GTPortBlock.FACING);
    }

    /**
     * Whether a capability may be offered at all.
     *
     * <p>A switched-off port answers nothing, which is how the run button takes it out of the
     * machine: the multiblock resolves its ports through capabilities, so no handler means no
     * input and no output.</p>
     */
    public boolean offersCapability(@Nullable Direction side) {
        return this.enabled && isFront(side);
    }''',
'''    /**
     * The face this port points at.
     *
     * <p>Not a restriction: the port is reachable from every side. The setting exists only to give
     * the active mode somewhere to reach — an active port pulls from, or pushes into, whatever block
     * this face touches.</p>
     */
    public Direction front() {
        return getBlockState().getValue(GTPortBlock.FACING);
    }

    /**
     * Whether a capability is offered to the outside world.
     *
     * <p>Every side is open. Passive input and output ignore both the facing and the run switch:
     * they are what the port is, and the machine resolves its ports through these capabilities, so
     * refusing a side would simply hide the port from the block next to it.</p>
     */
    public boolean offersCapability(@Nullable Direction side) {
        return true;
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('isFront refs left:', s.count('isFront'))
