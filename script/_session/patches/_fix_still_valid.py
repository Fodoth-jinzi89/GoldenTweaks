"""Fix the window closing on the advanced machine.

`GTChemicalReactorMenu.stillValid` verified the block at the tile's position was the *basic*
kettle. The advanced machine shares the block class but not the id, so the check failed on the
first tick and vanilla closed the window it had just opened — which is the "opens then shuts
immediately" symptom.

The check now accepts any host of the shared block class. Naming a class rather than an id is
what makes it machine-agnostic: a dismantled host is replaced by air or a casing block, neither of
which is this class, so the teardown case still closes the window as intended.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java"
s = io.open(p, encoding="utf-8").read()

old = '''            if (this.tile.getLevel() != null
                    && !this.tile.getLevel().getBlockState(this.tile.getBlockPos())
                            .is(GTChemicalReactorMultiblock.MACHINE.get())) {
                return false;
            }'''
new = '''            // The host block class, not one machine's id: both kettles share the class, so naming
            // the basic id invalidated the advanced window on the tick it opened and vanilla shut
            // it again. A dismantled host becomes air or casing, neither of that class, so the
            // teardown case still closes the window.
            if (this.tile.getLevel() != null
                    && !(this.tile.getLevel().getBlockState(this.tile.getBlockPos())
                            .getBlock() instanceof GTChemicalReactorMachineBlock)) {
                return false;
            }'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("stillValid accepts any host of the shared class")
