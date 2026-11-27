"""Point every switch read at the shared accessors.

The drawing, the click handler and the tooltip now ask the same methods, so a plate cannot be drawn
in one place and answer the cursor in another.
"""
PATH = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\java\net\fodoth\skina\goldentweaks\content\multiblock\GTPortScreen.java"

SUBS = [
    # clicks
    ("""                && hits(this.leftPos + MODE_X, this.topPos + MODE_Y,
                        MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY)) {""",
     """                && hits(this.leftPos + modeX(), this.topPos + modeY(),
                        MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY)) {"""),
    ("""                && hits(this.leftPos + CHANNEL_X, this.topPos + CHANNEL_Y,
                        CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY)) {""",
     """                && hits(this.leftPos + channelX(), this.topPos + channelY(),
                        CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY)) {"""),
    ("""                && hits(this.leftPos + activeX(), this.topPos + ACTIVE_Y,
                        ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY)) {""",
     """                && hits(this.leftPos + activeX(), this.topPos + activeY(),
                        ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY)) {"""),
    # tooltips
    ("""        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;""",
     """        int x = this.leftPos + modeX();
        int y = this.topPos + modeY();"""),
    ("""        int x = this.leftPos + CHANNEL_X;
        int y = this.topPos + CHANNEL_Y;""",
     """        int x = this.leftPos + channelX();
        int y = this.topPos + channelY();"""),
    ("""        int x = this.leftPos + activeX();
        int y = this.topPos + ACTIVE_Y;""",
     """        int x = this.leftPos + activeX();
        int y = this.topPos + activeY();"""),
    # drawing
    ("""    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;""",
     """    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + modeX();
        int y = this.topPos + modeY();"""),
    ("""    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();
        int y = this.topPos + ACTIVE_Y;""",
     """    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();
        int y = this.topPos + activeY();"""),
    ("""    private void drawChannelButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + CHANNEL_X;
        int y = this.topPos + CHANNEL_Y;""",
     """    private void drawChannelButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + channelX();
        int y = this.topPos + channelY();"""),
    # the pressed test now takes the same coordinates the plate is drawn at
    ("""        boolean held = isPressed(x, this.topPos + MODE_Y, MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY);""",
     """        boolean held = isPressed(x, y, MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY);"""),
    ("""        boolean held = isPressed(x, this.topPos + ACTIVE_Y, ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY);""",
     """        boolean held = isPressed(x, y, ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY);"""),
    ("""        boolean held = isPressed(x, this.topPos + CHANNEL_Y, CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY);""",
     """        boolean held = isPressed(x, y, CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY);"""),
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
