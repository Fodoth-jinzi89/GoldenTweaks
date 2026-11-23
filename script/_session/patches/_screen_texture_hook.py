"""Let the screen choose its texture, so a second machine can reuse it.

The window is one class drawing from one sheet. A second machine differs only in that sheet,
so the constant becomes a method with the basic kettle's sheet as the default.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "textures/gui/gt_multiblock_machine.png");''',
'''    /**
     * The panel sheet this window draws from.
     *
     * <p>A method rather than a constant so a second machine can substitute its own art without
     * duplicating the thousand lines of layout below: every coordinate is the same, only the
     * pixels differ.</p>
     */
    protected ResourceLocation texture() {
        return ResourceLocation.fromNamespaceAndPath(
                GoldenTweaks.MODID, "textures/gui/gt_multiblock_machine.png");
    }''', 1)

s = s.replace('''        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);''',
'''        graphics.blit(texture(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);''', 1)

s = s.replace('''        graphics.blit(TEXTURE, this.leftPos + ENERGY_X, this.topPos + ENERGY_Y,''',
'''        graphics.blit(texture(), this.leftPos + ENERGY_X, this.topPos + ENERGY_Y,''', 1)

s = s.replace('''            graphics.blit(TEXTURE, x, y, backgroundU, ADDON_SPRITE_V, ADDON_WIDTH, ADDON_HEIGHT);''',
'''            graphics.blit(texture(), x, y, backgroundU, ADDON_SPRITE_V, ADDON_WIDTH, ADDON_HEIGHT);''', 1)

s = s.replace('''                graphics.blit(TEXTURE, x, y, iconU, ADDON_ICON_V_ISOLATE, ADDON_WIDTH, ADDON_HEIGHT);''',
'''                graphics.blit(texture(), x, y, iconU, ADDON_ICON_V_ISOLATE, ADDON_WIDTH, ADDON_HEIGHT);''', 1)

s = s.replace('''            graphics.blit(TEXTURE, x, y, iconU, iconV, ADDON_WIDTH, ADDON_HEIGHT);''',
'''            graphics.blit(texture(), x, y, iconU, iconV, ADDON_WIDTH, ADDON_HEIGHT);''', 1)

s = s.replace('''        graphics.blit(TEXTURE, x, top, backgroundU, SIDE_SPRITE_V, SIDE_WIDTH, SIDE_HEIGHT);''',
'''        graphics.blit(texture(), x, top, backgroundU, SIDE_SPRITE_V, SIDE_WIDTH, SIDE_HEIGHT);''', 1)

s = s.replace('''        graphics.blit(TEXTURE, x, top, iconColumn * SIDE_WIDTH, iconV, SIDE_WIDTH, SIDE_HEIGHT);''',
'''        graphics.blit(texture(), x, top, iconColumn * SIDE_WIDTH, iconV, SIDE_WIDTH, SIDE_HEIGHT);''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("screen texture is a hook")
print("TEXTURE references left:", s.count("TEXTURE,"))
