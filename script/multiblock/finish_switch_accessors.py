"""Finish the switch accessor migration.

Leftovers from the offset-based version: the drawing still added `switchShift()`, read the plain
constants, and a second `activeX()` shadowed the shared one.
"""
PATH = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\java\net\fodoth\skina\goldentweaks\content\multiblock\GTPortScreen.java"

SUBS = [
    # the old per-kind accessor is now the shared one, defined above
    ("""    /**
     * Where the reach-through switch sits for this port.
     *
     * <p>After the stacking switch when there is one, on the first plate when there is not. Kept in
     * one method so the drawing, the click area and the tooltip cannot disagree about it.</p>
     */
    private int activeX() {
        return this.menu.kind() == GTPortKind.ENERGY ? ACTIVE_X_NO_MODE : ACTIVE_X;
    }

""", ""),
    # drawing: use the accessors, no offset
    ("""    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();
        int y = this.topPos + ACTIVE_Y + switchShift();""",
     """    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();
        int y = this.topPos + activeY();"""),
    ("""    private void drawChannelButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + CHANNEL_X;
        int y = this.topPos + CHANNEL_Y + switchShift();""",
     """    private void drawChannelButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + channelX();
        int y = this.topPos + channelY();"""),
    ("""    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y + switchShift();""",
     """    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + modeX();
        int y = this.topPos + modeY();"""),
]


def main():
    with open(PATH, encoding="utf-8") as handle:
        text = handle.read()
    for old, new in SUBS:
        if old not in text:
            print("MISS:", old.splitlines()[0].strip()[:70])
            continue
        text = text.replace(old, new, 1)
        head = new.splitlines()[0].strip() if new.strip() else "(removed)"
        print("ok  :", head[:70])
    with open(PATH, "w", encoding="utf-8", newline="\n") as handle:
        handle.write(text)


if __name__ == "__main__":
    main()
