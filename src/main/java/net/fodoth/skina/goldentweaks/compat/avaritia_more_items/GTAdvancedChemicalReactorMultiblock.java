package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTChemicalReactorItem;
import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers the advanced kettle: its block, item, block entity and window.
 *
 * <p>The block class is the basic kettle's, unchanged — it places, rotates and opens a menu the
 * same way. What differs is registered here: a distinct id, a distinct block entity type (the
 * type is what decides which entity class a placed block gets), and a distinct menu type.</p>
 */
public final class GTAdvancedChemicalReactorMultiblock {

    public static final String ID = "advanced_chemical_plant_controller";

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(GoldenTweaks.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GoldenTweaks.MODID);

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, GoldenTweaks.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GTChemicalReactorMenu>> MENU =
            MENU_TYPES.register("advanced_chemical_reactor", () -> IMenuTypeExtension.create(
                    (containerId, inventory, data) ->
                            GTChemicalReactorMenu.create(menuType(), containerId, inventory,
                                    data.readBlockPos())));

    /**
     * The menu type, read through a call rather than the field.
     *
     * <p>The factory below is stored inside {@code MENU} itself, so naming the field from its own
     * initializer would be a self-reference the compiler rejects. A method body is deferred until
     * the factory actually runs, by which point the field is set.</p>
     */
    private static MenuType<?> menuType() {
        return MENU.get();
    }

    /**
     * The block itself.
     *
     * <p>Reusing the basic kettle's class is deliberate: the block is only a shell that forwards to
     * its block entity, and every difference lives there.</p>
     */
    public static final DeferredBlock<GTChemicalReactorMachineBlock> MACHINE =
            BLOCKS.register(ID,
                    () -> new GTChemicalReactorMachineBlock(GTMultiblockBlocks.properties(),
                            "advanced_chemical_reactor", GTAdvancedChemicalReactorBlockEntity::new,
                            GTMultiblockBlocks.SHELL_PP));

    @SuppressWarnings("unused")
    public static final DeferredItem<BlockItem> MACHINE_ITEM =
            ITEMS.register(ID,
                    () -> new GTChemicalReactorItem(MACHINE.get(), new Item.Properties(),
                            "block.goldentweaks.shell_pp"));

    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<GTAdvancedChemicalReactorBlockEntity>> MACHINE_ENTITY =
            BLOCK_ENTITIES.register(ID,
                    () -> BlockEntityType.Builder.of(
                            GTAdvancedChemicalReactorBlockEntity::new,
                            MACHINE.get()
                    ).build(null));

    private GTAdvancedChemicalReactorMultiblock() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENU_TYPES.register(eventBus);
        GTAdvancedChemicalReactionRecipes.register(eventBus);
    }
}
