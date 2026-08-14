package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import thaumcraft.api.aspects.Aspect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * JSON-driven Thaumcraft aspect registration.
 * <p>
 * JSON files go in {@code data/<namespace>/thaumcraft/aspects/}.
 * <p>
 * Primal aspect (no components):
 * <pre>{@code
 * {
 *   "type": "goldentweaks:aspect",
 *   "tag": "LUCIDUS",
 *   "color": "0xffffff"
 * }
 * }</pre>
 *
 * Compound aspect (two components):
 * <pre>{@code
 * {
 *   "type": "goldentweaks:aspect",
 *   "tag": "LUMEN",
 *   "color": "#ffff7e",
 *   "components": ["AER", "LUCIDUS"],
 *   "image": "goldentweaks:textures/aspects/lumen.png",
 *   "blend": 1
 * }
 * }</pre>
 *
 * <p>Color may be a decimal integer or a hex string ({@code "0xffaa00"}, {@code "#ffaa00"} or {@code "ffaa00"}).
 * If omitted, {@code image} defaults to {@code thaumcraft:textures/aspects/<tag>.png} and {@code blend} to {@code 1}.
 * <p>Set {@code "tint": false} when the icon texture is already colored and must be drawn
 * without the aspect-color tint (defaults to {@code true}).
 * <p>Set {@code "cosmic": true} to render the aspect icon in GUIs through the cosmic
 * proxy item model (renderblender cosmic shader) instead of the plain texture (defaults to {@code false}).
 * Aspect display names are localized through {@code tc.aspect.<tag>} in language files.
 */
public final class GTAspectEntry {

    private static final String TYPE = "goldentweaks:aspect";
    private static final String ENTRY_NAME = "aspect";
    private static final String ENTRY_PATH = "thaumcraft/aspects";

    /** Tags whose icons are drawn without the aspect-color tint (see {@code "tint": false}). */
    private static final Set<String> UNTINTED_TAGS = new HashSet<>();

    /** Tags whose icons are drawn with the cosmic proxy item model (see {@code "cosmic": true}). */
    private static final Set<String> COSMIC_TAGS = new HashSet<>();

    private final String tag;
    private final int color;
    private final int blend;
    private final boolean tint;
    private final boolean cosmic;
    private final ResourceLocation image;
    private final List<String> components = new ArrayList<>();

    private GTAspectEntry(String tag, int color, int blend, boolean tint, boolean cosmic, ResourceLocation image) {
        this.tag = tag;
        this.color = color;
        this.blend = blend;
        this.tint = tint;
        this.cosmic = cosmic;
        this.image = image;
    }

    /**
     * @return {@code true} if the given aspect tag was declared with {@code "tint": false}
     *         in its aspect JSON, so its icon must be rendered without the aspect color.
     */
    public static boolean isUntinted(String tag) {
        return UNTINTED_TAGS.contains(tag);
    }

    /**
     * @return {@code true} if the given aspect tag was declared with {@code "cosmic": true}
     *         in its aspect JSON, so its GUI icon must be rendered via the cosmic proxy item.
     */
    public static boolean isCosmic(String tag) {
        return COSMIC_TAGS.contains(tag);
    }

    /**
     * Registers the aspect if not already present. Compound aspects only succeed once
     * every referenced component exists, so callers may retry.
     *
     * @return {@code true} if the aspect is registered (or was already registered).
     */
    boolean register() {
        if (!tint) {
            UNTINTED_TAGS.add(tag);
        }
        if (cosmic) {
            COSMIC_TAGS.add(tag);
        }
        if (Aspect.get(tag) != null) {
            GoldenTweaks.LOGGER.debug("Thaumcraft aspect '{}' already registered, skipping.", tag);
            return true;
        }

        Aspect[] componentAspects = new Aspect[components.size()];
        for (int i = 0; i < components.size(); i++) {
            Aspect component = Aspect.get(components.get(i));
            if (component == null) {
                return false;
            }
            componentAspects[i] = component;
        }

        try {
            new Aspect(tag, color, componentAspects, image, blend);
            GoldenTweaks.LOGGER.debug("Registered Thaumcraft aspect '{}'.", tag);
            return true;
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft aspect '{}': {}", tag, e.getMessage());
            return false;
        }
    }

    // ---- JSON parsing ----------------------------------------------------

