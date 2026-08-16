package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSON-driven entity aspect registration for Thaumcraft.
 * <p>
 * JSON files go in {@code data/<namespace>/recipe/thaumcraft/entity_aspects/}.
 * <p>
 * Supports an optional list of NBT conditions; when present, the aspects only
 * apply to entities whose saved data matches every condition. Number values are
 * compared numerically, everything else by string equality. Last-registered wins.
 *
 * <pre>{@code
 * {
 *   "type": "goldentweaks:entity_aspect",
 *   "entity": { "id": "minecraft:cow" },
 *   "aspects": { "bestia": 4, "victus": 2 }
 * }
 * }</pre>
 *
 * <pre>{@code
 * {
 *   "type": "goldentweaks:entity_aspect",
 *   "entity": { "id": "minecraft:sheep" },
 *   "nbts": [
 *     { "name": "Color", "value": 1 }
 *   ],
 *   "aspects": { "bestia": 4, "vinculum": 2 }
 * }
 * }</pre>
 */
public final class GTEntityAspectEntry {

    private static final String TYPE = "goldentweaks:entity_aspect";
    private static final String ENTRY_NAME = "entity aspect";
    private static final String ENTRY_PATH = "recipe/thaumcraft/entity_aspects";

    private final ResourceLocation entityType;
    private final AspectList aspects = new AspectList();
    private final List<ThaumcraftApi.EntityTagsNBT> nbts = new ArrayList<>();

    private GTEntityAspectEntry(ResourceLocation entityType) {
        this.entityType = entityType;
    }

    private void aspect(Aspect aspect, int amount) {
        if (aspect != null && amount > 0) {
            aspects.add(aspect, amount);
        }
    }

    boolean register() {
        if (aspects.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Failed to register entity aspect entry '{}': no aspects defined.", entityType);
            return false;
        }

        try {
            ThaumcraftApi.registerEntityTag(entityType, aspects, nbts.toArray(ThaumcraftApi.EntityTagsNBT[]::new));
            return true;
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register entity aspect entry '{}': {}", entityType, e.getMessage());
            return false;
        }
    }

    // ---- JSON parsing ----------------------------------------------------

    static GTEntityAspectEntry fromJson(JsonObject json) {
        if (json == null || !ThaumcraftRecipeUtil.isType(json, TYPE)) {
            return null;
        }

        JsonObject entityObject = ThaumcraftRecipeUtil.getRequiredObject(json, "entity");
        if (entityObject == null) {
            GoldenTweaks.LOGGER.warn("Thaumcraft entity aspect entry missing 'entity' field.");
            return null;
        }

        ResourceLocation entityType = parseEntityType(entityObject);
        if (entityType == null) {
            return null;
        }

        GTEntityAspectEntry entry = new GTEntityAspectEntry(entityType);

        if (!readNbts(json, entry)) {
            return null;
        }

        JsonObject aspectObject = ThaumcraftRecipeUtil.getRequiredObject(json, "aspects");
        if (aspectObject == null) {
            return null;
        }

        for (Map.Entry<String, JsonElement> e : aspectObject.entrySet()) {
            if (!e.getValue().isJsonPrimitive() || !e.getValue().getAsJsonPrimitive().isNumber()) {
                GoldenTweaks.LOGGER.warn(
                        "Invalid Thaumcraft entity aspect entry '{}': aspect '{}' amount must be a number.",
                        entityType,
                        e.getKey()
                );
                return null;
            }

            int amount = e.getValue().getAsInt();
            if (amount <= 0) {
                GoldenTweaks.LOGGER.warn(
                        "Invalid Thaumcraft entity aspect entry '{}': aspect '{}' amount must be positive.",
                        entityType,
                        e.getKey()
                );
                return null;
            }

            Aspect aspect = Aspect.get(e.getKey());
            if (aspect == null) {
                GoldenTweaks.LOGGER.warn(
                        "Invalid Thaumcraft entity aspect entry '{}': unknown aspect '{}'.",
                        entityType,
                        e.getKey()
                );
                return null;
            }

            entry.aspect(aspect, amount);
        }

        if (entry.aspects.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft entity aspect entry '{}': aspects object is empty.", entityType);
            return null;
        }

        return entry;
    }

    private static ResourceLocation parseEntityType(JsonObject entityObject) {
        String entityId = ThaumcraftRecipeUtil.getRequiredString(entityObject, "id");
        if (entityId == null) {
            return null;
        }

        ResourceLocation id = ResourceLocation.parse(entityId);
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft entity aspect entry: unknown entity type '{}'.", entityId);
            return null;
        }

        return id;
    }

    private static boolean readNbts(JsonObject json, GTEntityAspectEntry entry) {
        JsonElement nbtsElement = json.get("nbts");
        if (nbtsElement == null) {
            return true;
        }

        if (!nbtsElement.isJsonArray()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft entity aspect entry '{}': 'nbts' must be an array.", entry.entityType);
            return false;
        }

        JsonArray nbtsArray = nbtsElement.getAsJsonArray();
        for (int i = 0; i < nbtsArray.size(); i++) {
            JsonElement element = nbtsArray.get(i);
            if (!element.isJsonObject()) {
                GoldenTweaks.LOGGER.warn(
                        "Invalid Thaumcraft entity aspect entry '{}': nbts[{}] must be an object.",
                        entry.entityType,
                        i
                );
                return false;
            }

            JsonObject nbtObject = element.getAsJsonObject();
            String name = ThaumcraftRecipeUtil.getRequiredString(nbtObject, "name");
            if (name == null || name.isBlank()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft entity aspect entry '{}': nbts[{}] has no valid name.", entry.entityType, i);
                return false;
            }

            Object value = parseNbtValue(nbtObject.get("value"), entry.entityType, i);
            if (value == null) {
                return false;
            }

            entry.nbts.add(new ThaumcraftApi.EntityTagsNBT(name, value));
        }

        return true;
    }

    private static Object parseNbtValue(JsonElement element, ResourceLocation entityType, int index) {
        if (element == null || !element.isJsonPrimitive()) {
            GoldenTweaks.LOGGER.warn(
                    "Invalid Thaumcraft entity aspect entry '{}': nbts[{}] has no valid value.",
                    entityType,
                    index
            );
            return null;
        }

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return primitive.getAsNumber();
        }
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isString()) {
            return primitive.getAsString();
        }

        GoldenTweaks.LOGGER.warn(
                "Invalid Thaumcraft entity aspect entry '{}': nbts[{}] value type is unsupported.",
                entityType,
                index
        );
        return null;
    }

    // ---- Resource loading -------------------------------------------------

    static void load(ResourceManager resourceManager) {
        ThaumcraftRecipeUtil.load(resourceManager, ENTRY_PATH, ENTRY_NAME, (json, location) -> {
            GTEntityAspectEntry entry = fromJson(json);
            return entry != null && entry.register();
        });
    }
}
