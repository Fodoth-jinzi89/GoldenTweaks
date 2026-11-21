import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# tripBreaker: release the lock too, since the craft is gone
s = s.replace("""        this.running = false;
        this.stopReason = reason;""",
"""        this.running = false;
        this.stopReason = reason;
        // The craft is gone, so the ports are editable again.
        this.activeChannel = null;
        this.selectedChannel = null;
        setChannelsLocked(false);""", 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
