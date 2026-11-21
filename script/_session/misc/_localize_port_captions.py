import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    private void drawEnergyCaptions(GuiGraphics graphics) {
        String storedText = GTEnergyFormat.format(this.menu.syncedAmount())
                + "FE/" + GTEnergyFormat.format(this.menu.syncedCapacity()) + "FE";
        String rateText = GTEnergyFormat.format(this.menu.syncedRate()) + "FE/t";
''',
'''    private void drawEnergyCaptions(GuiGraphics graphics) {
        // Both figures go through the translations, units included: the port window and the machine
        // tooltips have to spell "1.06 kFE/t" the same way, and only one place can own that wording.
        Component storedText = Component.translatable("gui.goldentweaks.port.energy.stored_value",
                GTEnergyFormat.format(this.menu.syncedAmount()),
                GTEnergyFormat.format(this.menu.syncedCapacity()));
        Component rateText = Component.translatable("gui.goldentweaks.port.energy.rate_value",
                GTEnergyFormat.format(this.menu.syncedRate()));
''', 1)

s = s.replace('''        graphics.drawString(this.font, storedText, 0, 0, CAPTION_COLOUR, false);''',
'''        graphics.drawString(this.font, storedText, 0, 0, CAPTION_COLOUR, false);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
