package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.gui.ThaumonomiconData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Replaces the {@code SimpleTexture} of every animated icon texture (one whose
 * {@code <image>.png.mcmeta} declares an {@code animation} block) with a
 * {@link GTAnimatedAspectTexture} that plays the animation. Handles both
 * Thaumcraft aspect icons and Thaumonomicon category icons.
 * <p>
 * Aspect icons are discovered via {@link Aspect#ordered()}: custom aspects are
 * registered when the integrated/dedicated server starts, so the client keeps
 * polling until the list is non-empty. Category icons come from the synced
 * research index ({@link ThaumonomiconData#categories()}), which is only
 * available after the client joins a world.
 * <p>
 * A plain {@code TextureManager} registration under the icon's resource
 * location is enough — all existing draw paths (research table, Thaumonomicon
 * tabs, tooltips) resolve the icon through the manager.
 * <p>
 * Registered on the game event bus by {@code GoldenTweaks} (client setup),
 * following the mod's convention of explicit {@code NeoForge.EVENT_BUS.register}.
 */
@OnlyIn(Dist.CLIENT)
public final class GTAnimatedIconAnimator {

    private static final List<GTAnimatedAspectTexture> ANIMATED = new ArrayList<>();
    private static boolean initialized = false;

    private GTAnimatedIconAnimator() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // Poll until the server has registered the (custom) aspects and the research index has been synced.
        if (!initialized && !Aspect.ordered().isEmpty() && !ThaumonomiconData.categories().isEmpty()) {
            initialize();
            initialized = true;
        }
        tickAll();
    }

    /** TextureManager is cleared on every resource reload, so re-register when joining a world. */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ANIMATED.clear();
        initialized = false;
    }

    private static void tickAll() {
        for (GTAnimatedAspectTexture texture : ANIMATED) {
            texture.tick();
        }
    }

    private static void initialize() {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceManager manager = minecraft.getResourceManager();
        for (Aspect aspect : Aspect.ordered()) {
            registerIfAnimated(minecraft, manager, aspect.getImage());
        }
        for (ThaumonomiconData.Category category : ThaumonomiconData.categories()) {
            registerIfAnimated(minecraft, manager, parseLocation(category.icon()));
        }
    }

    private static void registerIfAnimated(Minecraft minecraft, ResourceManager manager, ResourceLocation image) {
        if (image == null) {
            return;
        }
        int frameDelay = readFrameDelay(manager, image);
        if (frameDelay == 0) {
            return;
        }
        registerAnimated(minecraft, manager, image, frameDelay);
    }

    private static ResourceLocation parseLocation(String value) {
        try {
            return ResourceLocation.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static void registerAnimated(Minecraft minecraft, ResourceManager manager, ResourceLocation image, int frameDelay) {
        Optional<Resource> png = manager.getResource(image);
        if (png.isEmpty()) {
            return;
        }
        try (InputStream stream = png.get().open()) {
            NativeImage source = NativeImage.read(stream);
            if (source.getHeight() <= source.getWidth() || source.getHeight() % source.getWidth() != 0) {
                source.close();
                return;
            }
            GTAnimatedAspectTexture texture = new GTAnimatedAspectTexture(source, frameDelay);
            minecraft.getTextureManager().register(image, texture);
            ANIMATED.add(texture);
            GoldenTweaks.LOGGER.info("Registered animated texture '{}'.", image);
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register animated texture '{}': {}", image, e.getMessage());
        }
    }

    /** Returns the mcmeta {@code animation.frametime} in ticks, or 0 if the texture has no animation block. */
    private static int readFrameDelay(ResourceManager manager, ResourceLocation image) {
        Optional<Resource> mcmeta = manager.getResource(image.withSuffix(".mcmeta"));
        if (mcmeta.isEmpty()) {
            return 0;
        }
        try (InputStream stream = mcmeta.get().open()) {
            JsonObject meta = JsonParser
                    .parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            JsonObject animation = meta.getAsJsonObject("animation");
            if (animation == null) {
                return 0;
            }
            JsonElement frametime = animation.get("frametime");
            if (frametime != null && frametime.isJsonPrimitive() && frametime.getAsJsonPrimitive().isNumber()) {
                return Math.max(1, frametime.getAsInt());
            }
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