    static GTAspectEntry fromJson(JsonObject json) {
        if (json == null || !ThaumcraftRecipeUtil.isType(json, TYPE)) {
            return null;
        }

        String tag = ThaumcraftRecipeUtil.getRequiredString(json, "tag");
        if (tag == null || tag.isBlank()) {
            return null;
        }

        Integer color = parseColor(json.get("color"));
        if (color == null) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft aspect entry '{}': missing or invalid 'color'.", tag);
            return null;
        }

        int blend = json.has("blend") ? ThaumcraftRecipeUtil.getRequiredInt(json, "blend") : 1;
        if (blend < 0) {
            blend = 1;
        }

        boolean tint = true;
        JsonElement tintElement = json.get("tint");
        if (tintElement != null) {
            if (!tintElement.isJsonPrimitive() || !tintElement.getAsJsonPrimitive().isBoolean()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft aspect entry '{}': 'tint' must be a boolean.", tag);
                return null;
            }
            tint = tintElement.getAsBoolean();
        }

        boolean cosmic = false;
        JsonElement cosmicElement = json.get("cosmic");
        if (cosmicElement != null) {
            if (!cosmicElement.isJsonPrimitive() || !cosmicElement.getAsJsonPrimitive().isBoolean()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft aspect entry '{}': 'cosmic' must be a boolean.", tag);
                return null;
            }
            cosmic = cosmicElement.getAsBoolean();
        }

        ResourceLocation image = null;
        JsonElement imageElement = json.get("image");
        if (imageElement != null) {
            if (!imageElement.isJsonPrimitive() || !imageElement.getAsJsonPrimitive().isString()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft aspect entry '{}': 'image' must be a string.", tag);
                return null;
            }
            try {
                image = ResourceLocation.parse(imageElement.getAsString());
            } catch (Exception e) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft aspect entry '{}': invalid 'image'.", tag);
                return null;
            }
        }

        GTAspectEntry entry = new GTAspectEntry(tag, color, blend, tint, cosmic, image);

        JsonElement componentsElement = json.get("components");
        if (componentsElement != null) {
            if (!componentsElement.isJsonArray()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft aspect entry '{}': 'components' must be an array.", tag);
                return null;
            }
            for (JsonElement element : componentsElement.getAsJsonArray()) {
                if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                    GoldenTweaks.LOGGER.warn(
                            "Invalid Thaumcraft aspect entry '{}': 'components' entries must be strings.",
                            tag
                    );
                    return null;
                }
                entry.components.add(element.getAsString());
            }
        }

        return entry;
    }

    private static Integer parseColor(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return primitive.getAsInt();
        }
        if (primitive.isString()) {
            String value = primitive.getAsString().trim();
            if (value.startsWith("0x") || value.startsWith("0X")) {
                value = value.substring(2);
            } else if (value.startsWith("#")) {
                value = value.substring(1);
            }
            try {
                return (int) Long.parseLong(value, 16);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // ---- Resource loading -------------------------------------------------

    static void load(ResourceManager resourceManager) {
        List<GTAspectEntry> entries = new ArrayList<>();
        ThaumcraftRecipeUtil.load(resourceManager, ENTRY_PATH, ENTRY_NAME, (json, location) -> {
            GTAspectEntry entry = fromJson(json);
            if (entry != null) {
                entries.add(entry);
                return true;
            }
            return false;
        });
        registerAll(entries);
    }

    private static void registerAll(List<GTAspectEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        int registered = 0;

        // Primals first so compounds can reference them.
        List<GTAspectEntry> pending = new ArrayList<>();
        for (GTAspectEntry entry : entries) {
            if (entry.components.isEmpty()) {
                if (entry.register()) {
                    registered++;
                }
            } else {
                pending.add(entry);
            }
        }

        // Compounds, retrying until no progress (handles chains of custom aspects).
        boolean progressed = true;
        while (progressed && !pending.isEmpty()) {
            progressed = false;
            Iterator<GTAspectEntry> it = pending.iterator();
            while (it.hasNext()) {
                GTAspectEntry entry = it.next();
                if (entry.register()) {
                    it.remove();
                    registered++;
                    progressed = true;
                }
            }
        }

        for (GTAspectEntry entry : pending) {
            GoldenTweaks.LOGGER.warn(
                    "Failed to register Thaumcraft aspect '{}': unresolved components.",
                    entry.tag
            );
        }

        GoldenTweaks.LOGGER.info("Thaumcraft aspects: {} registered.", registered);
    }
}
