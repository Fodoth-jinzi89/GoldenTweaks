package net.fodoth.skina.goldentweaks.mixin.feature.avaritia_more_items;

import net.fodoth.skina.goldentweaks.content.multiblock.common.GTExactEnergy;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MatchedMultiblockRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.math.BigInteger;

/**
 * Gives AMI's matched recipe an exact energy figure.
 *
 * <p>AMI stores {@code energyPerTick} as an {@code int}, which a large parallel multiplier or a
 * high overclock can exceed. Saturating there would silently make a huge machine behave like a
 * merely large one, so the exact figure is carried beside the record instead.</p>
 *
 * <p>An interface rather than a mutable field: the value is known when the record is built, and the
 * accessor lets our code read it without the record itself having to change shape. The default is
 * the record's own {@code int}, so a recipe nobody has scaled keeps working untouched.</p>
 */
@Mixin(value = MatchedMultiblockRecipe.class, remap = false)
public abstract class GTExactEnergyMixin implements GTExactEnergy {

    /** The exact figure, or {@code null} until something sets it. */
    @Unique
    private BigInteger gt$exactEnergy;

    @Override
    public void gt$setExactEnergy(BigInteger exact) {
        this.gt$exactEnergy = exact;
    }

    @Override
    public BigInteger gt$exactEnergy() {
        if (this.gt$exactEnergy != null) {
            return this.gt$exactEnergy;
        }
        // No exact figure recorded: fall back to what the record actually holds.
        return BigInteger.valueOf(((MatchedMultiblockRecipe) (Object) this).energyPerTick());
    }
}
