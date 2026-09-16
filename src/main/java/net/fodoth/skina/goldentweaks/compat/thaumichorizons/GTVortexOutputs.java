package net.fodoth.skina.goldentweaks.compat.thaumichorizons;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers item entities that a rift has just produced so the rift's hungry field skips them.
 * <p>
 * Entries are kept a little longer than the vanilla item despawn time, which keeps the map small
 * while guaranteeing that a freshly crafted output is never devoured again.
 */
public final class GTVortexOutputs {

    /** Vanilla item entities despawn after 6000 ticks (5 minutes); stay a bit above that. */
    private static final long LIFETIME_MS = 360_000L;

    private static final Map<UUID, Long> FRESH = new ConcurrentHashMap<>();

    private GTVortexOutputs() {
    }

    public static void mark(UUID uuid) {
        long now = System.currentTimeMillis();
        FRESH.entrySet().removeIf(entry -> now - entry.getValue() > LIFETIME_MS);
        FRESH.put(uuid, now);
    }

    public static boolean isFresh(UUID uuid) {
        Long stamp = FRESH.get(uuid);
        if (stamp == null) {
            return false;
        }

        if (System.currentTimeMillis() - stamp > LIFETIME_MS) {
            FRESH.remove(uuid);
            return false;
        }

        return true;
    }
}
