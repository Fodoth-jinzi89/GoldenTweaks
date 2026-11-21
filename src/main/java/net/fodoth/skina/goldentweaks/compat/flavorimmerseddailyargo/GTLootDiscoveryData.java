package net.fodoth.skina.goldentweaks.compat.flavorimmerseddailyargo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class GTLootDiscoveryData {

    private GTLootDiscoveryData() {
    }

    public static <K> Map<K, JsonElement> compact(@NotNull Map<K, JsonElement> tables) {
        Map<K, JsonElement> compact = new HashMap<>(tables.size());
        tables.forEach((id, table) -> compact.put(id, project(table)));
        return compact;
    }

    private static JsonElement project(JsonElement element) {
        if (element == null || element.isJsonNull() || element.isJsonPrimitive()) {
            return element;
        }
        if (element.isJsonArray()) {
            JsonArray array = new JsonArray(element.getAsJsonArray().size());
            for (JsonElement child : element.getAsJsonArray()) {
                array.add(project(child));
            }
            return array;
        }
        JsonObject source = element.getAsJsonObject();
        JsonObject result = new JsonObject();
        // Exactly the fields read by LootProductEntries; preserve nesting for its depth limits.
        for (String field : new String[] { "type", "name", "value", "pools", "entries", "children" }) {
            if (source.has(field)) {
                result.add(field, project(source.get(field)));
            }
        }
        return result;
    }
}
