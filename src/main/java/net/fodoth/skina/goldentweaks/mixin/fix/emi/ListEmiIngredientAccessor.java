package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = ListEmiIngredient.class, remap = false)
public interface ListEmiIngredientAccessor {

    @Mutable
    @Accessor("fullList")
    void gt$setFullList(List<EmiStack> stacks);
}
