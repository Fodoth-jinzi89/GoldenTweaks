"""Split a switch's drawing from the place it answers the cursor.

The plates are drawn eleven pixels lower on an assembly's sheet, but the cursor still meets them
where the single-kind art puts them — so the interaction reads the plain coordinates while the
blit adds the offset.
"""
PATH = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\java\net\fodoth\skina\goldentweaks\content\multiblock\GTPortScreen.java"

SUBS = [
    # clicks: back to the plain coordinate
    ("""                && hits(this.leftPos + MODE_X, this.topPos + MODE_Y + switchShift(),
                        MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY)) {""",
     """                && hits(this.leftPos + MODE_X, this.topPos + MODE_Y,
                        MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY)) {"""),
    ("""                && hits(this.leftPos + CHANNEL_X, this.topPos + CHANNEL_Y + switchShift(),
                        CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY)) {""",
     """                && hits(this.leftPos + CHANNEL_X, this.topPos + CHANNEL_Y,
                        CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY)) {"""),
    ("""                && hits(this.leftPos + activeX(), this.topPos + ACTIVE_Y + switchShift(),
                        ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY)) {""",
     """                && hits(this.leftPos + activeX(), this.topPos + ACTIVE_Y,
                        ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY)) {"""),
    # tooltips: back to the plain coordinate
    ("""        int y = this.topPos + MODE_Y + switchShift();
        if (mouseX < x || mouseX >= x + MODE_WIDTH || mouseY < y || mouseY >= y + MODE_HEIGHT) {""",
     """        int y = this.topPos + MODE_Y;
        if (mouseX < x || mouseX >= x + MODE_WIDTH || mouseY < y || mouseY >= y + MODE_HEIGHT) {"""),
    ("""        int y = this.topPos + CHANNEL_Y + switchShift();
        if (mouseX < x || mouseX >= x + CHANNEL_WIDTH || mouseY < y || mouseY >= y + CHANNEL_HEIGHT) {""",
     """        int y = this.topPos + CHANNEL_Y;
        if (mouseX < x || mouseX >= x + CHANNEL_WIDTH || mouseY < y || mouseY >= y + CHANNEL_HEIGHT) {"""),
    ("""        int y = this.topPos + ACTIVE_Y + switchShift();
        if (mouseX < x || mouseX >= x + ACTIVE_WIDTH || mouseY < y || mouseY >= y + ACTIVE_HEIGHT) {""",
     """        int y = this.topPos + ACTIVE_Y;
        if (mouseX < x || mouseX >= x + ACTIVE_WIDTH || mouseY < y || mouseY >= y + ACTIVE_HEIGHT) {"""),
    # the pressed plate follows the cursor, so it tests the plain rectangle while sitting at the
    # shifted one
    ("""    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y + switchShift();
        boolean held = isPressed(x, y, MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY);""",
     """    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y + switchShift();
        boolean held = isPressed(x, this.topPos + MODE_Y, MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY);"""),
    ("""    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();
        int y = this.topPos + ACTIVE_Y + switchShift();
        boolean held = isPressed(x, y, ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY);""",
     """    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();
        int y = this.topPos + ACTIVE_Y + switchShift();
        boolean held = isPressed(x, this.topPos + ACTIVE_Y, ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY);"""),
    ("""    private void drawChannelButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + CHANNEL_X;
        int y = this.topPos + CHANNEL_Y + switchShift();
        boolean held = isPressed(x, y, CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY);""",
     """    private void drawChannelButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + CHANNEL_X;
        int y = this.topPos + CHANNEL_Y + switchShift();
        boolean held = isPressed(x, this.topPos + CHANNEL_Y, CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY);"""),
]


def main():
    with open(PATH, encoding="utf-8") as handle:
        text = handle.read()
    for old, new in SUBS:
        if old not in text:
            print("MISS:", old.splitlines()[0].strip()[:70])
            continue
        text = text.replace(old, new, 1)
        print("ok  :", new.splitlines()[0].strip()[:70])
    with open(PATH, "w", encoding="utf-8", newline="\n") as handle:
        handle.write(text)


if __name__ == "__main__":
    main()
