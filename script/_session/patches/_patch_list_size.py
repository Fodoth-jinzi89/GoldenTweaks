import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    private static final int LIST_X = 15;
    private static final int LIST_Y = 38;
    private static final int LIST_WIDTH = 120;
    private static final int LIST_HEIGHT = 48;
    private static final int ROW_HEIGHT = 16;''',
'''    private static final int LIST_X = 15;
    private static final int LIST_Y = 38;
    /**
     * The output band is wide and short: 140% of the old width at two thirds of its height.
     *
     * <p>The extra width reaches past where the energy trough starts, which is fine because the
     * trough sits well below it — the two no longer share a row once the band is this shallow.</p>
     */
    private static final int LIST_WIDTH = 168;
    private static final int LIST_HEIGHT = 32;
    private static final int ROW_HEIGHT = 16;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
