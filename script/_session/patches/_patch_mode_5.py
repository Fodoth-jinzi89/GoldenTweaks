import io
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s
old = """                // Not enough inputs or no room for the results; try again on a later tick.
                this.selectedChannel = null;
                return;"""
new = """                // Not enough inputs or no room for the results; try again on a later tick.
                this.selectedChannel = null;
                this.selectedMode = null;
                return;"""
s = s.replace(old, new, 1)
old2 = """        if (!startRecipe(nextBatched)) {
            this.selectedChannel = null;
            return;
        }"""
new2 = """        if (!startRecipe(nextBatched)) {
            this.selectedChannel = null;
            this.selectedMode = null;
            return;
        }"""
s = s.replace(old2, new2, 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('selectedMode nulls:', s.count('this.selectedMode = null;'))
