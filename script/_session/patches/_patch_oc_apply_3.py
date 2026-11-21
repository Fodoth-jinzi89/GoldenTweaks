import io, json

# ---- menu: expose the effective value ----
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('''    /** The overclock ceiling as the side pane shows it. */
    public int getOverclockLimit() {
        return this.data.get(DATA_OVERCLOCK);
    }''',
'''    /** The overclock setting as the side pane shows it; {@code -1} means automatic. */
    public int getOverclockLimit() {
        return this.data.get(DATA_OVERCLOCK);
    }

    /**
     * The overclock levels actually in force.
     *
     * <p>Not the same as {@link #getOverclockLimit}: the setting may say automatic, and either way
     * the energy ports cap it. The side pane shows this one, because it is what the machine runs
     * at and what the cost is billed against.</p>
     */
    public int getOverclockSteps() {
        return this.tile == null ? 0 : this.tile.overclockSteps();
    }''', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('menu:', 'changed' if s != orig else 'NO CHANGE')

# ---- screen: first line shows the live ratio ----
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''        Component first = Component.translatable(overclock
                ? "gui.goldentweaks.chemical_reactor.sidebar.ratio"
                : "gui.goldentweaks.chemical_reactor.sidebar.parallel.title");''',
'''        Component first = Component.translatable(overclock
                ? "gui.goldentweaks.chemical_reactor.sidebar.ratio"
                : "gui.goldentweaks.chemical_reactor.sidebar.parallel.title",
                overclock ? ratioLabel() : Component.empty());''', 1)

s2 = s2.replace('''    /** The control circuit's icon, in the pane's slot. */''',
'''    /**
     * The live ratio, as {@code 4/2}, {@code 16/8} and so on.
     *
     * <p>Each step quadruples the power and halves the time, so both figures of the ratio are powers
     * of four over two. At zero steps it is exactly the item's own {@code 4/2} marking.</p>
     */
    private Component ratioLabel() {
        int steps = this.menu.getOverclockSteps();
        int duty = 1 << (2 * steps);
        int time = 1 << steps;
        return Component.literal(duty + "/" + time);
    }

    /** The control circuit's icon, in the pane's slot. */''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('screen:', 'changed' if s2 != orig2 else 'NO CHANGE')

# ---- lang: the ratio line takes a parameter now ----
zh = {'gui.goldentweaks.chemical_reactor.sidebar.ratio': '超频比：%s'}
en = {'gui.goldentweaks.chemical_reactor.sidebar.ratio': 'Ratio: %s'}
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
