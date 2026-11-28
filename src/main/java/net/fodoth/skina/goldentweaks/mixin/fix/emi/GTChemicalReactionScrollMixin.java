package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.WidgetGroup;
import net.fodoth.skina.goldentweaks.content.multiblock.recipe.client.GTChemicalReactionPagedGrid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Delivers the wheel and drag to the reaction page's own paged grids.
 *
 * <p>EMI only forwards {@code mouseClicked} and {@code keyPressed} to recipe widgets — its
 * {@code Widget} base class has no scroll or drag hooks at all — so a widget that pages its own
 * contents can never see either event. {@code RecipeScreen.mouseScrolled} would otherwise turn the
 * wheel into "next recipe", which is exactly wrong while the pointer is over a paged grid.</p>
 *
 * <p>The hit test is done in the widget group's own coordinates, because widget rectangles are
 * relative to the group that owns them, while the screen reports absolute mouse positions. This
 * mirrors what {@code TmrvRecipeScreenScrollMixin} already does for the JEI-aspect source page.</p>
 */
@Mixin(value = RecipeScreen.class, remap = false)
public abstract class GTChemicalReactionScrollMixin {

    /** Fractional wheel movement is accumulated; only whole pages are applied. */
    @Unique
    private static double gt$chemicalScrollAccumulator;

    @Shadow
    private List<WidgetGroup> currentPage;

    @Unique
    private static GTChemicalReactionPagedGrid gt$gridAt(WidgetGroup group, double mouseX, double mouseY) {
        double localX = mouseX - group.x();
        double localY = mouseY - group.y();

        for (Widget widget : group.widgets) {
            if (widget instanceof GTChemicalReactionPagedGrid grid
                    && grid.getBounds().contains((int) localX, (int) localY)) {
                return grid;
            }
        }
        return null;
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void gt$scrollReactionGrids(double mouseX, double mouseY, double horizontalAmount,
                                        double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (verticalAmount == 0.0 || this.currentPage == null) {
            return;
        }

        for (WidgetGroup group : this.currentPage) {
            GTChemicalReactionPagedGrid grid = gt$gridAt(group, mouseX, mouseY);
            if (grid == null) {
                continue;
            }

            gt$chemicalScrollAccumulator += verticalAmount;
            int pages = (int) gt$chemicalScrollAccumulator;
            gt$chemicalScrollAccumulator -= pages;
            if (pages != 0) {
                grid.scrollPages(-pages);
            }

            // Consume the event even when it did not add up to a whole page, otherwise EMI would
            // switch recipes from under the pointer.
            cir.setReturnValue(true);
            return;
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void gt$dragReactionScrollbar(double mouseX, double mouseY, int button,
                                          double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if (this.currentPage == null) {
            return;
        }

        for (WidgetGroup group : this.currentPage) {
            GTChemicalReactionPagedGrid grid = gt$gridAt(group, mouseX, mouseY);
            if (grid == null) {
                continue;
            }
            if (grid.scrollTo(mouseX - group.x(), mouseY - group.y())) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
