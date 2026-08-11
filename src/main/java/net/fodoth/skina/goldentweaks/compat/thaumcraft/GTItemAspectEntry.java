package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.util.Map;

/**
 * JSON-driven item aspect registration for Thaumcraft.
 * <p>
 * JSON files go in {@code data/<namespace>/recipe/thaumcraft/aspects/}.
 * <p>
 * Supports individual items and item tags. Aspects can be freely redefined —
 * last-registered wins.
 *
 * <pre>{@code
 * {
 *   "type": "goldentweaks:item_aspect",
 *   "item": { "id": "minecraft:diamond" },
 *   "aspects": { "vitreus": 10, "lucrum": 15 }
 * }
 * }</pre>
 *
 * <pre>{@code
 * {
 *   "type": "goldentweaks:item_aspect",
 *   "item": { "type": "tag", "id": "minecraft:logs" },
 *   "aspects": { "arbor": 5 }
 * }
 * }</pre>
 */
public final class GTItemAspectEntry {

    private static final String TYPE = "goldentweaks:item_aspect";
    private static final String ENTRY_NAME = "item aspect";
    private static final String ENTRY_PATH = "recipe/thaumcraft/aspects";

    private final Object target;
    private final AspectList aspects = new AspectList();

    private GTItemAspectEntry(Object target) {
        this.target = target;
    }

    private void aspect(Aspect aspect, int amount) {
        if (aspect != null && amount > 0) {
            aspects.add(aspect, amount);
        }
    }

    boolean register() {
        if (aspects.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Failed to register item aspect entry: no aspects defined.");
            return false;
        }

        try {
            if (target instanceof ItemStack stack) {
                ThaumcraftApi.registerObjectTag(stack, aspects);
            } else if (target instanceof TagKey<?> tag) {
                @SuppressWarnings("unchecked")
                TagKey<Item> itemTag = (TagKey<Item>) tag;
                ThaumcraftApi.registerObjectTag(itemTag, aspects);
            } else {
                return false;
            }
            return true;
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register item aspect entry: {}", e.getMessage());
            return false;
        }
    }

    // ---- JSON parsing ----------------------------------------------------

    static GTItemAspectEntry fromJson(JsonObject json) {
        if (json == null || !ThaumcraftRecipeUtil.isType(json, TYPE)) {
            return null;
        }

        JsonObject itemObject = ThaumcraftRecipeUtil.getRequiredObject(json, "item");
        if (itemObject == null) {
            GoldenTweaks.LOGGER.warn("Thaumcraft item aspect entry missing 'item' field.");
            return null;
        }

        Object target = parseTarget(itemObject);
        if (target == null) {
            return null;
        }

        GTItemAspectEntry entry = new GTItemAspectEntry(target);

        JsonObject aspectObject = ThaumcraftRecipeUtil.getRequiredObject(json, "aspects");
        if (aspectObject == null) {
            return null;
        }

        for (Map.Entry<String, JsonElement> e : aspectObject.entrySet()) {
            if (!e.getValue().isJsonPrimitive() || !e.getValue().getAsJsonPrimitive().isNumber()) {
                GoldenTweaks.LOGGER.warn(
                        "Invalid Thaumcraft item aspect entry: aspect '{}' amount must be a number.",
                        e.getKey()
                );
                return null;
            }

            int amount = e.getValue().getAsInt();
            if (amount <= 0) {
                GoldenTweaks.LOGGER.warn(
                        "Invalid Thaumcraft item aspect entry: aspect '{}' amount must be positive.",
                        e.getKey()
                );
                return null;
            }

            Aspect aspect = Aspect.get(e.getKey());
            if (aspect == null) {
                GoldenTweaks.LOGGER.warn(
                        "Invalid Thaumcraft item aspect entry: unknown aspect '{}'.",
                        e.getKey()
                );
                return null;
            }

            entry.aspect(aspect, amount);
        }

        if (entry.aspects.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft item aspect entry: aspects object is empty.");
            return null;
        }

        return entry;
    }

    private static Object parseTarget(JsonObject itemObject) {
        if (ThaumcraftRecipeUtil.isType(itemObject, "tag")) {
            String tagId = ThaumcraftRecipeUtil.getRequiredString(itemObject, "id");
            if (tagId == null) {
                return null;
            }

            try {
                return TagKey.create(Registries.ITEM, ResourceLocation.parse(tagId));
            } catch (Exception e) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft item aspect entry: invalid tag '{}'.", tagId);
                return null;
            }
        }

        // Default: direct item reference
        String itemId = ThaumcraftRecipeUtil.getRequiredString(itemObject, "id");
        if (itemId == null) {
            return null;
        }

        ResourceLocation id = ResourceLocation.parse(itemId);
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft item aspect entry: unknown item '{}'.", itemId);
            return null;
        }

        return new ItemStack(item);
    }

    // ---- Resource loading -------------------------------------------------

    static void load(ResourceManager resourceManager) {
        ThaumcraftRecipeUtil.load(resourceManager, ENTRY_PATH, ENTRY_NAME, (json, location) -> {
            GTItemAspectEntry entry = fromJson(json);
            return entry != null && entry.register();
        });
    }
}
