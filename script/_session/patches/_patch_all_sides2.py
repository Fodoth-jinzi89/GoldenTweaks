import io, re

# ---------------- capabilities: the gate is gone, so register unconditionally ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortCapabilities.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace(' && port.offersCapability(side)', '')
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('capabilities:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- entity: drop the always-true gate and use front() ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

s2 = s2.replace('''    /**
     * Whether a capability is offered to the outside world.
     *
     * <p>Every side is open. Passive input and output ignore both the facing and the run switch:
     * they are what the port is, and the machine resolves its ports through these capabilities, so
     * refusing a side would simply hide the port from the block next to it.</p>
     */
    public boolean offersCapability(@Nullable Direction side) {
        return true;
    }
''', '')

s2 = s2.replace('''        Direction front = getBlockState().getValue(GTPortBlock.FACING);''',
'''        Direction front = front();''', 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('entity:', 'changed' if s2 != orig2 else 'NO CHANGE')
