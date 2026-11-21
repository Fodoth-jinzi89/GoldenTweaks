import io, json

# ---------------- tidy the one redundant branch ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('''            ItemStack leftover = to instanceof net.neoforged.neoforge.items.wrapper.RangedWrapper
                    ? stack : insertInto(to, stack);''',
'''            ItemStack leftover = insertInto(to, stack);''', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('entity tidy:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- screen: the third button ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

s2 = s2.replace('''    /** Text drawn directly on the light panel, which needs the darker ink to stay readable. */''',
'''    /**
     * The reach-through switch, on the second plate of the top-right block.
     *
     * <p>Same construction as the stacking switch: one plate from the sheet, with the state shown by
     * which of two icons is laid over it. Yellow is passive (the plain behaviour, where the port
     * waits to be reached), red is active (it also reaches through its own face).</p>
     */
    private static final int ACTIVE_X = 179;
    private static final int ACTIVE_Y = 27;
    private static final int ACTIVE_PLATE_U = 44;
    private static final int ACTIVE_PLATE_V = 178;
    private static final int ACTIVE_WIDTH = 21;
    private static final int ACTIVE_HEIGHT = 17;
    private static final int ACTIVE_ICON_U_PASSIVE = 44;
    private static final int ACTIVE_ICON_U_ACTIVE = 67;
    private static final int ACTIVE_ICON_V = 212;

    /** Text drawn directly on the light panel, which needs the darker ink to stay readable. */''', 1)

s2 = s2.replace('''        drawRunButton(graphics, mouseX, mouseY);
        drawModeButton(graphics, mouseX, mouseY);''',
'''        drawRunButton(graphics, mouseX, mouseY);
        drawModeButton(graphics, mouseX, mouseY);
        drawActiveButton(graphics, mouseX, mouseY);''', 1)

s2 = s2.replace('''    private void drawLabels(GuiGraphics graphics) {''',
'''    /** The reach-through switch: plate, then the icon for the state it is in. */
    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + ACTIVE_X;
        int y = this.topPos + ACTIVE_Y;
        graphics.blit(IO_TEXTURE, x, y, ACTIVE_PLATE_U, ACTIVE_PLATE_V,
                ACTIVE_WIDTH, ACTIVE_HEIGHT);
        boolean active = this.menu.isActivePort();
        graphics.blit(IO_TEXTURE, x, y,
                active ? ACTIVE_ICON_U_ACTIVE : ACTIVE_ICON_U_PASSIVE, ACTIVE_ICON_V,
                ACTIVE_WIDTH, ACTIVE_HEIGHT);
    }

    private void drawLabels(GuiGraphics graphics) {''', 1)

# click
s2 = s2.replace('''        // The run switch flips participation; the server applies the change and reports it back.''',
'''        // The reach-through switch, handled exactly like the other two.
        if (button == 0
                && mouseX >= this.leftPos + ACTIVE_X && mouseX < this.leftPos + ACTIVE_X + ACTIVE_WIDTH
                && mouseY >= this.topPos + ACTIVE_Y && mouseY < this.topPos + ACTIVE_Y + ACTIVE_HEIGHT) {
            GTPortBlockEntity port = this.menu.port();
            if (port != null) {
                PacketDistributor.sendToServer(new C2SPortTogglePacket(
                        port.getBlockPos(), C2SPortTogglePacket.Target.ACTIVE));
            }
            return true;
        }

        // The run switch flips participation; the server applies the change and reports it back.''', 1)

# tooltip
s2 = s2.replace('''        renderRunTooltip(graphics, mouseX, mouseY);
        renderModeTooltip(graphics, mouseX, mouseY);''',
'''        renderRunTooltip(graphics, mouseX, mouseY);
        renderModeTooltip(graphics, mouseX, mouseY);
        renderActiveTooltip(graphics, mouseX, mouseY);''', 1)

s2 = s2.replace('''    /** Whether any of the text boxes currently has focus. */''',
'''    /** Explains the reach-through switch, whose icon alone does not say which way round it is. */
    private void renderActiveTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + ACTIVE_X;
        int y = this.topPos + ACTIVE_Y;
        if (mouseX < x || mouseX >= x + ACTIVE_WIDTH || mouseY < y || mouseY >= y + ACTIVE_HEIGHT) {
            return;
        }
        graphics.renderTooltip(this.font, Component.translatable(this.menu.isActivePort()
                ? "gui.goldentweaks.port.active.on"
                : "gui.goldentweaks.port.active.off"), mouseX, mouseY);
    }

    /** Whether any of the text boxes currently has focus. */''', 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('screen:', 'changed' if s2 != orig2 else 'NO CHANGE')

# ---------------- menu slot ----------------
p3 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortMenu.java'
s3 = io.open(p3, encoding='utf-8').read()
orig3 = s3
s3 = s3.replace('''    public static final int DATA_SINGLE_CELL = 3;
    private static final int DATA_COUNT = 4;''',
'''    public static final int DATA_SINGLE_CELL = 3;
    /** One when the port also reaches through its face. */
    public static final int DATA_ACTIVE = 4;
    private static final int DATA_COUNT = 5;''', 1)
s3 = s3.replace('''                case DATA_SINGLE_CELL -> GTPortMenu.this.isSingleCellMode() ? 1 : 0;''',
'''                case DATA_SINGLE_CELL -> GTPortMenu.this.isSingleCellMode() ? 1 : 0;
                case DATA_ACTIVE -> GTPortMenu.this.isActivePort() ? 1 : 0;''', 1)
s3 = s3.replace('''    /** Synced input rate, in FE per tick. */''',
'''    /** Whether the port also reaches through its face, rather than only waiting to be reached. */
    public boolean isActivePort() {
        return this.port == null || this.port.isActive();
    }

    /** Synced input rate, in FE per tick. */''', 1)
io.open(p3, 'w', encoding='utf-8', newline='\n').write(s3)
print('menu:', 'changed' if s3 != orig3 else 'NO CHANGE')

# ---------------- lang ----------------
zh = {
    'gui.goldentweaks.port.active.on': '主动：额外从正面抽取 / 推出',
    'gui.goldentweaks.port.active.off': '被动：仅等待外部输入 / 输出',
}
en = {
    'gui.goldentweaks.port.active.on': 'Active: also pulls from / pushes to its face',
    'gui.goldentweaks.port.active.off': 'Passive: only accepts outside input',
}
for path, extra in [
    (r"src/main/resources/assets/goldentweaks/lang/zh_cn.json", zh),
    (r"src/main/resources/assets/goldentweaks/lang/en_us.json", en),
]:
    data = json.load(io.open(path, encoding="utf-8"))
    data.update(extra)
    with io.open(path, "w", encoding="utf-8", newline="\n") as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write("\n")
    print("lang updated", path)
