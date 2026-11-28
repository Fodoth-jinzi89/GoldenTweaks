package net.fodoth.skina.goldentweaks.content.multiblock.common;

import com.google.gson.JsonElement;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.pattern.AMiBlockPattern;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.pattern.AMiBlockPatternReloadListener;

import java.io.BufferedReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

/**
 * Loads our bottom-up structure files and converts them into AMI patterns.
 *
 * <p>Files live in {@code data/<namespace>/multiblock/structures/<name>.json} and use the
 * player-facing layout documented on {@link GTMultiblockPattern}; they are deliberately kept out
 * of AMI's own {@code multiblock_patterns} directory, whose {@code [depth][height][width]} arrays
 * cannot be read while editing. {@code GTMultiblockPatternsMixin} pushes the result into AMI's
 * registry once AMI has finished its own reload.</p>
 *
 * <p>The pattern object is built by {@code AMiBlockPatternReloadListener.parsePattern} rather
 * than by calling a constructor: {@code AMiBlockPattern} changed its constructor signature between
 * AMI builds, so only this entry point keeps us from binding to one particular build.</p>
 */
public final class GTMultiblockStructures {

    static final String DIRECTORY = "multiblock/structures";
    private static final String SUFFIX = ".json";

    /**
     * The parsed layouts, kept alongside AMI's own pattern objects.
     *
     * <p>AMI's {@code AMiBlockPattern} only holds opaque predicates, so once a structure is handed
     * over there is no way to ask which cells are casing and which are ports. The projector needs
     * exactly that, so the source layout is retained here, keyed by the same structure id.</p>
     */
    private static final Map<ResourceLocation, GTMultiblockPattern> LAYOUTS = new ConcurrentHashMap<>();

    private GTMultiblockStructures() {
    }

    /** The layout with the given id, or {@code null} if no structure by that name was loaded. */
    @Nullable
    public static GTMultiblockPattern layout(ResourceLocation id) {
        return LAYOUTS.get(id);
    }

    /**
     * @return patterns keyed by their structure id, e.g. {@code goldentweaks:chemical_reactor}
     */
    public static Map<ResourceLocation, AMiBlockPattern> load(ResourceManager manager) {
        Map<ResourceLocation, AMiBlockPattern> loaded = new LinkedHashMap<>();

        Map<ResourceLocation, Resource> resources =
                manager.listResources(DIRECTORY, path -> path.getPath().endsWith(SUFFIX));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation file = entry.getKey();
            ResourceLocation id = structureId(file);
            if (id == null) {
                continue;
            }

            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonElement root = JsonParser.parseReader(reader);
                GTMultiblockPattern structure = GTMultiblockPattern.parse(root);

                AMiBlockPattern pattern =
                        AMiBlockPatternReloadListener.parsePattern(structure.toAmiJson());

                loaded.put(id, pattern);
                LAYOUTS.put(id, structure);
                GoldenTweaks.LOGGER.info("[GT] Multiblock structure {}: {}", id, structure.describe());
            } catch (Exception error) {
                GoldenTweaks.LOGGER.error("[GT] Failed to load multiblock structure {}", id, error);
            }
        }

        return loaded;
    }

    /**
     * {@code goldentweaks:multiblock/structures/chemical_reactor.json} becomes
     * {@code goldentweaks:chemical_reactor}; anything nested deeper is ignored.
     */
    private static ResourceLocation structureId(ResourceLocation file) {
        String path = file.getPath();
        String prefix = DIRECTORY + "/";
        if (!path.startsWith(prefix) || !path.endsWith(SUFFIX)) {
            return null;
        }
        String name = path.substring(prefix.length(), path.length() - SUFFIX.length());
        if (name.isEmpty() || name.contains("/")) {
            GoldenTweaks.LOGGER.warn("[GT] Ignoring nested multiblock structure file {}", file);
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(file.getNamespace(), name);
    }
}
