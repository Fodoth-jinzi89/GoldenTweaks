"""Dismantle gives back the controller it actually is.

`createHostStack` built the basic kettle's item, so pulling apart an advanced machine returned a
polyethylene controller — the wrong block, and the polypropylene casings it had been built from no
longer fit it.

The item is now resolved from the host block at the tile's position, which is what the machine
actually is. `asItem()` on that block gives the block's own registered item, so a third machine
needs no change here.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''    /**
     * The host's own item, carrying the anvil name the GUI title was showing.
     *
     * <p>Kept out of the matter cluster: a cluster cannot carry a custom name, and the name is
     * meant to survive taking the machine apart.</p>
     */
    private ItemStack createHostStack() {
        ItemStack host = new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get());
        if (this.customName != null) {
            host.set(DataComponents.CUSTOM_NAME, this.customName);
        }
        return host;
    }'''
new = '''    /**
     * The host's own item, carrying the anvil name the GUI title was showing.
     *
     * <p>Kept out of the matter cluster: a cluster cannot carry a custom name, and the name is
     * meant to survive taking the machine apart.</p>
     *
     * <p>The item comes from the block this tile sits in, not from a named constant: both kettles
     * share one block class but are two different blocks, and dismantling an advanced machine used
     * to hand back the basic controller.</p>
     */
    private ItemStack createHostStack() {
        ItemStack host = new ItemStack(getBlockState().getBlock().asItem());
        if (this.customName != null) {
            host.set(DataComponents.CUSTOM_NAME, this.customName);
        }
        return host;
    }'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("dismantle returns the machine's own controller")
