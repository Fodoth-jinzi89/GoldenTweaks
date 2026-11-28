package net.fodoth.skina.goldentweaks.content.multiblock.basic_chemical_plant;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineItem;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockBlocks;

/**
 * Registers the reaction kettle when {@code avaritia_more_items} is absent.
 *
 * <p>It shares the {@code chemical_plant_controller} id and blockstate with the AMI multiblock
 * host, so only one of the two is ever registered; {@link GTMultiblockBlocks#register} picks.
 * Without AMI there is no structure scan, so the kettle is a plain decorative block.</p>
 *
 * <p>The entries are declared from {@link #declare()} rather than as static fields, because a
 * static initializer would run as soon as this class is loaded and would then collide with the
 * AMI host's own registration of the same id.</p>
 */
public final class StandaloneReactor {

    private static DeferredBlock<GTChemicalReactorBlock> reactor;
    private static DeferredItem<BlockItem> reactorItem;

    private StandaloneReactor() {
    }

    public static void declare() {
        reactor = GTMultiblockBlocks.BLOCKS.register("chemical_plant_controller",
                () -> new GTChemicalReactorBlock(GTMultiblockBlocks.properties()));
        reactorItem = GTMultiblockBlocks.ITEMS.register("chemical_plant_controller",
                () -> new GTMachineItem(reactor.get(), new Item.Properties()));
    }
}
