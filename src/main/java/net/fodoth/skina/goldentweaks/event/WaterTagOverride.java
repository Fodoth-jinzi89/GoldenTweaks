package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * createnuclear 与 flavor_immersed_daily_argo 都会往 minecraft:water 流体标签里合并自己的流体，
 * 导致 EMI 在显示使用该标签的配方时多出液氮和水车中的水。
 * 1.21.1 的标签不支持移除条目，这里注册一个最高优先级的数据包，用 replace 重写为原版内容。
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class WaterTagOverride {

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "gt_tag_overrides"),
                PackType.SERVER_DATA,
                Component.literal("GoldenTweaks Water Tag Override"),
                PackSource.FEATURE,
                true,
                Pack.Position.TOP
        );
    }

    private WaterTagOverride() {
    }
}
