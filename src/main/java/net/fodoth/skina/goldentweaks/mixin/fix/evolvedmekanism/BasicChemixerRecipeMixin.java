package net.fodoth.skina.goldentweaks.mixin.fix.evolvedmekanism;

import fr.iglee42.evolvedmekanism.impl.BasicChemixerRecipe;
import fr.iglee42.evolvedmekanism.registries.EMBlocks;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = BasicChemixerRecipe.class, remap = false)
public abstract class BasicChemixerRecipeMixin {

    /**
     * @author GoldenTweaks
     * @reason Fix incorrect recipe group.
     */
    @Overwrite
    public String getGroup() {
        return EMBlocks.CHEMIXER.getName();
    }

    /**
     * @author GoldenTweaks
     * @reason Fix incorrect toast symbol block.
     */
    @Overwrite
    public ItemStack getToastSymbol() {
        return new ItemStack(EMBlocks.CHEMIXER.asItem());
    }
}