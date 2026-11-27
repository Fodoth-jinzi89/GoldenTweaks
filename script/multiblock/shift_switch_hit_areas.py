"""Move the assembly switch hit areas eleven pixels down.

The plates are drawn where the single-kind ones are; only the art underneath them moved. So the
sprite rows take the offset and the plates do not — but the icon a player aims at sits lower, and
the hit areas follow the icon.
"""
import re

PATH = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\java\net\fodoth\skina\goldentweaks\content\multiblock\GTPortScreen.java"

SUBS = [
    # clicks: stacking switch
    ("""        if (button == 0 && this.menu.kind() != GTPortKind.ENERGY
                && mouseX >= this.leftPos + MODE_X && mouseX < this.leftPos + MODE_X + MODE_WIDTH
                && mouseY >= this.topPos + MODE_Y && mouseY < this.topPos + MODE_Y + MODE_HEIGHT) {""",
     """        if (button == 0 && this.menu.kind() != GTPortKind.ENERGY
                && hits(this.leftPos + MODE_X, this.topPos + MODE_Y,
                        MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY)) {"""),
    # clicks: channel switch
    ("""        if (button == 0 && this.menu.kind() == GTPortKind.GENERAL
                && mouseX >= this.leftPos + CHANNEL_X
                && mouseX < this.leftPos + CHANNEL_X + CHANNEL_WIDTH
                && mouseY >= this.topPos + CHANNEL_Y
                && mouseY < this.topPos + CHANNEL_Y + CHANNEL_HEIGHT) {""",
     """        if (button == 0 && this.menu.kind() == GTPortKind.GENERAL
                && hits(this.leftPos + CHANNEL_X, this.topPos + CHANNEL_Y,
                        CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY)) {"""),
    # clicks: reach-through switch
    ("""        if (button == 0
                && mouseX >= this.leftPos + activeX() && mouseX < this.leftPos + activeX() + ACTIVE_WIDTH
                && mouseY >= this.topPos + ACTIVE_Y && mouseY < this.topPos + ACTIVE_Y + ACTIVE_HEIGHT) {""",
     """        if (button == 0
                && hits(this.leftPos + activeX(), this.topPos + ACTIVE_Y,
                        ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY)) {"""),
    # clicks: run switch
    ("""        if (button == 0
                && mouseX >= this.leftPos + RUN_X && mouseX < this.leftPos + RUN_X + RUN_WIDTH
                && mouseY >= this.topPos + RUN_Y && mouseY < this.topPos + RUN_Y + RUN_HEIGHT) {""",
     """        if (button == 0
                && hits(this.leftPos + RUN_X, this.topPos + RUN_Y,
                        RUN_WIDTH, RUN_HEIGHT, mouseX, mouseY)) {"""),
    # tooltips: the three plates
    ("""        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        if (mouseX < x || mouseX >= x + MODE_WIDTH || mouseY < y || mouseY >= y + MODE_HEIGHT) {""",
     """        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y + hitShift();
        if (mouseX < x || mouseX >= x + MODE_WIDTH || mouseY < y || mouseY >= y + MODE_HEIGHT) {"""),
    ("""        int x = this.leftPos + CHANNEL_X;
        int y = this.topPos + CHANNEL_Y;
        if (mouseX < x || mouseX >= x + CHANNEL_WIDTH || mouseY < y || mouseY >= y + CHANNEL_HEIGHT) {""",
     """        int x = this.leftPos + CHANNEL_X;
        int y = this.topPos + CHANNEL_Y + hitShift();
        if (mouseX < x || mouseX >= x + CHANNEL_WIDTH || mouseY < y || mouseY >= y + CHANNEL_HEIGHT) {"""),
    ("""        int x = this.leftPos + activeX();
        int y = this.topPos + ACTIVE_Y;
        if (mouseX < x || mouseX >= x + ACTIVE_WIDTH || mouseY < y || mouseY >= y + ACTIVE_HEIGHT) {""",
     """        int x = this.leftPos + activeX();
        int y = this.topPos + ACTIVE_Y + hitShift();
        if (mouseX < x || mouseX >= x + ACTIVE_WIDTH || mouseY < y || mouseY >= y + ACTIVE_HEIGHT) {"""),
]


def main():
    with open(PATH, encoding="utf-8") as handle:
        text = handle.read()
    for old, new in SUBS:
        if old not in text:
            print("MISS:", old.splitlines()[1].strip()[:70])
            continue
        text = text.replace(old, new, 1)
        print("ok  :", new.splitlines()[1].strip()[:70])
    with open(PATH, "w", encoding="utf-8", newline="\n") as handle:
        handle.write(text)


if __name__ == "__main__":
    main()
