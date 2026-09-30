package net.fodoth.skina.goldentweaks.compat.mekanicalcreate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Access-ordered, entry-bounded map used to replace the plain {@code HashMap} that
 * {@code SimulationRecipeResolver}'s candidate-catalog lambda stores per {@code RecipeManager}.
 *
 * <p>That inner map has no size limit, so on a big Create+Mekanism pack it accumulates tens of thousands of
 * candidate catalogs (a heap dump showed ~95 MB held there). Bounding it keeps the memoisation useful while
 * capping the retained heap.</p>
 */
public final class BoundedCandidateMap<K, V> extends LinkedHashMap<K, V> {

    private static final int MAX_ENTRIES = 128;
    private static final long serialVersionUID = 1L;

    public BoundedCandidateMap() {
        super(16, 0.75f, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > MAX_ENTRIES;
    }
}
