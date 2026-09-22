package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftRecipeLoader;
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
        List<PackResources> packs = Minecraft.getInstance().getResourceManager().listPacks().toList();
        // Not closed: these pack instances belong to the client's own resource manager.
        GTThaumcraftRecipeLoader.loadClientData(new MultiPackResourceManager(PackType.SERVER_DATA, packs));
    }
}
