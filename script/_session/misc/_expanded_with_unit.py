import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTEnergyFormat.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    private static String format(long amount, boolean withUnit) {''',
'''    /**
     * The expanded figure with its unit: {@code 1,728 FE}.
     *
     * <p>Always a space. An expanded number carries no compact prefix, so there is nothing for the
     * unit to attach to — the same reason a bare {@code 64} is spaced while {@code 1.06 k} is not.</p>
     */
    public static String formatExpandedWithUnit(long amount, String unit) {
        String figure = formatExpanded(amount);
        return unit.isEmpty() ? figure : figure + " " + unit;
    }

    private static String format(long amount, boolean withUnit) {''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('energy format:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- JEI ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionJeiCategory.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''                GTEnergyFormat.formatExpanded(recipe.totalEnergy())));''',
'''                GTEnergyFormat.formatExpandedWithUnit(recipe.totalEnergy(), "FE")));''', 1)
s2 = s2.replace('''                GTEnergyFormat.formatExpanded(recipe.getEnergyPerTick())));''',
'''                GTEnergyFormat.formatExpandedWithUnit(recipe.getEnergyPerTick(), "FE/t")));''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('jei:', 'changed' if s2 != orig2 else 'NO CHANGE')

# ---------------- EMI ----------------
p3 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionEmiRecipe.java'
s3 = io.open(p3, encoding='utf-8').read()
orig3 = s3
s3 = s3.replace('''                        GTEnergyFormat.formatExpanded(this.recipe.totalEnergy())),''',
'''                        GTEnergyFormat.formatExpandedWithUnit(this.recipe.totalEnergy(), "FE")),''', 1)
s3 = s3.replace('''                        GTEnergyFormat.formatExpanded(this.recipe.getEnergyPerTick())),''',
'''                        GTEnergyFormat.formatExpandedWithUnit(this.recipe.getEnergyPerTick(), "FE/t")),''', 1)
io.open(p3, 'w', encoding='utf-8', newline='\n').write(s3)
print('emi:', 'changed' if s3 != orig3 else 'NO CHANGE')
