package net.fodoth.skina.goldentweaks.mixin.fix.extendedae_plus;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.VerticalButtonBar;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.api.IExPatternPage;
import com.extendedae_plus.api.bridge.ExPatternProviderMenuPageBridge;
import com.glodblock.github.extendedae.client.button.ActionEPPButton;
import com.glodblock.github.extendedae.client.gui.GuiExPatternProvider;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.mixin.fix.extendedae_plus.accessor.VerticalButtonBarAccessor;
import net.fodoth.skina.goldentweaks.mixin.fix.extendedae_plus.accessor.WidgetContainerAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Map;

@Mixin(targets = "appeng.client.gui.implementations.PatternProviderScreen", priority = 800)
public abstract class PatternProviderScreenMixin<C extends PatternProviderMenu> extends AEBaseScreen<C> {

    /** extendedae_plus' own multiply/divide buttons, hidden in favour of patternbetter's "balance" bar. */
    @Unique
    private static final String[] GT$EAEP_SCALING_BUTTONS = {
            "x2Button", "divideBy2Button", "x5Button", "divideBy5Button"
    };

    @Unique
    private ActionEPPButton gt$nextPage;

    @Unique
    private ActionEPPButton gt$previousPage;

    @Unique
    private AETextField gt$pageInputField;

    @Unique
    private boolean gt$updatingPageInput;

    @Unique
    private boolean gt$loggedRightBar;

    @Unique
    private boolean gt$loggedHidden;

    protected PatternProviderScreenMixin(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gt$addPatternBetterPageControls(C menu, Inventory playerInventory, Component title,
                                                  ScreenStyle style, CallbackInfo ci) {
        if (!((Object) this instanceof IExPatternPage page)) {
            return;
        }

        gt$previousPage = new ActionEPPButton(button -> gt$changePage(page, -1), Icon.ARROW_LEFT);
        gt$nextPage = new ActionEPPButton(button -> gt$changePage(page, 1), Icon.ARROW_RIGHT);
        gt$previousPage.setHalfSize(true);
        gt$nextPage.setHalfSize(true);
        gt$previousPage.setTooltip(Tooltip.create(Component.translatable("gui.pattern_provider.prev_page")));
        gt$nextPage.setTooltip(Tooltip.create(Component.translatable("gui.pattern_provider.next_page")));
        widgets.add("prevPage", gt$previousPage);
        widgets.add("nextPage", gt$nextPage);

        gt$pageInputField = widgets.addTextField("numberInputField");
        gt$pageInputField.setResponder(value -> gt$setPageFromInput(page, value));
        gt$pageInputField.setValue("1");
    }

    /**
     * Positions patternbetter's right-hand button bar ({@code rightBar}, the "balance"/multiply strip) so its first
     * button sits 2px below the upgrade sidebar.
     */
    @Inject(method = "updateBeforeRender", at = @At("HEAD"))
    private void gt$moveRightBarBelowUpgrades(CallbackInfo ci) {
        Map<String, ICompositeWidget> compositeWidgets =
                ((WidgetContainerAccessor) (Object) widgets).gt$getCompositeWidgets();
        var rightBar = compositeWidgets.get("rightBar");
        var upgrades = compositeWidgets.get("upgrades");
        if (!(rightBar instanceof VerticalButtonBar buttonBar) || upgrades == null) {
            if (!gt$loggedRightBar) {
                gt$loggedRightBar = true;
                GoldenTweaks.LOGGER.info("[GT/btn] rightBar={} upgrades={} ids={}",
                        rightBar, upgrades, compositeWidgets.keySet());
            }
            return;
        }

        Point position = ((VerticalButtonBarAccessor) buttonBar).gt$getPosition();
        int y = upgrades.getBounds().getY() + upgrades.getBounds().getHeight() - 62;
        buttonBar.setPosition(new Point(position.getX(), y));
        if (!gt$loggedRightBar) {
            gt$loggedRightBar = true;
            GoldenTweaks.LOGGER.info("[GT/btn] rightBar -> y={} (upgrades bottom={}) x={}",
                    y, upgrades.getBounds().getY() + upgrades.getBounds().getHeight(), position.getX());
        }
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void gt$hideExtendedAePlusScalingButtons(CallbackInfo ci) {
        if (!((Object) this instanceof GuiExPatternProvider)) {
            return;
        }
        StringBuilder found = new StringBuilder();
        for (String name : GT$EAEP_SCALING_BUTTONS) {
            AbstractWidget widget = gt$findWidget(name);
            found.append(name).append('=').append(widget != null).append(' ');
            if (widget != null) {
                widget.visible = false;
            }
        }
        if (!gt$loggedHidden) {
            gt$loggedHidden = true;
            GoldenTweaks.LOGGER.info("[GT/btn] hide extendedae_plus scaling buttons: {}", found);
        }
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void gt$updatePageControls(CallbackInfo ci) {
        if (!((Object) this instanceof GuiExPatternProvider)) {
            return;
        }
        int pageCount = gt$getAvailablePageCount();
        if (gt$previousPage != null && gt$nextPage != null) {
            gt$previousPage.active = pageCount > 1;
            gt$nextPage.active = pageCount > 1;
            gt$previousPage.visible = true;
            gt$nextPage.visible = true;
        }
        if (gt$pageInputField != null) {
            gt$pageInputField.visible = true;
        }
    }

    @Unique
    private AbstractWidget gt$findWidget(String name) {
        for (Class<?> type = getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                Object value = field.get(this);
                return value instanceof AbstractWidget widget ? widget : null;
            } catch (NoSuchFieldException ignored) {
                // keep walking up
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    @Unique
    private int gt$getAvailablePageCount() {
        return menu instanceof ExPatternProviderMenuPageBridge menuPage
                ? Math.max(1, menuPage.eap$getAvailablePageCount())
                : 1;
    }

    @Unique
    private void gt$changePage(IExPatternPage page, int delta) {
        int pageCount = gt$getAvailablePageCount();
        int targetPage = Math.floorMod(page.eap$getCurrentPage() + delta, pageCount);
        page.eap$setCurrentPage(targetPage);
        gt$setPageInputValue(Integer.toString(targetPage + 1));
    }

    @Unique
    private void gt$setPageFromInput(IExPatternPage page, String value) {
        if (gt$updatingPageInput || value.isEmpty() || !value.chars().allMatch(Character::isDigit)) {
            return;
        }

        try {
            int targetPage = Math.max(1, Math.min(Integer.parseInt(value), gt$getAvailablePageCount()));
            if (!value.equals(Integer.toString(targetPage))) {
                gt$setPageInputValue(Integer.toString(targetPage));
            }
            page.eap$setCurrentPage(targetPage - 1);
        } catch (NumberFormatException ignored) {
        }
    }

    @Unique
    private void gt$setPageInputValue(String value) {
        gt$updatingPageInput = true;
        try {
            gt$pageInputField.setValue(value);
        } finally {
            gt$updatingPageInput = false;
        }
    }
}
