package net.fodoth.skina.goldentweaks.compat.oei;

import cc.sighs.oelib.data.DataManager;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * OEI 统一规则的自检与自愈。
 *
 * <p>OELib 在数据包 reload 期间校验规则时物品标签尚未绑定，会把全部 {@code #c:xxx} 规则标记为
 * {@code deferred}，而 deferred 数据既不会重试也不会被 {@code getAllData()} 读到，导致服务端统一失效
 * （重复矿物锭、配方补全异常等）。{@code UtilsMixin} + {@link OeiRawTagResolver} 已从原始
 * tags/item 回退修复该时序问题；这里再做一次启动自检，并在必要时触发一次资源重载兜底。</p>
 */
public final class OreUnificationGuard {

    private OreUnificationGuard() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!GoldenTweaksCommonConfig.isOreUnificationGuardEnabled()) {
            return;
        }
        MinecraftServer server = event.getServer();
        // 延后一 tick，确保 OEI 自己的 ServerStartedEvent（重建替换缓存）已经执行完毕
        server.execute(() -> inspect(server));
    }

    private static void inspect(MinecraftServer server) {
        try {
            int rules = DataManager.getDataList(Replacements.class).size();
            if (rules == 0) {
                GoldenTweaks.LOGGER.debug("[GT-OEI] 未发现任何统一规则，跳过自检");
                return;
            }
            if (ItemReplacementCache.hasAnyMappings()) {
                GoldenTweaks.LOGGER.info("[GT-OEI] 物品统一规则已生效（{} 条）", rules);
                return;
            }

            GoldenTweaks.LOGGER.error(
                    "[GT-OEI] 物品统一规则未生效：已读取 {} 条规则，但 OEI 运行时映射为空。"
                            + "原因通常是 OELib 校验规则时物品标签尚未绑定（规则被 deferred 且不会重试）。",
                    rules);

            if (!GoldenTweaksCommonConfig.isOreUnificationSelfHealEnabled()) {
                return;
            }
            GoldenTweaks.LOGGER.warn("[GT-OEI] 触发一次资源重载以自愈：重载后标签已就绪，规则会正常加载");
            server.reloadResources(server.getPackRepository().getSelectedIds());
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.warn("[GT-OEI] 物品统一自检失败", t);
        }
    }
}
