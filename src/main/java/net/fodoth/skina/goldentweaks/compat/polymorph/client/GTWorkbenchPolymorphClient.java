package net.fodoth.skina.goldentweaks.compat.polymorph.client;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import thaumcraft.common.menu.ArcaneWorkbenchMenu;

/**
 * 奥术工作台的选择控件注册；单独成类以隔离 {@code thaumcraft} 的引用。
 */
final class GTWorkbenchPolymorphClient {

    private GTWorkbenchPolymorphClient() {
    }

    static void register(PolymorphWidgets widgets) {
        widgets.registerWidget(screen -> {
            // 工作台结果槽固定是第 0 格；控件据产物槽定位，落在产物格上方。
            if (screen.getMenu() instanceof ArcaneWorkbenchMenu menu && !menu.slots.isEmpty()) {
                return new GTArcaneRecipesWidget(screen, menu.getSlot(0));
            }
            return null;
        });
    }
}
