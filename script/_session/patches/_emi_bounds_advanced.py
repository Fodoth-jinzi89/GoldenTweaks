"""The advanced kettle's window also needs the EMI bounds provider.

The provider was registered for `GTChemicalReactorScreen` only. EMI matches providers by exact
class, not by inheritance, so the advanced screen — a subclass — got no bounds and EMI laid its
sidebar over the machine's own side pane.

Registering the same shape for the subclass fixes the overlap. The provider is shared through a
method so both entries cannot drift apart.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionEmiPlugin.java"
s = io.open(p, encoding="utf-8").read()

old = '''        // EMI places its own panels around whatever space a screen announces. The kettle draws a
        // side pane outside its main panel, so without this it lays the recipe list straight over
        // that pane — in both its collapsed and expanded sizes.
        registry.addScreenBoundsProvider(GTChemicalReactorScreen.class,
                screen -> new Bounds(screen.panelLeft(), screen.panelTop(),
                        screen.sidebarRight() - screen.panelLeft(),
                        screen.sidebarBottom() - screen.panelTop()));'''
new = '''        // EMI places its own panels around whatever space a screen announces. The kettle draws a
        // side pane outside its main panel, so without this it lays the recipe list straight over
        // that pane — in both its collapsed and expanded sizes.
        //
        // Registered for both screens: EMI matches providers by exact class, so registering only the
        // base would leave the advanced window with no bounds and its sidebar overlapped.
        registry.addScreenBoundsProvider(GTChemicalReactorScreen.class,
                GTChemicalReactionEmiPlugin::kettleBounds);
        registry.addScreenBoundsProvider(GTAdvancedChemicalReactorScreen.class,
                GTChemicalReactionEmiPlugin::kettleBounds);'''
assert old in s
s = s.replace(old, new, 1)

s = s.replace('''    /**
     * Registry access for the AE keys.''',
'''    /** The area the kettle's window occupies, side pane included. */
    private static Bounds kettleBounds(GTChemicalReactorScreen screen) {
        return new Bounds(screen.panelLeft(), screen.panelTop(),
                screen.sidebarRight() - screen.panelLeft(),
                screen.sidebarBottom() - screen.panelTop());
    }

    /**
     * Registry access for the AE keys.''', 1)

if "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client.GTAdvancedChemicalReactorScreen;" not in s:
    s = s.replace(
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client.GTChemicalReactorScreen;",
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client.GTAdvancedChemicalReactorScreen;\n"
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client.GTChemicalReactorScreen;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("both screens announce their bounds")
