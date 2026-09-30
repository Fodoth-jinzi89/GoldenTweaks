package net.fodoth.skina.goldentweaks.mixin.fix.tradecycling;

import de.maxhenkel.tradecycling.gui.CycleTradesButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes Trade Cycling's "刷新交易" (refresh trades) button flashing and disappearing in the villager
 * trading screen.
 *
 * <p>{@code CycleTradesButton#renderWidget} recomputes {@code visible = canCycle(menu)} every time it
 * runs - but {@code AbstractWidget#render} only calls {@code renderWidget} while {@code visible} is
 * already {@code true}. So on the frame where the menu is not yet fully synced (or after the villager
 * has been traded with) the button hides itself once and can never re-evaluate: it shows for a single
 * frame, then stays gone until the screen is rebuilt. Reopening makes it flash again for the same
 * reason.</p>
 *
 * <p>Reported upstream as henkelmax/trade-cycling issues #34 and #47. Here we recompute the button's
 * visibility from the screen itself, once per frame, before the widgets are drawn, so the button
 * appears exactly when cycling is actually allowed and never latches off.</p>
 */
@Mixin(MerchantScreen.class)
public abstract class MerchantScreenTradeCyclingMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void goldentweaks$syncCycleTradesButton(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo ci
    ) {

        MerchantMenu menu = ((MerchantScreen) (Object) this).getMenu();
        if (menu == null) {
            return;
        }

        for (Renderable renderable : ((Screen) (Object) this).renderables) {
            if (renderable instanceof CycleTradesButton button) {
                button.visible = CycleTradesButton.canCycle(menu);
            }
        }
    }
}
