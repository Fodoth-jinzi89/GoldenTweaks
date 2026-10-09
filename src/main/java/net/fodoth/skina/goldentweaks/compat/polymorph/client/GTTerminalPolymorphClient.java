package net.fodoth.skina.goldentweaks.compat.polymorph.client;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import net.minecraft.world.inventory.Slot;
import thaumicenergistics.common.container.ContainerArcaneCraftingTerminal;
import thaumicenergistics.common.container.slot.ArcaneCraftingResultSlot;

/**
 * 神秘能源「奥术合成终端」的选择控件注册。
 *
 * <p>单独成类是为了把 {@code thaumicenergistics} 的引用隔离出来：只有安装了该模组时
 * （见 {@link GTArcanePolymorphClient}）才会加载本类，未安装时不会触发类加载错误。</p>
 *
 * <p>AE2 会按界面样式重新摆放槽位，结果槽的下标并不稳定；这里直接按
 * {@link ArcaneCraftingResultSlot} 类型查找产物槽，控件才能落在正确位置。</p>
 */
final class GTTerminalPolymorphClient {

    private GTTerminalPolymorphClient() {
    }

    static void register(PolymorphWidgets widgets) {
        widgets.registerWidget(screen -> {
            if (screen.getMenu() instanceof ContainerArcaneCraftingTerminal menu) {
                for (Slot slot : menu.slots) {
                    if (slot instanceof ArcaneCraftingResultSlot result) {
                        return new GTArcaneRecipesWidget(screen, result);
                    }
                }
            }
            return null;
        });
    }
}
