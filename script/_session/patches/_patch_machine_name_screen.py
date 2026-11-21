import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# imports
s = s.replace('import net.minecraft.client.gui.GuiGraphics;',
              'import net.minecraft.client.gui.GuiGraphics;\nimport net.minecraft.client.gui.components.EditBox;', 1)
s = s.replace('import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactorStatus;',
              'import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactorStatus;\nimport net.fodoth.skina.goldentweaks.network.packet.C2SMachineNamePacket;', 1)
s = s.replace('import net.neoforged.fml.ModList;',
              'import net.neoforged.fml.ModList;\nimport net.neoforged.neoforge.network.PacketDistributor;', 1)

# field
s = s.replace('''    private final boolean[] held = new boolean[WIDGET_COUNT];''',
'''    /** The renaming box, drawn over the title strip. */
    private EditBox nameField;

    private final boolean[] held = new boolean[WIDGET_COUNT];''', 1)

# init
s = s.replace('''    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);''',
'''    @Override
    protected void init() {
        super.init();
        this.nameField = new EditBox(this.font, this.leftPos + TITLE_X, this.topPos + TITLE_Y,
                TITLE_WIDTH, this.font.lineHeight, Component.translatable("gui.goldentweaks.chemical_reactor.name"));
        this.nameField.setMaxLength(C2SMachineNamePacket.NAME_LIMIT);
        this.nameField.setBordered(false);
        this.nameField.setTextColor(0xFFFFFF);
        this.nameField.setTextColorUneditable(0x8A8A8A);
        this.nameField.setValue(this.menu.getDisplayName().getString());
        addRenderableWidget(this.nameField);
    }

    /** Sends the typed name if it differs from what the machine reports. */
    private void commitName() {
        if (this.nameField == null) {
            return;
        }
        String typed = this.nameField.getValue();
        String shown = this.menu.getDisplayName().getString();
        if (!typed.equals(shown)) {
            PacketDistributor.sendToServer(
                    new C2SMachineNamePacket(this.menu.getMachinePos(), typed));
        }
    }

    @Override
    public void onClose() {
        commitName();
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Clicking away from the box commits, so a name is stored without closing the window.
        if (this.nameField != null && !this.nameField.isMouseOver(mouseX, mouseY)) {
            commitName();
            this.nameField.setFocused(false);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == 257 || keyCode == 335) && this.nameField != null && this.nameField.isFocused()) {
            commitName();
            this.nameField.setFocused(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);''', 1)

# the title is now editable, so stop drawing it as a label
s = s.replace('''        drawMarquee(graphics, this.menu.getDisplayName(), TITLE_X, TITLE_Y,
                TITLE_WIDTH);''', '', 1)

# keep the box in step with the stored name
s = s.replace('''    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {''',
'''    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.nameField != null && !this.nameField.isFocused()) {
            String shown = this.menu.getDisplayName().getString();
            if (!shown.equals(this.nameField.getValue())) {
                this.nameField.setValue(shown);
            }
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
