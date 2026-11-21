import io, json

# ---------------- provider: send the tier ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeProvider.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    static final String RATE = "Rate";''',
'''    static final String RATE = "Rate";
    /** Highest energy hatch tier in use, as its one-based level; zero when there is none. */
    static final String ENERGY_TIER = "EnergyTier";''', 1)

s = s.replace('''        root.putInt(RATE, machine.getInputEnergyRate());''',
'''        root.putInt(RATE, machine.getInputEnergyRate());
        root.putInt(ENERGY_TIER, machine.maxOverclock());''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('provider:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- component: the new line ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

s2 = s2.replace('''        if (root.getInt(GTReactorJadeProvider.RATE) > 0) {
            tooltip.add(Component.translatable("gui.goldentweaks.jade.max_input",''',
'''        // Between the channel and the power figure: which class of hatch the machine runs on, which
        // is what decides both the input rate below and how far it may overclock.
        int energyTier = root.getInt(GTReactorJadeProvider.ENERGY_TIER);
        if (energyTier > 0) {
            tooltip.add(Component.translatable("gui.goldentweaks.jade.energy_tier",
                    tierName(energyTier)).withStyle(ChatFormatting.GREEN));
        }

        if (root.getInt(GTReactorJadeProvider.RATE) > 0) {
            tooltip.add(Component.translatable("gui.goldentweaks.jade.max_input",''', 1)

s2 = s2.replace('''    private static GTReactorStatus statusOf(CompoundTag root) {''',
'''    /** The tier codes a player knows, in level order. */
    private static final String[] TIER_CODES = {
            "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV"
    };

    /** The one-based level as the shorthand written on the hatches. */
    private static String tierName(int level) {
        int index = Math.max(0, Math.min(TIER_CODES.length - 1, level - 1));
        return TIER_CODES[index];
    }

    private static GTReactorStatus statusOf(CompoundTag root) {''', 1)

# ---------------- products: icons only, the corner already carries the count ----------------
s2 = s2.replace('''            if (GTReactorJadeProvider.KIND_NAMED.equals(kind)) {
                tooltip.add(Component.literal(entry.getString(GTReactorJadeProvider.PRODUCT_NAME)
                        + "  x" + entry.getInt(GTReactorJadeProvider.PRODUCT_COUNT))
                        .withStyle(ChatFormatting.WHITE));
                shown++;
                continue;
            }

            IElement icon = iconOf(entry);
            if (icon == null) {
                continue;
            }
            int count = entry.getInt(GTReactorJadeProvider.PRODUCT_COUNT);
            boolean fluid = GTReactorJadeProvider.KIND_FLUID.equals(kind);
            row.add(icon);
            row.add(IElementHelper.get().spacer(2, 0));
            // Fluids are measured in millibuckets, so the unit is spelled out; an item count is a
            // bare "xN" the way a slot shows it.
            row.add(IElementHelper.get().text(Component.literal(
                    fluid ? "x" + count + " mB" : "x" + count).withStyle(ChatFormatting.WHITE)));
            row.add(IElementHelper.get().spacer(4, 0));
            shown++;''',
'''            if (GTReactorJadeProvider.KIND_NAMED.equals(kind)) {
                // No icon exists for this kind, so the amount is the only thing there is to say.
                tooltip.add(Component.literal(entry.getString(GTReactorJadeProvider.PRODUCT_NAME)
                        + "  " + entry.getInt(GTReactorJadeProvider.PRODUCT_COUNT))
                        .withStyle(ChatFormatting.WHITE));
                shown++;
                continue;
            }

            IElement icon = iconOf(entry);
            if (icon == null) {
                continue;
            }
            // The icon alone: a stack's own corner already prints its amount, and repeating it as a
            // "xN" beside the icon said the same thing twice.
            row.add(icon);
            row.add(IElementHelper.get().spacer(4, 0));
            shown++;''', 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('component:', 'changed' if s2 != orig2 else 'NO CHANGE')

# ---------------- lang ----------------
zh = {'gui.goldentweaks.jade.energy_tier': '能量等级：%s'}
en = {'gui.goldentweaks.jade.energy_tier': 'Energy tier: %s'}
for path, extra in [
    (r"src/main/resources/assets/goldentweaks/lang/zh_cn.json", zh),
    (r"src/main/resources/assets/goldentweaks/lang/en_us.json", en),
]:
    data = json.load(io.open(path, encoding="utf-8"))
    data.update(extra)
    with io.open(path, "w", encoding="utf-8", newline="\n") as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write("\n")
    print("lang updated", path)
