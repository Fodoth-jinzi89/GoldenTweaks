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

    @Unique
    private static final String[] GT$EAEP_SCALING_BUTTON_FIELDS = {
            "x2Button", "divideBy2Button", "x5Button", "divideBy5Button"
    };

    @Unique
    private Field[] gt$eaepScalingButtonFields;

    @Unique
    private ActionEPPButton gt$nextPage;

    @Unique
    private ActionEPPButton gt$previousPage;

    @Unique
    private AETextField gt$pageInputField;

    @Unique
    private boolean gt$updatingPageInput;

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

    @Inject(method = "updateBeforeRender", at = @At("HEAD"))
    private void gt$movePatternScalingButtonsBelowUpgrades(CallbackInfo ci) {
        Map<String, ICompositeWidget> compositeWidgets =
                ((WidgetContainerAccessor) (Object) widgets).gt$getCompositeWidgets();
        var rightBar = compositeWidgets.get("rightBar");
        if (rightBar instanceof VerticalButtonBar buttonBar) {
            Point position = ((VerticalButtonBarAccessor) buttonBar).gt$getPosition();
            var upgrades = compositeWidgets.get("upgrades");
            int y = upgrades != null
                    ? upgrades.getBounds().getY() + upgrades.getBounds().getHeight() - 31
                    : position.getY();
            buttonBar.setPosition(new Point(position.getX(), y));
        }
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void gt$hideExtendedAEPlusScalingButtons(CallbackInfo ci) {
        if ((Object) this instanceof GuiExPatternProvider) {
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

            try {
                if (gt$eaepScalingButtonFields == null) {
                    Field[] fields = new Field[GT$EAEP_SCALING_BUTTON_FIELDS.length];
                    for (int i = 0; i < GT$EAEP_SCALING_BUTTON_FIELDS.length; i++) {
                        Field field = getClass().getDeclaredField(GT$EAEP_SCALING_BUTTON_FIELDS[i]);
                        field.setAccessible(true);
                        fields[i] = field;
                    }
                    gt$eaepScalingButtonFields = fields;
                }
                for (Field field : gt$eaepScalingButtonFields) {
                    if (field.get(this) instanceof AbstractWidget button) {
                        button.visible = false;
                    }
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
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
