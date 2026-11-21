import io, shutil, os

# ---- 1. copy the updated sprite ----
src = r'multiblock\cc_1.png'
dst = r'src/main/resources/assets/goldentweaks/textures/gui/cc_1.png'
shutil.copyfile(src, dst)
print('sprite updated ->', dst, os.path.getsize(dst), 'bytes')

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- 2. the drawn slots are gone from the new art; the item has to go where they were ----
s = s.replace('''    /**
     * The drawn slot inside the pane, where the projector's icon goes.
     *
     * <p>Both states carry their own slot and they do not sit at the same offset: the collapsed tab
     * puts it at (1, 5) and the expanded panel at (3, 6). Rendering to each state's own offset is
     * what keeps the icon inside the frame while the pane is opening, rather than sliding in from
     * the corner as the artwork is revealed.</p>
     */''',
'''    /**
     * Where the control circuit's icon goes inside the pane.
     *
     * <p>The current artwork draws no slot of its own, so these are the places the frame leaves
     * for it: the collapsed tab keeps its icon in the upper part of the small square, and the
     * expanded panel in the wide band above its title bar. Each state gets its own offset because
     * they are not at the same distance from the pane's corner, and the artwork is revealed from
     * that corner as the pane opens.</p>
     */''', 1)

# ---- 3. the item itself ----
s = s.replace('''    /** The projector's icon, in the pane's drawn slot. */
    private void renderSidebarSlot(GuiGraphics graphics, int x, int y) {
        graphics.renderItem(projectorIcon(), x, y);
    }

    /** A stand-in stack for the projector item, with its own renderer so no real stack is needed. */
    private static ItemStack projectorIcon() {
        return new ItemStack(GTMultiblockBlocks.PROJECTOR.get());
    }''',
'''    /** The control circuit's icon, in the pane's slot. */
    private void renderSidebarSlot(GuiGraphics graphics, int x, int y) {
        graphics.renderItem(overclockIcon(), x, y);
    }

    /** A stand-in stack for the overclock control circuit, so no real stack is needed. */
    private static ItemStack overclockIcon() {
        return new ItemStack(GTMultiblockBlocks.CONTROL_CIRCUIT_OVERCLOCK_4_2.get());
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('screen:', 'changed' if s != orig else 'NO CHANGE')
