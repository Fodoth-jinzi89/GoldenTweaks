package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

/**
 * Opens the connected-texture shifts for business once the block atlas is complete.
 *
 * <p>A shift entry resolves its textures by id against that atlas, and a lookup made before stitching
 * yields {@code null} — which the entry then keeps for the rest of the session. Building here means
 * every casing's own wall is ready before the first frame, and a resource reload replaces the entries
 * instead of keeping the ones built from the previous pack.</p>
 *
 * <p>Plates whose casing pairs are only discovered after this point are built on first use;
 * {@link GTPortCasingBehaviour} notifies the stitch callback itself in that case.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTPortCasingAtlas {

    private GTPortCasingAtlas() {
    }

    @SubscribeEvent
    public static void onStitched(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location() != TextureAtlas.LOCATION_BLOCKS) {
            return;
        }
        GTPortCasingBehaviour.rebuildShifts();
    }
}
