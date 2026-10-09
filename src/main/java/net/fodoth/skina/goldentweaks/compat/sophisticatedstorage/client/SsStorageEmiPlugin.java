package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.client;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.renderer.Rect2i;
import net.neoforged.fml.ModList;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeSettingsTabControl;

/**
 * 把装置上的精致存储界面（{@link SsStorageScreen}）登记给 EMI 的避让区。
 *
 * <p>SS 自带的 EMI 插件只按它自己的 StorageScreen/LimitedBarrelScreen/设置屏幕类型登记避让，
 * 覆盖不到我们这套屏幕类，导致 EMI 侧栏/配方界面会压住升级栏、设置页签、排序按钮。
 * 这里用 SS 使用的同一套控件矩形登记避让。</p>
 */
@EmiEntrypoint
public class SsStorageEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        if (!ModList.get().isLoaded("sophisticatedstorage") || !ModList.get().isLoaded("create")) {
            return;
        }
        registry.addExclusionArea(SsStorageScreen.class, (screen, consumer) -> {
            UpgradeSettingsTabControl control = screen.getUpgradeSettingsControl();
            if (control == null) {
                return;
            }
            screen.getUpgradeSlotsRectangle().ifPresent(rect -> consumer.accept(toBounds(rect)));
            control.getTabRectangles().forEach(rect -> consumer.accept(toBounds(rect)));
            screen.getSortButtonsRectangle().ifPresent(rect -> consumer.accept(toBounds(rect)));
        });
    }

    private static Bounds toBounds(Rect2i rect) {
        return new Bounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }
}
