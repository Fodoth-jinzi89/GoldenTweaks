package net.fodoth.skina.goldentweaks.compat.thaumichorizons;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * One row of the rift crafting viewer page: what goes into the rift and what comes out.
 *
 * <p>{@code input} is either the expected {@link ItemStack} or a {@link TagKey TagKey&lt;Item&gt;}.
 * {@code output} is empty for the built-in Thaumic Horizons recipes that produce something other
 * than an item (a pocket plane, a voidling golem, wisps); those carry a {@code note} instead.
 */
public record GTRiftDisplayRecipe(
        ResourceLocation id,
        Object input,
        ItemStack output,
        @Nullable Component note
) {

    public boolean hasOutput() {
        return !output.isEmpty();
    }

    public boolean hasNote() {
        return note != null;
    }

    public boolean hasItemInput() {
        return input instanceof ItemStack;
    }

    public boolean hasTagInput() {
        return input instanceof TagKey<?>;
    }

    @Nullable
    public ItemStack inputStack() {
        return input instanceof ItemStack stack ? stack : null;
    }

    @Nullable
    public TagKey<Item> inputTag() {
        if (!(input instanceof TagKey<?> tag)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        TagKey<Item> itemTag = (TagKey<Item>) tag;
        return itemTag;
    }
}
