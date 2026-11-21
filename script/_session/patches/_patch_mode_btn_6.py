import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * The visible slice of the storage grid.
     *
     * <p>Only occupied cells are drawn: the frame drawn by the sheet already marks the grid, and
     * empty boxes would add nothing but noise.</p>
     */
    private void drawStacks(GuiGraphics graphics) {
        List<GTReactionStack> rows = contents();
        if (rows.isEmpty()) {
            return;
        }
        HolderLookup.Provider registries = registries();
        var context = dev.emi.emi.runtime.EmiDrawContext.wrap(graphics);

        int first = this.scrollRow * GTPortMenu.STORAGE_COLUMNS;
        int cells = Math.min(GTPortMenu.STORAGE_CELLS, rows.size() - first);
        for (int index = 0; index < cells; index++) {
            int column = index % GTPortMenu.STORAGE_COLUMNS;
            int row = index / GTPortMenu.STORAGE_COLUMNS;
            int x = this.leftPos + GTPortMenu.STORAGE_X + column * GTPortMenu.SLOT_STEP;
            int y = this.topPos + GTPortMenu.STORAGE_Y + row * GTPortMenu.SLOT_STEP;
            drawStack(graphics, context, rows.get(first + index), x, y, registries);
        }
    }''',
'''    /**
     * The visible slice of the storage grid.
     *
     * <p>Only occupied cells are drawn: the frame drawn by the sheet already marks the grid, and
     * empty boxes would add nothing but noise.</p>
     *
     * <p>How many cells an entry takes is the stacking mode's whole effect. In single-cell mode each
     * entry is one cell wide, so the grid is a plain row-major list. In multi-cell mode an entry
     * occupies as many cells as it needs, measured as the ceiling of its amount over the slot
     * ceiling — a stack of 64 in a 64-per-slot bus takes one cell, a stack of 192 takes three.</p>
     */
    private void drawStacks(GuiGraphics graphics) {
        List<GTReactionStack> rows = contents();
        if (rows.isEmpty()) {
            return;
        }
        HolderLookup.Provider registries = registries();
        var context = dev.emi.emi.runtime.EmiDrawContext.wrap(graphics);

        int perSlot = Math.max(1, this.menu.slotLimit());
        int columns = GTPortMenu.STORAGE_COLUMNS;
        int capacity = GTPortMenu.STORAGE_CELLS;

        // Walk entries, reserving however many cells each one claims, and stop when the visible
        // window is full. `cursor` is the first cell of the entry currently being laid out.
        int cursor = 0;
        int drawn = 0;
        for (GTReactionStack stack : rows) {
            if (drawn >= capacity) {
                break;
            }
            int span = this.menu.isSingleCellMode()
                    ? 1 : Math.max(1, Math.ceilDiv(stack.amount(), perSlot));
            // Skip entries above the visible window, and clip one that starts inside it.
            if (cursor + span <= this.scrollRow * columns) {
                cursor += span;
                continue;
            }
            for (int cell = 0; cell < span && drawn < capacity; cell++) {
                int absolute = cursor + cell;
                int windowCell = absolute - this.scrollRow * columns;
                if (windowCell < 0) {
                    continue;
                }
                if (windowCell >= capacity) {
                    break;
                }
                int column = windowCell % columns;
                int row = windowCell / columns;
                int x = this.leftPos + GTPortMenu.STORAGE_X + column * GTPortMenu.SLOT_STEP;
                int y = this.topPos + GTPortMenu.STORAGE_Y + row * GTPortMenu.SLOT_STEP;
                drawStack(graphics, context, stack, x, y, registries);
                drawn++;
            }
            cursor += span;
        }
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('screen:', 'changed' if s != orig else 'NO CHANGE')
