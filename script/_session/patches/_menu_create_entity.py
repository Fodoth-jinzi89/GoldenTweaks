"""Add the create overload that takes the entity directly.

`createMenu` already holds the tile, so routing through a block position would look it up again
for nothing.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    /** Server side constructor, used by {@link GTChemicalReactorMachineBlockEntity#createMenu}. */''',
'''    /**
     * The same, for a caller that already holds the tile.
     *
     * <p>{@code createMenu} is handed the entity, so looking it up by position again would be a
     * redundant walk of the chunk.
     */
    public static GTChemicalReactorMenu create(MenuType<?> type, int containerId,
                                               Inventory inventory,
                                               GTChemicalReactorMachineBlockEntity tile) {
        return new GTChemicalReactorMenu(type, containerId, inventory, tile, tile);
    }

    /** Server side constructor, used by {@link GTChemicalReactorMachineBlockEntity#createMenu}. */''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("entity-based create added")
