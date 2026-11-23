"""Bind the advanced kettle's window and register the machine."""
import io

# ---------------- screen registration ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorClientEvents.java"
s = io.open(p, encoding="utf-8").read()
s = s.replace('''        event.register(GTChemicalReactorMultiblock.MENU.get(), GTChemicalReactorScreen::new);''',
'''        event.register(GTChemicalReactorMultiblock.MENU.get(), GTChemicalReactorScreen::new);
        // The advanced kettle shares the window class, so it shares the EMI bounds provider too:
        // that one is registered against the screen class, and a subclass matches it.
        event.register(net.fodoth.skina.goldentweaks.compat.avaritia_more_items
                        .GTAdvancedChemicalReactorMultiblock.MENU.get(),
                GTAdvancedChemicalReactorScreen::new);''', 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("advanced screen registered")

# ---------------- the machine is registered at startup ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''            GTChemicalReactorMultiblock.register(eventBus);''',
'''            GTChemicalReactorMultiblock.register(eventBus);
            net.fodoth.skina.goldentweaks.compat.avaritia_more_items
                    .GTAdvancedChemicalReactorMultiblock.register(eventBus);''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("advanced machine registered")
