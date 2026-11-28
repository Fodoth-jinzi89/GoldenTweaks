package net.fodoth.skina.goldentweaks.content.multiblock.advanced_chemical_plant;

import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineMenu;
import net.fodoth.skina.goldentweaks.content.multiblock.client.GTMachineScreen;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTEnergyFormat;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The advanced kettle's window.
 *
 * <p>The basic kettle's window with a different sheet. Every coordinate, every widget and the whole
 * sidebar are inherited; only {@link #texture()} differs, which is what makes the two machines look
 * distinct without a second copy of a thousand lines of layout.</p>
 */
public class GTAdvancedChemicalReactorScreen extends GTMachineScreen {

    public GTAdvancedChemicalReactorScreen(GTMachineMenu menu, Inventory inventory,
                                           Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected ResourceLocation texture() {
        return ResourceLocation.fromNamespaceAndPath(
                GoldenTweaks.MODID, "textures/gui/advanced_chemical_plant_gui.png");
    }

    /**
     * The fourfold-parallel circuit, not the single-parallel one.
     *
     * <p>This is the parallel pane's icon. The overclock pane keeps the 4/2 circuit: the advanced
     * kettle overclocks at the same ratio as the basic one, and what it changes is how many copies
     * it may run.</p>
     */
    @Override
    protected ItemStack parallelIcon() {
        return new ItemStack(GTMultiblockBlocks.CONTROL_CIRCUIT_PARALLEL_4X.get());
    }

    /**
     * The copy ceiling as a bare figure, which is what the box beneath it edits.
     *
     * <p>Grouped rather than printed raw: the ceiling is four per voltage tier, and the top of the
     * hatch ladder is deep enough that a high-tier machine reaches five figures. An unbroken run of
     * digits would be unreadable at the pane's 0.7 scale.</p>
     */
    @Override
    protected Component parallelCeilingLabel() {
        return Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.parallel.max_4x",
                GTEnergyFormat.formatExpanded(this.menu.getMachineParallelCeiling()));
    }
}
