import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''        String unit = this.menu.kind() == GTPortKind.FLUID ? " mB" : "";
        String amount = GTEnergyFormat.formatExpanded(this.menu.syncedAmount())
                + " / " + GTEnergyFormat.formatExpanded(this.menu.syncedCapacity()) + unit;''',
'''        // Compact rather than every digit: a high-tier hatch holds hundreds of thousands of mB, and
        // the exact figure is unreadable at this size. The same ladder the energy port uses, so a
        // number reads the same way across the machine's windows.
        String unit = this.menu.kind() == GTPortKind.FLUID ? " mB" : "";
        String amount = GTEnergyFormat.format(this.menu.syncedAmount())
                + " / " + GTEnergyFormat.format(this.menu.syncedCapacity()) + unit;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
