import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- exact size from the artwork ----
s = s.replace('''    private static final int SIDEBAR_FIELD_X = 4;
    private static final int SIDEBAR_FIELD_Y = 25;
    private static final int SIDEBAR_FIELD_WIDTH = 48;
    private static final int SIDEBAR_FIELD_HEIGHT = 9;''',
'''    private static final int SIDEBAR_FIELD_X = 5;
    private static final int SIDEBAR_FIELD_Y = 25;
    /** Exactly the artwork's black band: x 5..54, y 51..59 of the expanded block. */
    private static final int SIDEBAR_FIELD_WIDTH = 50;
    private static final int SIDEBAR_FIELD_HEIGHT = 9;''', 1)

# ---- the text starts one pixel in, so it is not flush against the band's edge ----
s = s.replace('''        this.overclockField.setVisible(false);
        addRenderableWidget(this.overclockField);''',
'''        // One pixel in from the band's left edge: an EditBox draws its text flush against its own
        // x, which reads as clipped when the artwork's edge is right there.
        this.overclockField.setX(0);
        this.overclockField.setVisible(false);
        addRenderableWidget(this.overclockField);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
