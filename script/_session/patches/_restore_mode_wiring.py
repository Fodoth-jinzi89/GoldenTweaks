"""Restore the click path and tooltip for the stacking switch, plus its server setter."""
import io

# ---------------- screen: click ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''        // The reach-through switch, handled exactly like the other two.''',
'''        // The stacking switch only exists on item and fluid ports, so its plate is checked only
        // there — otherwise clicking an energy port's first plate would both change the mode and
        // reach through, since both controls claim the same spot.
        if (button == 0 && this.menu.kind() != GTPortKind.ENERGY
                && mouseX >= this.leftPos + MODE_X && mouseX < this.leftPos + MODE_X + MODE_WIDTH
                && mouseY >= this.topPos + MODE_Y && mouseY < this.topPos + MODE_Y + MODE_HEIGHT) {
            GTPortBlockEntity port = this.menu.port();
            if (port != null) {
                PacketDistributor.sendToServer(new C2SPortTogglePacket(
                        port.getBlockPos(), C2SPortTogglePacket.Target.STACK_MODE));
            }
            return true;
        }

        // The reach-through switch, handled exactly like the other two.''', 1)

# ---------------- screen: tooltip ----------------
s = s.replace('''    /** Explains the reach-through switch, whose icon alone does not say which way round it is. */''',
'''    /** Explains the stacking switch: the icon alone does not say which way round it is. */
    private void renderModeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.menu.kind() == GTPortKind.ENERGY) {
            return;
        }
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        if (mouseX < x || mouseX >= x + MODE_WIDTH || mouseY < y || mouseY >= y + MODE_HEIGHT) {
            return;
        }
        graphics.renderTooltip(this.font, Component.translatable(this.menu.isSingleCellMode()
                ? "gui.goldentweaks.port.stack.single"
                : "gui.goldentweaks.port.stack.multi"), mouseX, mouseY);
    }

    /** Explains the reach-through switch, whose icon alone does not say which way round it is. */''', 1)

s = s.replace('''        renderActiveTooltip(graphics, mouseX, mouseY);''',
'''        renderModeTooltip(graphics, mouseX, mouseY);
        renderActiveTooltip(graphics, mouseX, mouseY);''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("screen click and tooltip restored")

# ---------------- packet: the target came back ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SPortTogglePacket.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''        /** Whether the port takes part in the machine. */
        ENABLED,''',
'''        /** Whether the port takes part in the machine. */
        ENABLED,
        /** Whether each entry occupies one cell or spans as many as it needs. */
        STACK_MODE,''', 1)
s2 = s2.replace('''                    case ENABLED -> port.setEnabled(!port.isEnabled());''',
'''                    case ENABLED -> port.setEnabled(!port.isEnabled());
                    case STACK_MODE -> port.toggleSingleCell();''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("packet target restored")

# ---------------- entity: the setter came back ----------------
p3 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s3 = io.open(p3, encoding="utf-8").read()
s3 = s3.replace('''    /** Whether this port currently takes part in the machine. */''',
'''    /** Flips the stacking mode and tells nearby clients. */
    public void toggleSingleCell() {
        this.singleCell = !this.singleCell;
        onContentChanged();
    }

    /** Whether this port currently takes part in the machine. */''', 1)
io.open(p3, "w", encoding="utf-8", newline="\n").write(s3)
print("entity setter restored")
