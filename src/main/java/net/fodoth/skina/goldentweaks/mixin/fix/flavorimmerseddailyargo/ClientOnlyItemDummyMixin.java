package net.fodoth.skina.goldentweaks.mixin.fix.flavorimmerseddailyargo;

import org.spongepowered.asm.mixin.Mixin;

/**
 * 空 mixin：唯一作用是让 mixin 插件对 flavor_immersed_daily_argo 的两个「开界面」物品调用 preApply，
 * 由 {@code ClientOnlyItemASM} 在专用服务器上删掉 {@code if (level.isClientSide) { ... }} 里的客户端代码，
 * 免得注册物品时加载 {@code Screen} 崩服（详见该类注释）。
 */
@Mixin(targets = {
        "com.flavor_immersed_daily_argo.item.FishingHandbookItem",
        "com.flavor_immersed_daily_argo.item.RangerClipboardItem"
}, remap = false)
public abstract class ClientOnlyItemDummyMixin {
}
