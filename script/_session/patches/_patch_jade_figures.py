import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''            tooltip.add(Component.translatable("gui.goldentweaks.jade.power",
                    GTEnergyFormat.format(power)).withStyle(ChatFormatting.GREEN));''',
'''            tooltip.add(figureLine("gui.goldentweaks.jade.power", power));''', 1)

s = s.replace('''            tooltip.add(Component.translatable("gui.goldentweaks.jade.max_input",
                    GTEnergyFormat.format(root.getInt(GTReactorJadeProvider.RATE)))
                    .withStyle(ChatFormatting.GRAY));''',
'''            tooltip.add(figureLine("gui.goldentweaks.jade.max_input", rate));''', 1)

s = s.replace('''        if (root.getInt(GTReactorJadeProvider.RATE) > 0) {''',
'''        int rate = root.getInt(GTReactorJadeProvider.RATE);
        if (rate > 0) {''', 1)

s = s.replace('''    /** The tier codes a player knows, in level order. */''',
'''    /**
     * A label and a figure, in two colours: the label green, the number and its unit aqua.
     *
     * <p>Split rather than formatted into one string so the number is the part that stands out —
     * these are the two figures a player opens the panel for. The unit goes inside the translatable
     * value, exactly as the energy bar's own label does it, so "XFE/t" stays in the translation
     * rather than being spelled out here.</p>
     */
    private static Component figureLine(String labelKey, int value) {
        return Component.translatable(labelKey).withStyle(ChatFormatting.GREEN)
                .append(Component.translatable(labelKey + ".value",
                                GTEnergyFormat.format(value))
                        .withStyle(ChatFormatting.AQUA));
    }

    /** The tier codes a player knows, in level order. */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
