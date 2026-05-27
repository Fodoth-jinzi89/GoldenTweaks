package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryHandler;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SymmetryHandler.class)
public class SymmetryHandlerMixin {

    @WrapOperation(
            method = "onRenderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tterrag/registrate/util/entry/ItemEntry;isIn(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean goldentweaks$preventUnboundWandCrash(
            ItemEntry<?> instance,
            ItemStack stack,
            Operation<Boolean> original
    ) {
        try {

            /*
             * Create 某些版本下 WAND_OF_SYMMETRY 可能未绑定
             */
            if (instance == AllItems.WAND_OF_SYMMETRY) {

                Object delegate = instance.getDelegate();

                if (delegate instanceof DeferredHolder<?, ?> holder
                        && !holder.isBound()) {
                    return false;
                }
            }

            return original.call(instance, stack);

        } catch (Exception e) {
            return false;
        }
    }
}