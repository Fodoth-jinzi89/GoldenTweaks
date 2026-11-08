package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftRecipeLoader;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.ObjectTagsCache;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.List;

/**
 * Loads the datapack-defined Thaumcraft content on the client, mirroring
 * {@code GTThaumcraftRecipeLoader}.
 *
 * <p>The JSON definitions live under {@code data/<namespace>/thaumcraft/} and
 * {@code data/<namespace>/recipe/thaumcraft/}, which the server loads while starting up. The
 * client-side resource manager only exposes {@code assets/}, so without this a dedicated server
 * client keeps an empty aspect registry, an empty {@code ThaumcraftApi} object-tag registry and an
 * empty research index — custom aspects, item aspects and the thaumonomicon's custom categories
 * and research entries would all be missing.</p>
 *
 * <p>Reading the same files through a {@link PackType#SERVER_DATA} view of the client's own
 * packs mirrors the server loader exactly (same {@code TreeMap} listing order, same condition
 * checks). Registering on login is what the server does as well — after every mod has been
 * constructed — which keeps both registries in the same order.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class GTThaumcraftClientData {

    private GTThaumcraftClientData() {
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            // 集成服务端已经在 ServerAboutToStart/ServerStarted 里注册过配方与要素，且客户端与它
            // 共享同一份静态注册表。这里再跑一遍 loadClientData 只会把同一批配方重复追加进
            // ThaumcraftApi 的静态配方表，导致 JEI/EMI 中每条配方出现两次。仅清掉启动期预索引
            // 用回退数据算出的要素缓存即可。
            ObjectTagsCache.clear();
            return;
        }
        List<PackResources> packs = Minecraft.getInstance().getResourceManager().listPacks().toList();
        // Not closed: these pack instances belong to the client's own resource manager.
        GTThaumcraftRecipeLoader.loadClientData(new MultiPackResourceManager(PackType.SERVER_DATA, packs));
    }
}
