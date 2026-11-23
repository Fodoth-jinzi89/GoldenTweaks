"""Let a second machine reuse the menu with its own MenuType.

`super(MENU.get(), ...)` is the first statement of a constructor, so a subclass cannot override
it. A protected constructor taking the type solves it: the public ones keep the basic kettle's
type, and the advanced machine's factory passes its own.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    private GTChemicalReactorMenu(int containerId, Inventory inventory, Container container,
                                  @Nullable GTChemicalReactorMachineBlockEntity tile) {
        super(GTChemicalReactorMultiblock.MENU.get(), containerId);''',
'''    private GTChemicalReactorMenu(int containerId, Inventory inventory, Container container,
                                  @Nullable GTChemicalReactorMachineBlockEntity tile) {
        this(GTChemicalReactorMultiblock.MENU.get(), containerId, inventory, container, tile);
    }

    /**
     * The constructor a second machine uses.
     *
     * <p>The menu type is what tells the client which window class to open, so the advanced kettle
     * has to pass its own; everything below is shared.</p>
     */
    protected GTChemicalReactorMenu(MenuType<?> type, int containerId, Inventory inventory,
                                    Container container,
                                    @Nullable GTChemicalReactorMachineBlockEntity tile) {
        super(type, containerId);''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("menu takes its type")
