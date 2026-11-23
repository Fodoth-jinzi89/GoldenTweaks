package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The advanced kettle's window.
 *
 * <p>The basic kettle's window with a different sheet. Every coordinate, every widget and the whole
 * sidebar are inherited; only {@link #texture()} differs, which is what makes the two machines look
 * distinct without a second copy of a thousand lines of layout.</p>
 */
public class GTAdvancedChemicalReactorScreen extends GTChemicalReactorScreen {

    public GTAdvancedChemicalReactorScreen(GTChemicalReactorMenu menu, Inventory inventory,
                                           Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected ResourceLocation texture() {
        return ResourceLocation.fromNamespaceAndPath(
                GoldenTweaks.MODID, "textures/gui/advanced_chemical_plant_gui.png");
    }
}
