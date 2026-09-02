package net.fodoth.skina.goldentweaks.compat.thaumcraft.emi;

import appeng.core.network.serverbound.FillCraftingGridFromRecipePacket;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumicenergistics.common.container.ContainerArcaneCraftingTerminal;

import java.util.ArrayList;
import java.util.List;

/** EMI transfer support for Thaumic Energistics' arcane crafting terminal. */
public class ArcaneTerminalEmiRecipeHandler implements StandardRecipeHandler<ContainerArcaneCraftingTerminal> {
    @Override public List<Slot> getInputSources(ContainerArcaneCraftingTerminal menu) {
        return List.of();
    }
    @Override public List<Slot> getCraftingSlots(ContainerArcaneCraftingTerminal menu) {
        List<Slot> slots = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) slots.add(menu.getSlot(i));
        return slots;
    }
    @Override public Slot getOutputSlot(ContainerArcaneCraftingTerminal menu) { return menu.getSlot(9); }
    @Override public boolean supportsRecipe(EmiRecipe recipe) { return true; }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ContainerArcaneCraftingTerminal> context) {
        return recipe.getInputs().stream().anyMatch(input -> input.getEmiStacks().stream()
                .anyMatch(stack -> !stack.getItemStack().isEmpty()));
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<ContainerArcaneCraftingTerminal> context) {
        NonNullList<ItemStack> templates = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(9, recipe.getInputs().size()); i++) {
            final var input = recipe.getInputs().get(i);
            ItemStack template = input.getEmiStacks().stream()
                    .map(EmiStack::getItemStack)
                    .filter(stack -> !stack.isEmpty())
                    .findFirst()
                    .orElseGet(() -> input.getEmiStacks().stream()
                            .map(EmiStack::getKey)
                            .filter(TagKey.class::isInstance)
                            .map(TagKey.class::cast)
                            .filter(tag -> tag.isFor(BuiltInRegistries.ITEM.key()))
                            .map(tag -> BuiltInRegistries.ITEM.getTag((TagKey<net.minecraft.world.item.Item>) tag)
                                    .flatMap(named -> named.stream().findFirst())
                                    .map(holder -> new ItemStack(holder.value()))
                                    .orElse(ItemStack.EMPTY))
                            .findFirst()
                            .orElse(ItemStack.EMPTY));
            templates.set(i, template);
        }
        PacketDistributor.sendToServer(new FillCraftingGridFromRecipePacket(
                recipe.getId(),
                templates,
                AbstractContainerScreen.hasControlDown()
        ));
        return true;
    }
}
