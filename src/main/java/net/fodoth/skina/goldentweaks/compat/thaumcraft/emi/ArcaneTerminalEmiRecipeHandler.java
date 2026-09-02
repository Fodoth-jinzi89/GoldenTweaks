package net.fodoth.skina.goldentweaks.compat.thaumcraft.emi;

import appeng.api.stacks.AEItemKey;
import appeng.core.network.serverbound.FillCraftingGridFromRecipePacket;
import appeng.util.CraftingRecipeUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumicenergistics.common.container.ContainerArcaneCraftingTerminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        ContainerArcaneCraftingTerminal menu = context.getScreenHandler();
        NonNullList<ItemStack> templates = NonNullList.withSize(9, ItemStack.EMPTY);

        List<Ingredient> ingredients = getIngredients(recipe);
        if (ingredients != null) {
            Map<AEItemKey, Long> storedAmounts = menu.snapshotStoredAmounts();
            Set<AEItemKey> craftableKeys = menu.snapshotCraftableKeys();
            Object2IntOpenHashMap<AEItemKey> usedCounts = new Object2IntOpenHashMap<>();
            for (int i = 0; i < Math.min(9, ingredients.size()); i++) {
                Ingredient ingredient = ingredients.get(i);
                if (!ingredient.isEmpty()) {
                    templates.set(i, menu.pickAvailableTemplate(ingredient, storedAmounts, craftableKeys, usedCounts));
                }
            }
        }

        // Send the real recipe id when available so the server can resolve tag ingredients
        // against the whole ME network; transient/arcane recipes fall back to templates.
        ResourceLocation recipeId = null;
        RecipeHolder<?> backing = recipe.getBackingRecipe();
        if (backing != null && backing.value() instanceof CraftingRecipe) {
            recipeId = backing.id();
            if (recipeId != null && menu.getPlayer().level().getRecipeManager().byKey(recipeId).isEmpty()) {
                recipeId = null;
            }
        }

        PacketDistributor.sendToServer(new FillCraftingGridFromRecipePacket(
                recipeId,
                templates,
                AbstractContainerScreen.hasControlDown()
        ));
        return true;
    }

    private static List<Ingredient> getIngredients(EmiRecipe recipe) {
        RecipeHolder<?> backing = recipe.getBackingRecipe();
        if (backing != null && backing.value() instanceof CraftingRecipe crafting) {
            return CraftingRecipeUtil.ensure3by3CraftingMatrix(crafting);
        }
        List<Ingredient> ingredients = new ArrayList<>(9);
        for (int i = 0; i < Math.min(9, recipe.getInputs().size()); i++) {
            ingredients.add(toIngredient(recipe.getInputs().get(i)));
        }
        while (ingredients.size() < 9) {
            ingredients.add(Ingredient.EMPTY);
        }
        return ingredients;
    }

    private static Ingredient toIngredient(EmiIngredient input) {
        List<ItemStack> stacks = new ArrayList<>();
        for (EmiStack emiStack : input.getEmiStacks()) {
            Object key = emiStack.getKey();
            if (key instanceof TagKey<?> tag && tag.isFor(BuiltInRegistries.ITEM.key())) {
                @SuppressWarnings("unchecked")
                TagKey<Item> itemTag = (TagKey<Item>) tag;
                BuiltInRegistries.ITEM.getTag(itemTag).ifPresent(named -> {
                    for (Holder<Item> holder : named) {
                        stacks.add(new ItemStack(holder.value()));
                    }
                });
                continue;
            }
            ItemStack item = emiStack.getItemStack();
            if (item != null && !item.isEmpty()) {
                stacks.add(item.copyWithCount(1));
            }
        }
        return stacks.isEmpty() ? Ingredient.EMPTY : Ingredient.of(stacks.stream());
    }
}
