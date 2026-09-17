package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.emi.EmiSearchDebounce;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * EMI 搜索节流的<b>兜底时钟</b>：每个客户端 tick 叫一次 {@link EmiSearchDebounce#tick()}。
 *
 * <p>不依赖任何 UI/渲染路径（之前把时钟挂在 {@code EditBox#renderWidget} 上根本不会触发，
 * 挂在 {@code EmiSearchWidget#renderWidget} 上也不保险），所以只要客户端在跑 tick，
 * "攒下的查询"就一定会被补跑。</p>
 *
 * <p>EMI 没装时直接返回：{@link EmiSearchDebounce} 引用 EMI 的类，必须先用 ModList 挡一下。</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class EmiSearchTickHandler {

    private EmiSearchTickHandler() {
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {

        if (!ModList.get().isLoaded("emi")) {
            return;
        }

        EmiSearchDebounce.tick();
    }
}
