package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which recipe each machine has pinned, as last sent by the server.
 *
 * <p>Pure data, like {@link GTReactionProducts}: the tooltip needs an id and nothing else, so keeping
 * a small cache here avoids reaching into the block entity from client code that may not have one.</p>
 */
public final class GTRecipeLocks {

    private static final Map<Long, ResourceLocation> LOCKS = new ConcurrentHashMap<>();

    private GTRecipeLocks() {
    }

    public static void put(long pos, ResourceLocation recipe) {
        LOCKS.put(pos, recipe);
    }

    public static void remove(long pos) {
        LOCKS.remove(pos);
    }

    /** The pinned recipe at {@code pos}, or {@code null} when the machine is not locked. */
    @Nullable
    public static ResourceLocation get(long pos) {
        return LOCKS.get(pos);
    }

    /** Drops cached ids for a machine that no longer exists. */
    public static void clear(long pos) {
        LOCKS.remove(pos);
    }
}
