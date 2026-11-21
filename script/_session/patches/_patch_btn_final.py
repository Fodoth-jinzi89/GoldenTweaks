import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    private static final int MODE_X = 49;
    private static final int MODE_Y = 178;''',
'''    private static final int MODE_X = 154;
    private static final int MODE_Y = 178;''', 1)

s = s.replace('''     * <p>Nudged right of the plate's own origin so the pair sits clear of the button row's left
     * edge; the same offset applies to the click area, so what is drawn is what is hit.</p>''',
'''     * <p>Drawn at its own spot in the panel rather than at the sheet's coordinate: the sheet's copy
     * sits where the artwork was laid out, and this button belongs beside the run switch. The same
     * position drives the click area, so what is drawn is what is hit.</p>''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
