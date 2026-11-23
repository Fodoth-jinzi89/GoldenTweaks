"""Give the advanced kettle a factory that passes its own menu type."""
import io

# ---------------- a factory on the menu ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    /** Server side constructor, used by {@link GTChemicalReactorMachineBlockEntity#createMenu}. */''',
'''    /**
     * Opens the window for a machine that is not the basic kettle.
     *
     * <p>The menu type is what the client uses to pick a screen class, so each machine's factory
     * has to name its own. Resolving the tile happens the same way it does for the basic one.</p>
     */
    public static GTChemicalReactorMenu create(MenuType<?> type, int containerId,
                                               Inventory inventory, BlockPos pos) {
        return new GTChemicalReactorMenu(type, containerId, inventory,
                resolveContainer(inventory, pos), resolveTile(inventory, pos));
    }

    /** Server side constructor, used by {@link GTChemicalReactorMachineBlockEntity#createMenu}. */''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("menu factory added")

# ---------------- the advanced registration uses it ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactorMultiblock.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''                            new GTChemicalReactorMenu(containerId, inventory, data.readBlockPos())));''',
'''                            GTChemicalReactorMenu.create(MENU.get(), containerId, inventory,
                                    data.readBlockPos())));''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("advanced menu uses its own type")
