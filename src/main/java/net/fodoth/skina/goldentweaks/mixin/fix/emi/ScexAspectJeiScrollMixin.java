package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.jemi.impl.extras.JemiScrollGridWidget;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import net.fodoth.skina.goldentweaks.util.emi.ScexAspectJeiScrollHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Restores scrolling for JEI scroll grids rendered through EMI's JEMI bridge. */
@Mixin(value = JemiScrollGridWidget.class, remap = false)
public abstract class ScexAspectJeiScrollMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void gt$registerGrid(List<IRecipeSlotDrawable> slots, int x, int y, int gridWidth, int gridHeight,
                                  CallbackInfo cir) {
        ScexAspectJeiScrollHandler.register((JemiScrollGridWidget) (Object) this);
    }
}
