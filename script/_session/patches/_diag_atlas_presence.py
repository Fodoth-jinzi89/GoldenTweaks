"""Report whether the CT sheets reached the block atlas at all.

Every attempt so far assumed the sheets were stitched and the mapping was wrong. That assumption has
not been checked, and a missing sheet produces exactly the symptom being chased — the black-and-magenta
checker — because `CTSpriteShiftEntry.set` silently resolves an unknown texture to the missing sprite.

This asks the atlas directly, once, at client setup.
"""
import io

path = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingDiagnostics.java"

content = '''package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

/**
 * Reports whether the connected-texture sheets are in the block atlas.
 *
 * <p>A CT shift entry resolves its two textures by id against that atlas. If either is missing the
 * lookup yields the missing sprite and every shifted quad draws as the black-and-magenta checker —
 * which looks like a broken model and is actually a missing texture. This tells the two apart.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTPortCasingDiagnostics {

    private static final ResourceLocation SHEET =
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "block/shell_pp");
    private static final ResourceLocation CT =
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "block/shell_pp_ctm");

    private GTPortCasingDiagnostics() {
    }

    @SubscribeEvent
    public static void onStitched(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location() != TextureAtlas.LOCATION_BLOCKS) {
            return;
        }
        report(event.getAtlas(), SHEET);
        report(event.getAtlas(), CT);
    }

    private static void report(TextureAtlas atlas, ResourceLocation id) {
        TextureAtlasSprite sprite = atlas.getSprite(id);
        // The missing sprite reports its own name, which is how absence is recognised.
        boolean missing = sprite == null || sprite.contents().name().equals(
                net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation());
        GoldenTweaks.LOGGER.info("[GT/diag] atlas {} -> {} ({})",
                id, sprite == null ? "null" : sprite.contents().name(),
                missing ? "MISSING" : "present");
    }
}
'''

io.open(path, "w", encoding="utf-8", newline="\n").write(content)
print("atlas diagnostic written")
