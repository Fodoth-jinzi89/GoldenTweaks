import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s = io.open(p, encoding='utf-8').read()
orig = s

old_start = s.index('    /**\n     * Widens a label so both bars come out the same size.')
old_end = s.index('    private static net.minecraft.client.gui.Font font() {')
old_end = s.index('}', s.index('return net.minecraft.client.Minecraft.getInstance().font;', old_end)) + 1

new = '''    /** The pixel width both bars are held to, so the two read as one column. */
    private static final int BAR_TEXT_WIDTH = 96;

    /**
     * Pads a label out to a fixed pixel width with spaces.
     *
     * <p>Jade sizes a bar from its text, so "00:12 / 01:40" and "29.1 k / 100 k FE" would otherwise
     * produce two different widths and the panel would look ragged. Padding to a shared width makes
     * both bars the same length; the text stays left-aligned inside, which is where a progress
     * readout belongs.</p>
     *
     * <p>A space is four pixels wide, so the padding lands on a four-pixel boundary. The remaining
     * one to three pixels are invisible inside the bar — Jade already adds a few pixels of its own
     * margin around the text.</p>
     */
    private static Component padded(Component label) {
        int width = font().width(label);
        if (width >= BAR_TEXT_WIDTH) {
            return label;
        }
        int spaces = (BAR_TEXT_WIDTH - width) / 4;
        if (spaces <= 0) {
            return label;
        }
        return Component.empty().append(label).append(" ".repeat(spaces));
    }

    private static net.minecraft.client.gui.Font font() {
        return net.minecraft.client.Minecraft.getInstance().font;
    }'''

s = s[:old_start] + new + s[old_end:]

# drop the now-unused measured cache
s = s.replace('''
    /** Shared bar-label width, measured once; negative until then. */
    private static int CACHED_BAR_WIDTH = -1;''', '')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
