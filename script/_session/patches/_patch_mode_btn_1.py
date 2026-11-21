import io, shutil, os

# ---- 1. copy the updated sheet ----
src = r'multiblock\gt_port_io.png'
dst = r'src/main/resources/assets/goldentweaks/textures/gui/gt_port_io.png'
shutil.copyfile(src, dst)
print('texture updated ->', dst, os.path.getsize(dst), 'bytes')

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- 2. constants: the mode switch, and what it changes ----
s = s.replace('''    /** Text drawn directly on the light panel, which needs the darker ink to stay readable. */''',
'''    /**
     * The stacking-mode switch: one cell per entry, or as many as the entry needs.
     *
     * <p>Two icons on the same 21 by 17 footprint, with the transparent margin the sheet reserves so
     * the pair lines up with the plates above. Yellow marks single-cell mode, red marks multi-cell;
     * the pair sits at the head of the button row and the labels below are shifted to clear it.</p>
     */
    private static final int MODE_X = 44;
    private static final int MODE_Y = 195;
    private static final int MODE_WIDTH = 21;
    private static final int MODE_HEIGHT = 17;
    private static final int MODE_ICON_U_SINGLE = 44;
    private static final int MODE_ICON_U_MULTI = 67;
    private static final int MODE_ICON_V = 195;

    /** Text drawn directly on the light panel, which needs the darker ink to stay readable. */''', 1)

# ---- 3. drawing ----
s = s.replace('''        drawRunButton(graphics, mouseX, mouseY);
    }''',
'''        drawRunButton(graphics, mouseX, mouseY);
        drawModeButton(graphics, mouseX, mouseY);
    }

    /**
     * The stacking-mode switch.
     *
     * <p>Which icon is drawn is the answer, not a highlight: yellow means each entry occupies one
     * cell, red means an entry may span as many as it needs. There is no pressed plate because the
     * sheet has none for this pair — the colour is the state.</p>
     */
    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        boolean single = this.menu.isSingleCellMode();
        graphics.blit(IO_TEXTURE, x, y,
                single ? MODE_ICON_U_SINGLE : MODE_ICON_U_MULTI, MODE_ICON_V,
                MODE_WIDTH, MODE_HEIGHT);
    }''', 1)

# ---- 4. click ----
s = s.replace('''        // The run switch flips participation; the server applies the change and reports it back.''',
'''        // The mode switch flips how entries are laid out; like the run switch, the server owns the
        // value and reports it back.
        if (button == 0
                && mouseX >= this.leftPos + MODE_X && mouseX < this.leftPos + MODE_X + MODE_WIDTH
                && mouseY >= this.topPos + MODE_Y && mouseY < this.topPos + MODE_Y + MODE_HEIGHT) {
            GTPortBlockEntity port = this.menu.port();
            if (port != null) {
                PacketDistributor.sendToServer(new C2SPortTogglePacket(port.getBlockPos(),
                        C2SPortTogglePacket.Target.STACK_MODE));
            }
            return true;
        }

        // The run switch flips participation; the server applies the change and reports it back.''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('screen:', 'changed' if s != orig else 'NO CHANGE')
