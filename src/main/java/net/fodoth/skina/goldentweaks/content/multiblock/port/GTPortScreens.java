package net.fodoth.skina.goldentweaks.content.multiblock.port;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Picks the window matching a port's kind.
 *
 * <p>All three kinds share one menu type, since the port entity carries the difference, so the
 * registration has a single hook and this is where it branches. Item and fluid ports share a
 * window: their sheets are the same drawing and they differ only in the unit beside the amount.</p>
 */
public final class GTPortScreens {

    private GTPortScreens() {
    }

    public static GTPortScreen create(GTPortMenu menu, Inventory inventory, Component title) {
        return switch (menu.kind()) {
            case ENERGY -> new GTPortEnergyScreen(menu, inventory, title);
            case GENERAL -> new GTPortGeneralScreen(menu, inventory, title);
            default -> new GTPortIOScreen(menu, inventory, title);
        };
    }
}
