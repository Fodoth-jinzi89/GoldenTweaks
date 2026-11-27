package net.fodoth.skina.goldentweaks.compat.mioicif.emi;

import com.miophas.singularity_iteration.common.processing.UuReferenceCatalog;
import com.miophas.singularity_iteration.core.runtime.uu.IndependentUuValueIndex;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.locating.IModFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The reference price table, read straight out of Singularity Iteration's jar.
 *
 * <p>EMI runs {@code register} on its own reload thread, at a point where the client's resource
 * manager does not yet expose other mods' data packs — asking it here found nothing and the page
 * came out empty. The file itself is simply inside SI's jar, so reading it from there works at any
 * point in startup and cannot be affected by reload ordering.</p>
 *
 * <p>{@link UuReferenceCatalog#read} is used rather than parsing the JSON by hand because it owns
 * the legacy {@code {id:...,Damage:...}} to modern item migration, which runs through vanilla's
 * data fixers. That migration does not actually consult the registries it is handed — the reference
 * table is all vanilla items and the fixers resolve them by id — so {@code null} is passed.</p>
 */
public final class GTUuReferencePrices {

    private static final String NAMESPACE = "mio_icif";
    private static final String PATH = "data/" + NAMESPACE + "/uu/observed_1122.json";
    /** The table quotes buckets; the replicator is measured in millibuckets. */
    private static final double MB_PER_BUCKET = 1000.0D;

    /** One row: an item and what a single one costs, in millibuckets. */
    public record Row(ItemStack stack, long millibuckets) {
    }

    private static List<Row> rows = List.of();
    private static boolean attempted;

    private GTUuReferencePrices() {
    }

    /** The priced items, richest first. Empty until {@link #load} has run and found the table. */
    public static List<Row> rows() {
        return rows;
    }

    public static void clear() {
        rows = List.of();
        attempted = false;
    }

    /** Reads the table once; a second call is a no-op. */
    public static void load() {
        if (attempted) {
            return;
        }
        attempted = true;
        rows = List.of();

        String table = readTable();
        if (table == null) {
            GoldenTweaks.LOGGER.warn("[GT] UU reference table '{}' not found in the mio_icif jar; "
                    + "its EMI page stays empty.", PATH);
            return;
        }

        try (StringReader reader = new StringReader(table)) {
            List<Row> read = new ArrayList<>();
            for (Map.Entry<IndependentUuValueIndex.Key, Double> entry
                    : UuReferenceCatalog.read(reader, registries()).prices().entrySet()) {
                double buckets = entry.getValue();
                if (!(buckets > 0.0D) || !Double.isFinite(buckets)) {
                    continue;
                }
                ItemStack stack = stackOf(entry.getKey());
                if (stack.isEmpty()) {
                    continue;
                }
                read.add(new Row(stack, millibucketsOf(buckets)));
            }
            // Richest first: the table is a reference, and the expensive rows are the ones worth
            // looking up.
            read.sort(Comparator.comparingLong(Row::millibuckets).reversed());
            rows = List.copyOf(read);
            GoldenTweaks.LOGGER.info("[GT] UU reference prices: {} readable rows.", rows.size());
        } catch (IOException | RuntimeException failure) {
            GoldenTweaks.LOGGER.warn("[GT] Could not read the UU reference table '{}'.", PATH, failure);
        }
    }

    /**
     * Registry access for the migration.
     *
     * <p>{@code ItemStack.parse} insists on a provider, but everything it has to resolve here lives
     * in the built-in registries — the reference table is all vanilla items — so the client's static
     * registries are enough and no loaded level is required. That matters because EMI runs this
     * during its reload thread, which can happen before a world is joined.</p>
     */
    private static HolderLookup.Provider registries() {
        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (level != null) {
            return level.registryAccess();
        }
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    /**
     * The table's contents, read out of SI's jar.
     *
     * <p>A mod jar is exposed as a zip file system that only exists while it is open, so the text is
     * copied out in one read rather than handing back a path that would already be dead.</p>
     */
    @Nullable
    private static String readTable() {
        IModFile modFile = ModList.get().getModFileById(NAMESPACE).getFile();
        Path archive = modFile.getFilePath();
        try {
            // A jar is a zip; a mod loaded from an exploded directory is a plain folder. Both are
            // reachable the same way once wrapped in a file system, which is what newFileSystem does
            // for the zip and where the fallback path read comes in for the directory.
            if (Files.isDirectory(archive)) {
                Path plain = archive.resolve(PATH);
                return Files.exists(plain) ? Files.readString(plain) : null;
            }
            try (FileSystem fileSystem = FileSystems.newFileSystem(archive)) {
                Path inside = fileSystem.getPath("/" + PATH);
                if (!Files.exists(inside)) {
                    return null;
                }
                return Files.readString(inside);
            }
        } catch (IOException failure) {
            GoldenTweaks.LOGGER.warn("[GT] Could not open the mio_icif jar at '{}'.", archive, failure);
            return null;
        }
    }

    /**
     * The item a price key names.
     *
     * <p>The key carries an id and a component hash, but the reference table's entries all describe
     * plain stacks, so the id alone is enough to draw them. An id that no longer exists is skipped
     * rather than shown as a missing-texture placeholder.</p>
     */
    private static ItemStack stackOf(IndependentUuValueIndex.Key key) {
        ResourceLocation id = parseId(key.itemId());
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Nullable
    private static ResourceLocation parseId(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String text = raw.indexOf(':') >= 0 ? raw : "minecraft:" + raw;
        return ResourceLocation.tryParse(text);
    }

    /** Buckets to whole millibuckets, rounded up: a fraction of a millibucket is still a cost. */
    private static long millibucketsOf(double buckets) {
        double mb = buckets * MB_PER_BUCKET;
        if (mb >= (double) Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.max(1L, (long) Math.ceil(mb));
    }
}
