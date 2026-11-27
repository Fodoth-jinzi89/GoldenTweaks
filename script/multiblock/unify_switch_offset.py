"""Put every part of a switch on one offset.

The drawing, the click area and the tooltip each used their own idea of where an assembly's
switch sits, so the plate was drawn in one place, answered the cursor in another and explained
itself in a third. This collapses them onto `switchShift()`.
"""
PATH = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\java\net\fodoth\skina\goldentweaks\content\multiblock\GTPortScreen.java"

SUBS = [
    # one offset everywhere
    ("spriteShift()", "switchShift()"),
    ("hitShift()", "switchShift()"),
    # the hit test no longer shifts: the caller passes the shifted coordinate
    ("""    private boolean isPressed(int x, int y, int width, int height, int mouseX, int mouseY) {
        return this.heldButton >= 0 && over(x, y + switchShift(), width, height, mouseX, mouseY);
    }""",
     """    private boolean isPressed(int x, int y, int width, int height, int mouseX, int mouseY) {
        return this.heldButton >= 0 && over(x, y, width, height, mouseX, mouseY);
    }"""),
    ("""    /** Whether the cursor is over a switch, for the click handler and for its tooltip. */
    private boolean hits(int x, int y, int width, int height, double mouseX, double mouseY) {
        return over(x, y + switchShift(), width, height, (int) mouseX, (int) mouseY);
    }""",
     """    /** Whether the cursor is over a switch, for the click handler and for its tooltip. */
    private boolean hits(int x, int y, int width, int height, double mouseX, double mouseY) {
        return over(x, y, width, height, (int) mouseX, (int) mouseY);
    }"""),
    # clicks: pass the shifted coordinate
    ("""                && hits(this.leftPos + MODE_X, this.topPos + MODE_Y,
                        MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY)) {""",
     """                && hits(this.leftPos + MODE_X, this.topPos + MODE_Y + switchShift(),
                        MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY)) {"""),
    ("""                && hits(this.leftPos + CHANNEL_X, this.topPos + CHANNEL_Y,
                        CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY)) {""",
     """                && hits(this.leftPos + CHANNEL_X, this.topPos + CHANNEL_Y + switchShift(),
                        CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY)) {"""),
    ("""                && hits(this.leftPos + activeX(), this.topPos + ACTIVE_Y,
                        ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY)) {""",
     """                && hits(this.leftPos + activeX(), this.topPos + ACTIVE_Y + switchShift(),
                        ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY)) {"""),
    # drawing: same coordinate as the hit test
    ("""    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;""",
     """    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y + switchShift();"""),
    ("""    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();
        int y = this.topPos + ACTIVE_Y;""",
     """    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();
        int y = this.topPos + ACTIVE_Y + switchShift();"""),
    ("""    private void drawChannelButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + CHANNEL_X;
        int y = this.topPos + CHANNEL_Y;""",
     """    private void drawChannelButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + CHANNEL_X;
        int y = this.topPos + CHANNEL_Y + switchShift();"""),
    # tooltips already read the offset; keep them consistent
    ("""        int y = this.topPos + MODE_Y + switchShift();
        if (mouseX < x || mouseX >= x + MODE_WIDTH || mouseY < y || mouseY >= y + MODE_HEIGHT) {""",
     """        int y = this.topPos + MODE_Y + switchShift();
        if (mouseX < x || mouseX >= x + MODE_WIDTH || mouseY < y || mouseY >= y + MODE_HEIGHT) {"""),
]


def main():
    with open(PATH, encoding="utf-8") as handle:
        text = handle.read()
    for old, new in SUBS:
        if old not in text:
            print("MISS:", old.splitlines()[0].strip()[:70])
            continue
        text = text.replace(old, new)
        print("ok  :", new.splitlines()[0].strip()[:70])
    with open(PATH, "w", encoding="utf-8", newline="\n") as handle:
        handle.write(text)


if __name__ == "__main__":
    main()
