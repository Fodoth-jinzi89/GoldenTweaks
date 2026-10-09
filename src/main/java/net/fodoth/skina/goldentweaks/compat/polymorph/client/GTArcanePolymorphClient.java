package net.fodoth.skina.goldentweaks.compat.polymorph.client;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 给奥术工作台与神秘能源「奥术合成终端」注册 Polymorph 的选择控件。
 *
 * <p>Polymorph 默认只给结果槽使用 {@code ResultContainer} 的界面自动挂控件，这两个界面的
 * 结果槽都是自定义容器，因此必须显式注册。具体界面的引用分别隔离在
 * {@link GTWorkbenchPolymorphClient} / {@link GTTerminalPolymorphClient} 里，只有对应模组
 * 存在时才会加载，避免可选依赖缺失时的类加载错误。</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTArcanePolymorphClient {

    private GTArcanePolymorphClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (!ModList.get().isLoaded("polymorph")) {
            return;
        }
        PolymorphWidgets widgets = PolymorphWidgets.getInstance();
        if (widgets == null) {
            return;
        }
        if (ModList.get().isLoaded("thaumcraft")) {
            GTWorkbenchPolymorphClient.register(widgets);
        }
        if (ModList.get().isLoaded("thaumicenergistics")) {
            GTTerminalPolymorphClient.register(widgets);
        }
    }
}
