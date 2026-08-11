package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

abstract class AbstractGTThaumcraftRecipe<T extends AbstractGTThaumcraftRecipe<T>> {

    protected final String research;
    protected final ItemStack output;
    protected final AspectList aspects = new AspectList();

    protected AbstractGTThaumcraftRecipe(String research, ItemStack output) {
        this.research = research;
        this.output = output.copy();
    }

    public T aspect(Aspect aspect, int amount) {
        if (aspect != null && amount > 0) {
            aspects.add(aspect, amount);
        }
        return self();
    }

    protected abstract T self();
}
