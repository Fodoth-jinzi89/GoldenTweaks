import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# 1) first start site: pin the group the recipe was found in
old1 = """            MatchedMultiblockRecipe batched = batchRecipe(found);
            if (!startRecipe(batched)) {
                // Not enough inputs or no room for the results; try again on a later tick.
                return;
            }"""
new1 = """            MatchedMultiblockRecipe batched = batchRecipe(found);
            if (!startRecipe(batched)) {
                // Not enough inputs or no room for the results; try again on a later tick.
                this.selectedChannel = null;
                return;
            }
            // Pin the group this recipe came from. Everything the craft does from here — drawing
            // energy, delivering products — stays inside it, so a later rearrangement of the ports
            // cannot move the results to a different production line.
            this.activeChannel = this.selectedChannel;"""

# 2) second start site (the tick that completes and restarts in one pass)
old2 = """        MatchedMultiblockRecipe nextBatched = batchRecipe(next);
        if (!startRecipe(nextBatched)) {
            return;
        }
        consumeInputs(nextBatched);"""
new2 = """        MatchedMultiblockRecipe nextBatched = batchRecipe(next);
        if (!startRecipe(nextBatched)) {
            this.selectedChannel = null;
            return;
        }
        this.activeChannel = this.selectedChannel;
        consumeInputs(nextBatched);"""

# 3) clearing: completeRecipe
old3 = """        applyRunningState();
        // The GUI stops listing the outputs as soon as nothing is running."""
new3 = """        applyRunningState();
        // The craft is over, so no group owns the machine any more and the next search is free to
        // pick whichever one matches.
        this.activeChannel = null;
        this.selectedChannel = null;
        // The GUI stops listing the outputs as soon as nothing is running."""


def apply(old, new, label):
    global s
    if old not in s:
        print('MISS', label)
        return
    s = s.replace(old, new, 1)
    print('ok', label)


apply(old1, new1, 'start-1')
apply(old2, new2, 'start-2')
apply(old3, new3, 'complete')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
