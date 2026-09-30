package net.fodoth.skina.goldentweaks.compat.thaumicbases;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import tb.common.registry.TBBlockEntityTypes;

/**
 * Thaumcraft registers {@code Capabilities.ItemHandler.BLOCK} for its own alchemical furnace block entity type only.
 * Thaumic Bases' advanced furnace has its own block entity type, so it inherited the container logic but none of the
 * capability registration - pipes, hoppers and other logistics simply could not reach it.
 */
public final class GTThaumicBasesFurnaceCompat {

    private GTThaumicBasesFurnaceCompat() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TBBlockEntityTypes.ADVANCED_ALCHEMICAL_FURNACE.get(),
                (furnace, side) -> new GTAlchemicalFurnaceItemHandler(furnace, side)
        );
    }
}
