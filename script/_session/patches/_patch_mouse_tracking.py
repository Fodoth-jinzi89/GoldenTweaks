import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** First visible row of the storage grid. */
    private int scrollRow;''',
'''    /** First visible row of the storage grid. */
    private int scrollRow;

    /**
     * Cursor position from the last frame, in panel coordinates.
     *
     * <p>Recorded here because a key press callback carries no coordinates, and the tooltip code
     * needs to know which stack the player is pointing at when R or U is pressed.</p>
     */
    private double lastMouseX = -1;
    private double lastMouseY = -1;''', 1)

s = s.replace('''    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        refreshChannelLock();''',
'''    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        refreshChannelLock();''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
