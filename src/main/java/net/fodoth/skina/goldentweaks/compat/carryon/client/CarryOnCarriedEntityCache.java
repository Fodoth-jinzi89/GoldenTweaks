package net.fodoth.skina.goldentweaks.compat.carryon.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import tschipp.carryon.common.carry.CarryOnData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keeps the client-side stand-in entity Carry On builds from NBT stable for as long as the same creature is
 * held.
 *
 * <p>{@code CarryOnData#getEntity} runs {@code EntityType.create} on every call and Carry On calls it from
 * its renderer - so the entity that gets drawn is a brand new object every frame, with nothing carried over:
 * a fresh {@code Entity#getId}, a fresh {@code Entity#random}, {@code tickCount} back to 0, and every piece
 * of per-entity animation state (the behaviour state machine and client tickers of NeoGuanNiao's birds, the
 * walk animation, whatever model/texture mods such as EMF, ETF and Fresh Animations keep per entity) reset.
 * That is what makes a carried creature twitch, change size between frames and only settle once the mods
 * involved stop reacting to the entity, and it is also why a per-game-tick animation clock such as
 * {@link net.fodoth.skina.goldentweaks.compat.thaumichorizons.client.GTCreaturePreview#prepareStored} cannot
 * work at all.</p>
 *
 * <p>NeoGuanNiao has the same problem for its bird cage and solves it exactly like this (its
 * {@code CageBirdPreview} recreates the stand-in only when the stored NBT changes), so this class mirrors that
 * behaviour for the entity Carry On holds.</p>
 *
 * <p>{@code CarryOnData#getEntity} is the only path onto the carried entity, so both the first-person render
 * ({@code drawFirstPersonEntity}) and the third-person one ({@code drawThirdPerson}) go through here and see
 * the same instance - a spectator and the carrying player therefore agree on the creature's pose, animation
 * phase and variant.</p>
 */
public final class CarryOnCarriedEntityCache {

    /** Distinct carried creatures kept alive; the map is access ordered, so the held ones stay. */
    private static final int MAX_ENTRIES = 32;

    private static final Map<CompoundTag, Entity> CACHE = new LinkedHashMap<>(MAX_ENTRIES, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<CompoundTag, Entity> eldest) {
            return this.size() > MAX_ENTRIES;
        }
    };

    private CarryOnCarriedEntityCache() {
    }

    /**
     * Stand-in for {@code CarryOnData#getEntity}: the previously created entity is handed back while the
     * stored NBT (keyed structurally, so a re-sent carry data packet for the same creature still hits) and the
     * level are unchanged.
     */
    public static synchronized Entity entity(CarryOnData data, Level level) {
        CompoundTag nbt = data.getContentNbt();

        if (nbt == null) {
            return data.getEntity(level);
        }

        Entity cached = CACHE.get(nbt);

        if (cached != null && cached.level() == level) {
            return cached;
        }

        Entity created = data.getEntity(level);
        CACHE.put(nbt.copy(), created);
        return created;
    }
}
