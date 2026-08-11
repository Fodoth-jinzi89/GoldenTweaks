package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import thaumcraft.api.aspects.Aspect;

import java.io.Reader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

final class ThaumcraftRecipeUtil {

    private static final String CONDITIONS = "neoforge:conditions";

    private ThaumcraftRecipeUtil() {
    }

    static void load(
            ResourceManager resourceManager,
            String recipePath,
            String recipeName,
            BiFunction<JsonObject, ResourceLocation, Boolean> loader
    ) {
        if (resourceManager == null) {
            return;
        }

        Map<ResourceLocation, Resource> resources;
        try {
            resources = resourceManager.listResources(recipePath, location -> location.getPath().endsWith(".json"));
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to scan Thaumcraft {} recipes: {}", recipeName, e.getMessage());
            return;
        }

        int loaded = 0;
        int skipped = 0;

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                if (!element.isJsonObject()) {
                    GoldenTweaks.LOGGER.warn("Skipped Thaumcraft {} recipe '{}': root is not an object.", recipeName, location);
                    skipped++;
                    continue;
                }

                JsonObject json = element.getAsJsonObject();
                if (!checkConditions(json, location, recipeName) || !checkRequiredMods(json, location, recipeName)) {
                    skipped++;
                    continue;
                }

                if (loader.apply(json, location)) {
                    loaded++;
                    GoldenTweaks.LOGGER.debug("Loaded Thaumcraft {} recipe '{}'.", recipeName, location);
                } else {
                    skipped++;
                    GoldenTweaks.LOGGER.warn("Skipped invalid Thaumcraft {} recipe '{}'.", recipeName, location);
                }
            } catch (Exception e) {
                GoldenTweaks.LOGGER.warn("Failed to load Thaumcraft {} recipe '{}': {}", recipeName, location, e.getMessage());
                skipped++;
            }
        }

        GoldenTweaks.LOGGER.info("Thaumcraft {} recipes: {} loaded, {} skipped.", recipeName, loaded, skipped);
    }

    static boolean isType(JsonObject json, String type) {
        JsonElement element = json.get("type");
        return element != null
                && element.isJsonPrimitive()
                && element.getAsJsonPrimitive().isString()
                && type.equals(element.getAsString());
    }

    static ItemStack parseStack(JsonObject json, String path) {
        try {
            JsonElement idElement = json.get("id");
            if (!isString(idElement)) {
                GoldenTweaks.LOGGER.warn("Invalid item stack at '{}': missing string field 'id'.", path);
                return ItemStack.EMPTY;
            }

            String itemId = idElement.getAsString();
            ResourceLocation id = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (item == null) {
                GoldenTweaks.LOGGER.warn("Unknown item '{}' at '{}'.", itemId, path);
                return ItemStack.EMPTY;
            }

            return ItemStack.CODEC
                    .parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> GoldenTweaks.LOGGER.warn("Failed to decode item stack at '{}': {}", path, error))
                    .orElse(ItemStack.EMPTY);
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to parse item stack at '{}': {}", path, e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    static Object parseIngredient(JsonElement element, ResourceLocation location, String recipeName, String fieldName) {
        if (!element.isJsonObject()) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft {} recipe '{}': {} must be an object.", recipeName, location, fieldName);
            return null;
        }

        JsonObject json = element.getAsJsonObject();
        JsonElement typeElement = json.get("type");
        if (!isString(typeElement)) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft {} recipe '{}': {} has no valid type.", recipeName, location, fieldName);
            return null;
        }

        String type = typeElement.getAsString();
        return switch (type) {
            case "item" -> {
                ItemStack stack = parseStack(json, fieldName);
                if (stack.isEmpty()) {
                    GoldenTweaks.LOGGER.warn("Skipped Thaumcraft {} recipe '{}': invalid item {}.", recipeName, location, fieldName);
                    yield null;
                }
                yield stack;
            }
            case "tag" -> parseTag(json, location, recipeName, fieldName);
            case "string" -> parseStringValue(json, location, recipeName, fieldName);
            default -> {
                GoldenTweaks.LOGGER.warn(
                        "Skipped Thaumcraft {} recipe '{}': unsupported {} type '{}'.",
                        recipeName,
                        location,
                        fieldName,
                        type
                );
                yield null;
            }
        };
    }

    static boolean readAspects(JsonObject json, String research, String recipeName, AbstractGTThaumcraftRecipe<?> recipe) {
        JsonObject aspectObject = getRequiredObject(json, "aspects");
        if (aspectObject == null || aspectObject.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft {} recipe '{}': aspects must not be empty.", recipeName, research);
            return false;
        }

        for (Map.Entry<String, JsonElement> entry : aspectObject.entrySet()) {
            JsonElement value = entry.getValue();
            if (!isNumber(value)) {
                GoldenTweaks.LOGGER.warn(
                        "Invalid Thaumcraft {} recipe '{}': aspect '{}' amount must be a number.",
                        recipeName,
                        research,
                        entry.getKey()
                );
                return false;
            }

            int amount = value.getAsInt();
            if (amount <= 0) {
                GoldenTweaks.LOGGER.warn(
                        "Invalid Thaumcraft {} recipe '{}': aspect '{}' amount must be positive.",
                        recipeName,
                        research,
                        entry.getKey()
                );
                return false;
            }

            Aspect aspect = Aspect.get(entry.getKey());
            if (aspect == null) {
                GoldenTweaks.LOGGER.warn(
                        "Invalid Thaumcraft {} recipe '{}': unknown aspect '{}'.",
                        recipeName,
                        research,
                        entry.getKey()
                );
                return false;
            }

            recipe.aspect(aspect, amount);
        }

        return true;
    }

    static String getRequiredString(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (!isString(element)) {
            GoldenTweaks.LOGGER.warn("Missing or invalid string field '{}'.", key);
            return null;
        }
        return element.getAsString();
    }

    static int getRequiredInt(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (!isNumber(element)) {
            GoldenTweaks.LOGGER.warn("Missing or invalid number field '{}'.", key);
            return -1;
        }
        return element.getAsInt();
    }

    static JsonObject getRequiredObject(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonObject()) {
            GoldenTweaks.LOGGER.warn("Missing or invalid object field '{}'.", key);
            return null;
        }
        return element.getAsJsonObject();
    }

    static JsonArray getRequiredArray(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonArray()) {
            GoldenTweaks.LOGGER.warn("Missing or invalid array field '{}'.", key);
            return null;
        }
        return element.getAsJsonArray();
    }

    private static Object parseTag(JsonObject json, ResourceLocation location, String recipeName, String fieldName) {
        JsonElement idElement = json.get("id");
        if (!isString(idElement)) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft {} recipe '{}': {} tag has no valid id.", recipeName, location, fieldName);
            return null;
        }

        try {
            return TagKey.create(Registries.ITEM, ResourceLocation.parse(idElement.getAsString()));
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn(
                    "Skipped Thaumcraft {} recipe '{}': invalid {} tag '{}'.",
                    recipeName,
                    location,
                    fieldName,
                    idElement.getAsString()
            );
            return null;
        }
    }

    private static String parseStringValue(JsonObject json, ResourceLocation location, String recipeName, String fieldName) {
        JsonElement valueElement = json.get("value");
        if (!isString(valueElement)) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft {} recipe '{}': {} string has no valid value.", recipeName, location, fieldName);
            return null;
        }

        String value = valueElement.getAsString();
        if (value.isBlank()) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft {} recipe '{}': {} string value is blank.", recipeName, location, fieldName);
            return null;
        }
        return value;
    }

    private static boolean checkConditions(JsonObject json, ResourceLocation location, String recipeName) {
        JsonElement element = json.get(CONDITIONS);
        if (element == null) {
            return true;
        }

        if (!element.isJsonArray()) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft {} recipe '{}': '{}' must be an array.", recipeName, location, CONDITIONS);
            return false;
        }

        return ICondition.LIST_CODEC
                .parse(JsonOps.INSTANCE, element)
                .resultOrPartial(error ->
                        GoldenTweaks.LOGGER.warn("Failed to decode conditions for Thaumcraft {} recipe '{}': {}", recipeName, location, error)
                )
                .map(conditions -> {
                    for (ICondition condition : conditions) {
                        if (!condition.test(ICondition.IContext.EMPTY)) {
                            GoldenTweaks.LOGGER.debug("Skipped Thaumcraft {} recipe '{}': condition not met.", recipeName, location);
                            return false;
                        }
                    }
                    return true;
                })
                .orElse(false);
    }

    private static boolean checkRequiredMods(JsonObject json, ResourceLocation location, String recipeName) {
        Set<String> requiredMods = new HashSet<>();

        collectIdMod(json.get("result"), requiredMods);
        collectIdMod(json.get("catalyst"), requiredMods);
        collectIngredientMods(json.get("key"), requiredMods);
        collectIngredientMods(json.get("ingredients"), requiredMods);

        for (String modId : requiredMods) {
            if (!ModList.get().isLoaded(modId)) {
                GoldenTweaks.LOGGER.debug(
                        "Skipped Thaumcraft {} recipe '{}': required mod '{}' is not loaded.",
                        recipeName,
                        location,
                        modId
                );
                return false;
            }
        }

        return true;
    }

    private static void collectIngredientMods(JsonElement element, Set<String> requiredMods) {
        if (element == null) {
            return;
        }

        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                collectIngredientMods(child, requiredMods);
            }
            return;
        }

        if (!element.isJsonObject()) {
            return;
        }

        JsonObject json = element.getAsJsonObject();
        JsonElement typeElement = json.get("type");
        if (!isString(typeElement)) {
            collectIdMod(json, requiredMods);
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                collectIngredientMods(entry.getValue(), requiredMods);
            }
            return;
        }

        switch (typeElement.getAsString()) {
            case "item", "tag" -> collectIdMod(json, requiredMods);
            case "items" -> collectIngredientMods(json.get("items"), requiredMods);
            default -> {
            }
        }

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (!"id".equals(entry.getKey()) && !"value".equals(entry.getKey())) {
                collectIngredientMods(entry.getValue(), requiredMods);
            }
        }
    }

    private static void collectIdMod(JsonElement element, Set<String> requiredMods) {
        if (element == null || !element.isJsonObject()) {
            return;
        }

        JsonElement idElement = element.getAsJsonObject().get("id");
        if (!isString(idElement)) {
            return;
        }

        try {
            ResourceLocation id = ResourceLocation.parse(idElement.getAsString());
            requiredMods.add(id.getNamespace());
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Invalid resource location '{}' while checking recipe dependencies.", idElement.getAsString());
        }
    }

    private static boolean isString(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }

    private static boolean isNumber(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }
}
