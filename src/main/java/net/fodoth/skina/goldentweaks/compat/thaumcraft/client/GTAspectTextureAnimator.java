package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Replaces the {@code SimpleTexture} of every animated aspect icon (one whose
 * {@code <image>.png.mcmeta} declares an {@code animation} block) with a
 * {@link GTAnimatedAspectTexture} that plays the animation.
 * <p>
 * Aspects are discovered via {@link Aspect#ordered()}: custom aspects are
 * registered when the integrated/dedicated server starts, so the client keeps
 * polling until the list is non-empty. The aspect's {@code image} resource
 * location lives under {@code assets}, which the client resource manager can
 * read. A plain {@code TextureManager} registration under that location is
 * enough — all existing draw paths ({@code AspectGuiRenderer}, tooltips,
 * research table) resolve the icon through the manager.
 * <p>
 * Registered on the game event bus by {@code GoldenTweaks} (client setup),
 * following the mod's convention of explicit {@code NeoForge.EVENT_BUS.register}.
 */
@OnlyIn(Dist.CLIENT)
public final class GTAspectTextureAnimator {

    private static final List<GTAnimatedAspectTexture> ANIMATED = new ArrayList<>();
    private static boolean initialized = false;

    private GTAspectTextureAnimator() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // Poll until the server has registered the (custom) aspects.
        if (!initialized && !Aspect.ordered().isEmpty()) {
            initialize();
            initialized = true;
        }
        tickAll();
    }

    /** TextureManager is cleared on every resource reload, so re-register when joining a world. */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ANIMATED.clear();
        if (!Aspect.ordered().isEmpty()) {
            initialize();
        }
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
            ResourceLocation image = aspect.getImage();
            if (image == null || !hasAnimationBlock(manager, image)) {
                continue;
            }
            registerAnimated(minecraft, manager, image);
        }
    }

    private static void registerAnimated(Minecraft minecraft, ResourceManager manager, ResourceLocation image) {
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
            GTAnimatedAspectTexture texture = new GTAnimatedAspectTexture(source);
            minecraft.getTextureManager().register(image, texture);
            ANIMATED.add(texture);
            GoldenTweaks.LOGGER.info("Registered animated aspect texture '{}'.", image);
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register animated aspect texture '{}': {}", image, e.getMessage());
        }
    }

    private static boolean hasAnimationBlock(ResourceManager manager, ResourceLocation image) {
        Optional<Resource> mcmeta = manager.getResource(image.withSuffix(".mcmeta"));
        if (mcmeta.isEmpty()) {
            return false;
        }
        try (InputStream stream = mcmeta.get().open()) {
            JsonObject meta = JsonParser
                    .parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            return meta.has("animation");
        } catch (Exception e) {
            return false;
        }
    }
}
