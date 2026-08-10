package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SequencedRecipe.class)
public class SequencedRecipeMixin {

    @Redirect(
            method = "initFromSequencedAssembly",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object goldentweaks$preventEmptyIngredientSet(
            NonNullList<Object> list,
            int index,
            Object value
    ) {
        if (list.isEmpty() && index == 0) {

            GoldenTweaks.LOGGER.warn(
                    "[GoldenTweaks] Blocked invalid Create sequenced recipe: empty ingredient list"
            );

            return null;
        }

        return list.set(index, value);
    }
}
