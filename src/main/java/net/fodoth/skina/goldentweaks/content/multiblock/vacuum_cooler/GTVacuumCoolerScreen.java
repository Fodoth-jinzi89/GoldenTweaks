package net.fodoth.skina.goldentweaks.content.multiblock.vacuum_cooler;

import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineMenu;
import net.fodoth.skina.goldentweaks.content.multiblock.client.GTMachineScreen;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The vacuum cooler's window.
 *
 * <p>The basic kettle's layout on the cooler's own sheet. Every coordinate and widget is inherited;
 * only {@link #texture()} differs, the same way the advanced kettle's window is done.</p>
 */
public class GTVacuumCoolerScreen extends GTMachineScreen {

    public GTVacuumCoolerScreen(GTMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected ResourceLocation texture() {
        return ResourceLocation.fromNamespaceAndPath(
                GoldenTweaks.MODID, "textures/gui/vacuum_cooler_screen.png");
    }
}
