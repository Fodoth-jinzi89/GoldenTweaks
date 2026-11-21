import io

# ---------------- GTPortScreen ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('''        Component storedText = Component.translatable("gui.goldentweaks.port.energy.stored_value",
                GTEnergyFormat.format(this.menu.syncedAmount()),
                GTEnergyFormat.format(this.menu.syncedCapacity()));
        Component rateText = Component.translatable("gui.goldentweaks.port.energy.rate_value",
                GTEnergyFormat.format(this.menu.syncedRate()));''',
'''        Component storedText = Component.translatable("gui.goldentweaks.port.energy.stored_value",
                formatWithUnit(this.menu.syncedAmount()),
                formatWithUnit(this.menu.syncedCapacity()));
        Component rateText = Component.translatable("gui.goldentweaks.port.energy.rate_value",
                formatWithUnit(this.menu.syncedRate()));''', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('screen:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- Jade component ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''    private static Component figureLine(String labelKey, int value) {
        return Component.translatable(labelKey).withStyle(ChatFormatting.GREEN)
                .append(Component.translatable(labelKey + ".value",
                                GTEnergyFormat.format(value))
                        .withStyle(ChatFormatting.AQUA));
    }''',
'''    private static Component figureLine(String labelKey, int value) {
        return Component.translatable(labelKey).withStyle(ChatFormatting.GREEN)
                .append(Component.literal(GTEnergyFormat.formatWithUnit(value, "FE/t"))
                        .withStyle(ChatFormatting.AQUA));
    }''', 1)
s2 = s2.replace('''        return Component.translatable("gui.goldentweaks.jade.energy",
                GTEnergyFormat.format(stored), GTEnergyFormat.format(capacity));''',
'''        return Component.translatable("gui.goldentweaks.jade.energy",
                GTEnergyFormat.formatWithUnit(stored, "FE"),
                GTEnergyFormat.formatWithUnit(capacity, "FE"));''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('jade:', 'changed' if s2 != orig2 else 'NO CHANGE')
