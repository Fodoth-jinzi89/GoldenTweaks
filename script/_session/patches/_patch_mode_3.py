import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------- pin the mode alongside the channel when a craft starts ----------
n = s.count('''            this.activeChannel = this.selectedChannel;''')
s = s.replace('''            this.activeChannel = this.selectedChannel;''',
              '''            this.activeChannel = this.selectedChannel;
            this.activeMode = this.selectedMode;''')
print('start pins:', n)

n2 = s.count('''        this.activeChannel = this.selectedChannel;''')
# the second occurrence (plus-fallthrough path) is written with 8 spaces
old2 = '''        this.activeChannel = this.selectedChannel;
        this.portShuffle = new Random(level.getRandom().nextLong());'''
new2 = '''        this.activeChannel = this.selectedChannel;
        this.activeMode = this.selectedMode;
        this.portShuffle = new Random(level.getRandom().nextLong());'''
s = s.replace(old2, new2, 1)
print('fallthrough pins:', n2)

# ---------- release both together wherever activeChannel is cleared ----------
before = s.count('this.activeChannel = null;')
s = s.replace('''        this.activeChannel = null;''',
              '''        this.activeChannel = null;
        this.activeMode = null;''')
s = s.replace('''            this.activeChannel = null;''',
              '''            this.activeChannel = null;
            this.activeMode = null;''')
print('clears:', before)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
