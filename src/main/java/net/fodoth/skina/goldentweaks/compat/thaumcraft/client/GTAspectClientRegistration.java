package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
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
 * Registers the datapack-defined Thaumcraft aspects on the client.
 *
 * <p>The JSON aspect definitions live in {@code data/<namespace>/thaumcraft/aspects/}, which
 * the server loads while starting up ({@code GTThaumcraftRecipeLoader}). The client-side
 * resource manager only exposes {@code assets/}, so on a dedicated server the client would
 * keep a shorter {@link thaumcraft.api.aspects.Aspect} registry than the server. The research
 * table menu syncs aspect pools, bonuses and discoveries by registry index, so the mismatch
 * empties both aspect palettes and makes aspect clicks encode the wrong aspect.</p>
 *
 * <p>Reading the same files through a {@link PackType#SERVER_DATA} view of the client's own
 * packs mirrors the server loader exactly — same {@code TreeMap} listing order, same condition
 * checks — and registering on login puts the aspects after every mod has been constructed,
 * which is likewise when the server registers them.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class GTAspectClientRegistration {

    private GTAspectClientRegistration() {
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        List<PackResources> packs = Minecraft.getInstance().getResourceManager().listPacks().toList();
        // Not closed: these pack instances belong to the client's own resource manager.
        GTAspectEntry.load(new MultiPackResourceManager(PackType.SERVER_DATA, packs));
    }
}
