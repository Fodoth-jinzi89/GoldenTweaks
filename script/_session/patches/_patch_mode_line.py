import io

# ---- EMI ----
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionEmiRecipe.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('''        widgets.addText(Component.translatable("goldentweaks.chemical_reaction.duration",
                        GTDurationFormat.format(this.recipe.getProcessingTime())),
                GTChemicalReactionLayout.TEXT_X, y + step * 2, 0x404040, false);
    }''',
'''        widgets.addText(Component.translatable("goldentweaks.chemical_reaction.duration",
                        GTDurationFormat.format(this.recipe.getProcessingTime())),
                GTChemicalReactionLayout.TEXT_X, y + step * 2, 0x404040, false);

        // The mode is printed as the raw integer the data pack carries, wildcard included: the
        // number is what a player types into a port, so translating 0 to a word would hide the value
        // they actually need to match.
        widgets.addText(Component.translatable("goldentweaks.chemical_reaction.mode",
                        String.valueOf(this.recipe.mode())),
                GTChemicalReactionLayout.TEXT_X, y + step * 3, 0x404040, false);
    }''', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('emi:', 'changed' if s != orig else 'NO CHANGE')

# ---- JEI ----
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionJeiCategory.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''        List<net.minecraft.network.chat.FormattedText> lines = new ArrayList<>(3);''',
                '''        List<net.minecraft.network.chat.FormattedText> lines = new ArrayList<>(4);''', 1)
s2 = s2.replace('''        lines.add(Component.translatable("goldentweaks.chemical_reaction.duration",
                GTDurationFormat.format(recipe.getProcessingTime())));''',
'''        lines.add(Component.translatable("goldentweaks.chemical_reaction.duration",
                GTDurationFormat.format(recipe.getProcessingTime())));
        // Raw integer, same reasoning as the EMI page: this is the value a port is set to.
        lines.add(Component.translatable("goldentweaks.chemical_reaction.mode",
                String.valueOf(recipe.mode())));''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('jei:', 'changed' if s2 != orig2 else 'NO CHANGE')

# ---- layout height: four lines need 11 more pixels ----
p3 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionLayout.java'
s3 = io.open(p3, encoding='utf-8').read()
orig3 = s3
s3 = s3.replace('''     * <p>The grids stay at the top and the figures below them; at 110 the margins come out even —
     * 6 above the grids, 10 between them and the text, 9 under the last line.</p>
     */
    public static final int HEIGHT = 110;''',
'''     * <p>The grids stay at the top and the figures below them; at 121 the margins come out even —
     * 6 above the grids, 10 between them and the text, 8 under the last of the four lines.</p>
     */
    public static final int HEIGHT = 121;''', 1)
io.open(p3, 'w', encoding='utf-8', newline='\n').write(s3)
print('layout:', 'changed' if s3 != orig3 else 'NO CHANGE')

# ---- lang ----
import json
zh = {'goldentweaks.chemical_reaction.mode': '模式：%s'}
en = {'goldentweaks.chemical_reaction.mode': 'Mode: %s'}
for path, extra in [
    (r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh),
    (r'src/main/resources/assets/goldentweaks/lang/en_us.json', en),
]:
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(extra)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print('lang:', path)
