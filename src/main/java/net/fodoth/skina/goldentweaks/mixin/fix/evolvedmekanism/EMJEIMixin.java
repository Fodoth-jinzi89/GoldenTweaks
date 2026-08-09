package net.fodoth.skina.goldentweaks.mixin.fix.evolvedmekanism;

import fr.iglee42.evolvedmekanism.jei.EMJEI;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mezz.jei.api.registration.IModIngredientRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = EMJEI.class, remap = false)
public abstract class EMJEIMixin {

    /**
     * @author GoldenTweaks
     * @reason Properly initialize Mekanism JEI chemical ingredients.
     */
    @Overwrite
    public void registerIngredients(IModIngredientRegistration registry) {
        try {
            MekanismJEI jei = new MekanismJEI();
            jei.registerIngredients(registry);
        } catch (Exception e) {
            // Ignore incompatible ingredient registration
        }
    }
}