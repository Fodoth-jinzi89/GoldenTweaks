package net.fodoth.skina.goldentweaks.mixin.fix;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import fr.iglee42.evolvedmekanism.recipes.ChemixerRecipe;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ChemixerRecipe.class, remap = false)
public abstract class ChemixerRecipeMixin {

    @Shadow
    @Final
    private ChemicalStackIngredient inputGas;


    @Inject(method = "isIncomplete", at = @At("RETURN"), cancellable = true)

    private void goldentweaks$validateChemicals(CallbackInfoReturnable<Boolean> cir) {

        if (cir.getReturnValue()) {
            return;
        }

        List<ChemicalStack> representations = this.inputGas.getRepresentations();

        if (representations.isEmpty()) {
            cir.setReturnValue(true);
            return;
        }

        for (ChemicalStack stack : representations) {

            if (stack == null || stack.isEmpty()) {
                cir.setReturnValue(true);
                return;
            }

            if (stack.getAmount() <= 0) {
                cir.setReturnValue(true);
                return;
            }

            Chemical chemical = stack.getChemical();
            @SuppressWarnings("removal")
            var bool = !chemical.getAsHolder().isBound();

            if (bool) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
