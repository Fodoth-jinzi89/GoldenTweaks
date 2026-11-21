package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import net.fodoth.skina.goldentweaks.compat.emi.GTIngredientSnapshots;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ListEmiIngredient.class, remap = false)
public abstract class ListEmiIngredientMixin {

    @Shadow @Final
    private List<EmiStack> fullList;

    @Inject(method = "copy", at = @At("RETURN"))
    private void gt$shareUnchangedSnapshot(CallbackInfoReturnable<EmiIngredient> cir) {
        EmiIngredient copy = cir.getReturnValue();
        List<EmiStack> snapshot = copy.getEmiStacks();
        List<EmiStack> shared = GTIngredientSnapshots.reuse(fullList, snapshot);
        if (shared != snapshot) {
            ((ListEmiIngredientAccessor) copy).gt$setFullList(shared);
        }
    }
}
