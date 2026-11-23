"""Temporary diagnostics for the advanced window closing on open.

Prints, on both sides, which menu type is actually being used, so the mismatch is visible instead
of inferred from behaviour.
"""
import io

# ---- server: what type goes out ---------------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactorBlockEntity.java"
s = io.open(p, encoding="utf-8").read()
s = s.replace('''    @Override
    protected MenuType<?> menuType() {
        return GTAdvancedChemicalReactorMultiblock.MENU.get();
    }''',
'''    @Override
    protected MenuType<?> menuType() {
        MenuType<?> type = GTAdvancedChemicalReactorMultiblock.MENU.get();
        net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                "[GT/diag] advanced createMenu uses type={} (advanced holder present={})",
                type, GTAdvancedChemicalReactorMultiblock.MENU.isPresent());
        return type;
    }''', 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("server diag added")

# ---- client: what got registered --------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorClientEvents.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''        event.register(GTChemicalReactorMultiblock.MENU.get(), GTChemicalReactorScreen::new);''',
'''        net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                "[GT/diag] registering screens: basic={} advanced={}",
                GTChemicalReactorMultiblock.MENU.get(), GTAdvancedChemicalReactorMultiblock.MENU.get());
        event.register(GTChemicalReactorMultiblock.MENU.get(), GTChemicalReactorScreen::new);''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("client diag added")

# ---- and the menu itself, so a wrong type is caught at construction ----------------------------
p3 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java"
s3 = io.open(p3, encoding="utf-8").read()
s3 = s3.replace('''        super(type, containerId);
        this.container = container;
        this.tile = tile;''',
'''        super(type, containerId);
        net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                "[GT/diag] menu built: type={} side={} container={}",
                type, inventory.player.level().isClientSide() ? "client" : "server",
                container.getClass().getSimpleName());
        this.container = container;
        this.tile = tile;''', 1)
io.open(p3, "w", encoding="utf-8", newline="\n").write(s3)
print("menu diag added")
