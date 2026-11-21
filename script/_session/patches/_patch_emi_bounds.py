import io

# ---- EMI plugin imports ----
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionEmiPlugin.java'
s = io.open(p, encoding='utf-8').read()
orig = s
if 'import dev.emi.emi.api.widget.Bounds;' not in s:
    s = s.replace('import dev.emi.emi.api.stack.EmiStack;',
                  'import dev.emi.emi.api.stack.EmiStack;\nimport dev.emi.emi.api.widget.Bounds;', 1)
if 'import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client.GTChemicalReactorScreen;' not in s:
    s = s.replace('import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipe;',
                  'import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipe;', 1)
    s = s.replace('import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipes;',
                  'import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipes;\n'
                  'import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client.GTChemicalReactorScreen;', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('plugin:', 'changed' if s != orig else 'NO CHANGE')

# ---- expanded slot shifted 1px right ----
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''    private static final int SIDEBAR_SLOT_EXPANDED_X = 3;''',
                '''    private static final int SIDEBAR_SLOT_EXPANDED_X = 4;''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('screen:', 'changed' if s2 != orig2 else 'NO CHANGE')
