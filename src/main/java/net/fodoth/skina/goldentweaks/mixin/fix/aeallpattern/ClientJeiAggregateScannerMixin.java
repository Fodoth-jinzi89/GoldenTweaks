package net.fodoth.skina.goldentweaks.mixin.fix.aeallpattern;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.langqi99.aeallpattern.client.ClientJeiAggregateScanner;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.nolij.toomanyrecipeviewers.impl.ingredient.TMRVStack;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient.TMRVSlotWidget;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.ingredients.TypedIngredient;
import mezz.jei.api.runtime.IJeiRuntime;
import net.fodoth.skina.goldentweaks.compat.aeallpattern.EmiAggregateScanner;
import net.minecraft.core.BlockPos;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.stream.Stream;

@Mixin(ClientJeiAggregateScanner.class)
public abstract class ClientJeiAggregateScannerMixin {
    @Redirect(
            method = {"chooseStack", "chooseInputSlot"},
            at = @At(value = "INVOKE", target = "Lmezz/jei/api/gui/ingredient/IRecipeSlotView;getAllIngredients()Ljava/util/stream/Stream;")
    )
    private static Stream<?> goldentweaks$emiIngredients(IRecipeSlotView slot) {
        if (slot instanceof TMRVSlotWidget tmrv) {
            EmiIngredient ingredient = tmrv.getStack();
            return Stream.concat(
                    slot.getAllIngredients(),
                    ingredient.getEmiStacks().stream().map(ClientJeiAggregateScannerMixin::goldentweaks$typedIngredient).filter(Objects::nonNull)
            );
        }
        return slot.getAllIngredients();
    }

    @Unique
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ITypedIngredient<?> goldentweaks$typedIngredient(EmiStack stack) {
        if (stack instanceof TMRVStack tmrv) {
            return TypedIngredient.createUnvalidated(tmrv.type, tmrv.ingredient);
        }
        var itemStack = stack.getItemStack();
        return itemStack.isEmpty()
                ? null
                : TypedIngredient.createUnvalidated(VanillaTypes.ITEM_STACK, itemStack);
    }

    @WrapOperation(
            method = "onRightClickBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/langqi99/aeallpattern/client/ClientJeiAggregateScanner;scan(Lmezz/jei/api/runtime/IJeiRuntime;Lnet/minecraft/core/BlockPos;)V"
            )
    )
    private static void goldentweaks$guardScan(
            IJeiRuntime runtime,
            BlockPos pos,
            Operation<Void> original
    ) {
        if (ModList.get().isLoaded("toomanyrecipeviewers")) {
            try {
                if (EmiAggregateScanner.scan(pos)) {
                    return;
                }
            } catch (RuntimeException ignored) {
                // Fall back to the original JEI scanner if EMI is not ready yet.
            }
        }
        try {
            original.call(runtime, pos);
        } catch (RuntimeException ignored) {
            // A malformed third-party recipe must not crash the client.
        }
    }
}
