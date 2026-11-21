import io, json

# ---------------- menu: count the port's real slots ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortMenu.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** How many of those cells are occupied. */
    public int usedCells() {
        return Math.min(STORAGE_CELLS, contents().size());
    }

    /** Total cells the storage band has. */
    public int totalCells() {
        return STORAGE_CELLS;
    }''',
'''    /** How many kinds the port currently holds. */
    public int usedCells() {
        return contents().size();
    }

    /**
     * How many cells the port has in all, which is its tier's slot count.
     *
     * <p>Not the size of the visible band: the band is a fixed 3 by 3 window that pages through
     * whatever the port holds, so reporting its nine would say "9" for every tier and hide the very
     * thing the readout is for.</p>
     */
    public int totalCells() {
        return switch (this.kind) {
            case ITEM -> this.port == null ? 0 : this.port.itemHandler() == null
                    ? 0 : this.port.itemHandler().getSlots();
            case FLUID -> this.port == null ? 0 : this.port.fluidHandler() == null
                    ? 0 : this.port.fluidHandler().getTanks();
            case ENERGY -> 0;
        };
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('menu:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- label: cells, not groups ----------------
zh = {'gui.goldentweaks.port.cells': '格'}
en = {'gui.goldentweaks.port.cells': 'cells'}
for path, extra in [
    (r"src/main/resources/assets/goldentweaks/lang/zh_cn.json", zh),
    (r"src/main/resources/assets/goldentweaks/lang/en_us.json", en),
]:
    data = json.load(io.open(path, encoding="utf-8"))
    data.pop('gui.goldentweaks.port.groups', None)
    data.update(extra)
    with io.open(path, "w", encoding="utf-8", newline="\n") as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write("\n")
    print("lang updated", path)

# ---------------- screen: use the new key ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''        String cells = this.menu.usedCells() + " / " + this.menu.totalCells() + " "
                + Component.translatable("gui.goldentweaks.port.groups").getString();''',
'''        String cells = this.menu.usedCells() + " / " + this.menu.totalCells() + " "
                + Component.translatable("gui.goldentweaks.port.cells").getString();''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('screen:', 'changed' if s2 != orig2 else 'NO CHANGE')
