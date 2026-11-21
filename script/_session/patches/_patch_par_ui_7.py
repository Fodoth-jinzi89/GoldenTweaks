import io, json

# ---------- 1. the parallel pane icon becomes the 1-parallel control circuit ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** The pane's own icon: the overclock circuit or the parallel one. */
    private void renderPaneIcon(GuiGraphics graphics, Pane pane, int x, int y) {
        if (pane == this.overclockPane) {
            graphics.renderItem(overclockIcon(), x, y);
            return;
        }
        // A plain sprite rather than an item, so it is blitted at the slot's own size.
        graphics.blit(PARALLEL_ICON_TEXTURE, x, y, 0, 0,
                PARALLEL_ICON_SIZE, PARALLEL_ICON_SIZE,
                PARALLEL_ICON_SIZE, PARALLEL_ICON_SIZE);
    }''',
'''    /** The pane's own icon: the 4/2 overclock circuit or the parallel one. */
    private void renderPaneIcon(GuiGraphics graphics, Pane pane, int x, int y) {
        graphics.renderItem(pane == this.overclockPane ? overclockIcon() : parallelIcon(), x, y);
    }''', 1)

s = s.replace('''    /** A stand-in stack for the overclock control circuit, so no real stack is needed. */
    private static ItemStack overclockIcon() {
        return new ItemStack(GTMultiblockBlocks.CONTROL_CIRCUIT_OVERCLOCK_4_2.get());
    }''',
'''    /** A stand-in stack for the 4/2 overclock control circuit, so no real stack is needed. */
    private static ItemStack overclockIcon() {
        return new ItemStack(GTMultiblockBlocks.CONTROL_CIRCUIT_OVERCLOCK_4_2.get());
    }

    /** A stand-in stack for the parallel control circuit. */
    private static ItemStack parallelIcon() {
        return new ItemStack(GTMultiblockBlocks.CONTROL_CIRCUIT_PARALLEL.get());
    }''', 1)

# ---------- 2. the parallel pane sits 2 px higher ----------
s = s.replace('''    /** Vertical gap between the two panes' collapsed tabs. */
    private static final int SIDEBAR_STACK_STEP = SIDEBAR_MIN_HEIGHT + 2;''',
'''    /** Vertical gap between the two panes' collapsed tabs. */
    private static final int SIDEBAR_STACK_STEP = SIDEBAR_MIN_HEIGHT + 2;
    /** The parallel pane is nudged up by this much, so the two tabs read as one column. */
    private static final int PARALLEL_PANE_OFFSET_Y = -2;''', 1)

s = s.replace('''    /** Top edge of a pane, in panel coordinates: panes stack downward from the top right. */
    private static int paneTop(int index) {
        return SIDEBAR_Y + index * SIDEBAR_STACK_STEP;
    }''',
'''    /**
     * Top edge of a pane, in panel coordinates.
     *
     * <p>The second pane is not merely stacked below the first: it is pushed down by however far
     * the first one has opened. Otherwise an expanding pane would draw over its neighbour, and the
     * two would overlap for the whole animation.</p>
     */
    private int paneTop(int index) {
        int top = SIDEBAR_Y + index * SIDEBAR_STACK_STEP;
        if (index > 0) {
            top += PARALLEL_PANE_OFFSET_Y;
            // Everything the pane above has grown past its collapsed height pushes this one down.
            for (int above = 0; above < index; above++) {
                top += this.panes.get(above).height - SIDEBAR_MIN_HEIGHT;
            }
        }
        return top;
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('screen:', 'changed' if s != orig else 'NO CHANGE')

# ---------- 3. caption wording ----------
zh = {'gui.goldentweaks.chemical_reactor.sidebar.parallel.max': '最大：1'}
en = {'gui.goldentweaks.chemical_reactor.sidebar.parallel.max': 'Max: 1'}
for path, extra in [
    (r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh),
    (r'src/main/resources/assets/goldentweaks/lang/en_us.json', en),
]:
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(extra)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print('lang updated', path)
