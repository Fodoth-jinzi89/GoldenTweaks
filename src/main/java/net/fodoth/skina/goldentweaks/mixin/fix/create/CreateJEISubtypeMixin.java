package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.compat.jei.CreateJEI;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CreateJEI.class)
public class CreateJEISubtypeMixin {

    @WrapOperation(
            method = "registerFluidSubtypes",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/registration/ISubtypeRegistration;registerSubtypeInterpreter(Lmezz/jei/api/ingredients/IIngredientTypeWithSubtypes;Ljava/lang/Object;Lmezz/jei/api/ingredients/subtypes/ISubtypeInterpreter;)V"
            ),
            remap = false
    )
    private <B, I> void goldentweaks$ignoreDuplicateSubtypeRegistration(
            ISubtypeRegistration instance,
            IIngredientTypeWithSubtypes<B, I> ingredientType,
            B ingredientBase,
            ISubtypeInterpreter<I> interpreter,
            Operation<Void> original
    ) {
        try {
            original.call(instance, ingredientType, ingredientBase, interpreter);
        } catch (Exception ignored) {
        }
    }
}