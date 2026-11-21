package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * The one projection the local player is currently watching.
 *
 * <p>Purely client state: it is toggled by right-clicking a host with the projector and holds
 * nothing but the host position and dimension. The shape itself is re-read every frame from the
 * host blockstate, so re-aiming or mirroring the host by left-clicking updates the projection
 * without any bookkeeping here.</p>
 */
public final class GTMultiblockProjection {

    @Nullable
    private static BlockPos hostPos;

    @Nullable
    private static ResourceKey<Level> dimension;

    /** Wall-clock deadline, so the countdown is unaffected by frame rate. */
    private static long expiresAt;

    private GTMultiblockProjection() {
    }

    /**
     * Lifetime of a projection, from the client config.
     *
     * <p>Read fresh on every use rather than cached, so editing the setting takes effect on the
     * next projection without restarting the game. A value of zero means "never expire".</p>
     */
    private static long lifetimeMs() {
        try {
            return GoldenTweaksClientConfig.MULTIBLOCK_PROJECTION_DURATION_MS.get();
        } catch (RuntimeException error) {
            // Config not loaded yet (very early call); fall back to the default so nothing breaks.
            return 10_000L;
        }
    }

    /** Shows the host's projection, or hides it when the same host is already projected. */
    public static void toggle(BlockPos pos) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (pos.equals(hostPos) && level.dimension().equals(dimension)) {
            clear();
        } else {
            show(pos);
        }
    }

    /**
     * Turns the projection on, without ever turning it off.
     *
     * <p>Used after a partial build: the player should see what could not be placed, and a toggle
     * would hide it again when the same host was already being projected. Showing it again also
     * restarts the countdown.</p>
     */
    public static void show(BlockPos pos) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        hostPos = pos.immutable();
        dimension = level.dimension();
        long lifetime = lifetimeMs();
        // Zero (or negative, if the config were ever loosened) means "stay until dismissed".
        expiresAt = lifetime > 0L ? System.currentTimeMillis() + lifetime : Long.MAX_VALUE;
    }

    /**
     * Clears the projection once its lifetime has passed.
     *
     * <p>Called from the render loop, which is the only place that knows the projection is still
     * being displayed. Time is taken from the wall clock rather than counted in frames so the
     * timeout is the same regardless of frame rate.</p>
     */
    public static void tick() {
        if (hostPos != null && System.currentTimeMillis() >= expiresAt) {
            clear();
        }
    }

    public static void clear() {
        hostPos = null;
        dimension = null;
        expiresAt = 0L;
    }

    @Nullable
    public static BlockPos hostPos() {
        return hostPos;
    }

    @Nullable
    public static ResourceKey<Level> dimension() {
        return dimension;
    }
}
