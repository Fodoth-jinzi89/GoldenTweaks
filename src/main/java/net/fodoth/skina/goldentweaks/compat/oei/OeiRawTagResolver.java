package net.fodoth.skina.goldentweaks.compat.oei;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OEI 在数据包 reload 期间校验/展开物品标签，而原版要到所有 reload listener 跑完后才把
 * tags/item 绑定进注册表，导致 {@code #c:xxx} 类规则全部被判定为 deferred 并永久搁置。
 *
 * <p>这里直接从数据包原始文件读取 tags/item 并自行展开，作为注册表未就绪时的回退数据源。
 */
public final class OeiRawTagResolver {

    private static final String TAG_DIR = "tags/item";
    private static final String TAG_DIR_PREFIX = TAG_DIR + "/";
    private static final String JSON_SUFFIX = ".json";

    private static volatile ResourceManager capturedManager;
    private static volatile Map<ResourceLocation, List<String>> tagEntries;
    private static volatile Map<ResourceLocation, Set<ResourceLocation>> expanded = new ConcurrentHashMap<>();
    private static volatile boolean fallbackLogged;

    private OeiRawTagResolver() {
    }

    public static void capture(ResourceManager resourceManager) {
        if (resourceManager == null || resourceManager == capturedManager) {
            return;
        }
        synchronized (OeiRawTagResolver.class) {
            if (resourceManager == capturedManager) {
                return;
            }
            capturedManager = resourceManager;
            tagEntries = null;
            expanded = new ConcurrentHashMap<>();
            fallbackLogged = false;
        }
    }

    public static boolean hasTag(ResourceLocation tagId) {
        return entries().containsKey(tagId);
    }

    public static Collection<Item> itemsOf(ResourceLocation tagId) {
        Set<ResourceLocation> itemIds = expand(tagId, new HashSet<>());
        if (itemIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<ResourceLocation> sorted = new ArrayList<>(itemIds);
        Collections.sort(sorted);

        Set<Item> items = new LinkedHashSet<>();
        for (ResourceLocation itemId : sorted) {
            if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private static Map<ResourceLocation, List<String>> entries() {
        Map<ResourceLocation, List<String>> cached = tagEntries;
        if (cached != null) {
            return cached;
        }
        synchronized (OeiRawTagResolver.class) {
            if (tagEntries != null) {
                return tagEntries;
            }
            Map<ResourceLocation, List<String>> built = build(capturedManager);
            tagEntries = built;
            return built;
        }
    }

    private static Map<ResourceLocation, List<String>> build(ResourceManager resourceManager) {
        Map<ResourceLocation, List<String>> built = new HashMap<>();
        if (resourceManager == null) {
            return built;
        }
        try {
            Map<ResourceLocation, List<Resource>> stacks =
                    resourceManager.listResourceStacks(TAG_DIR, rl -> rl.getPath().endsWith(JSON_SUFFIX));
            for (Map.Entry<ResourceLocation, List<Resource>> stackEntry : stacks.entrySet()) {
                ResourceLocation tagId = toTagId(stackEntry.getKey());
                if (tagId == null) {
                    continue;
                }
                List<String> values = new ArrayList<>();
                List<Resource> stack = stackEntry.getValue();
                for (int i = stack.size() - 1; i >= 0; i--) {
                    appendValues(stack.get(i), values);
                }
                built.put(tagId, values);
            }
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.warn("[GT-OEI] 读取原始 tags/item 失败，本次回退解析不可用", t);
            return new HashMap<>();
        }

        if (!built.isEmpty() && !fallbackLogged) {
            fallbackLogged = true;
            GoldenTweaks.LOGGER.info(
                    "[GT-OEI] OEI 标签校验期注册表尚未绑定 tags/item，已改用原始数据包回退解析（{} 个标签）", built.size());
        }
        return built;
    }

    private static void appendValues(Resource resource, List<String> out) {
        try (Reader reader = resource.openAsReader()) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root == null || !root.isJsonObject()) {
                return;
            }
            JsonObject object = root.getAsJsonObject();
            JsonElement replace = object.get("replace");
            if (replace != null && replace.isJsonPrimitive() && replace.getAsJsonPrimitive().isBoolean()
                    && replace.getAsBoolean()) {
                out.clear();
            }
            JsonElement values = object.get("values");
            if (values == null || !values.isJsonArray()) {
                return;
            }
            for (JsonElement element : values.getAsJsonArray()) {
                String id = readEntryId(element);
                if (id != null && !id.isBlank()) {
                    out.add(id);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static String readEntryId(JsonElement element) {
        if (element == null) {
            return null;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }
        if (element.isJsonObject()) {
            JsonElement id = element.getAsJsonObject().get("id");
            if (id != null && id.isJsonPrimitive() && id.getAsJsonPrimitive().isString()) {
                return id.getAsString();
            }
        }
        return null;
    }

    private static Set<ResourceLocation> expand(ResourceLocation tagId, Set<ResourceLocation> visiting) {
        Map<ResourceLocation, Set<ResourceLocation>> cache = expanded;
        Set<ResourceLocation> cached = cache.get(tagId);
        if (cached != null) {
            return cached;
        }
        if (!visiting.add(tagId)) {
            return Collections.emptySet();
        }

        Set<ResourceLocation> result = new LinkedHashSet<>();
        List<String> values = entries().get(tagId);
        if (values != null) {
            for (String value : values) {
                if (value.startsWith("#")) {
                    ResourceLocation nested = ResourceLocation.tryParse(value.substring(1));
                    if (nested != null) {
                        result.addAll(expand(nested, visiting));
                    }
                } else {
                    ResourceLocation itemId = ResourceLocation.tryParse(value);
                    if (itemId != null) {
                        result.add(itemId);
                    }
                }
            }
        }

        visiting.remove(tagId);
        cache.put(tagId, result);
        return result;
    }

    private static ResourceLocation toTagId(ResourceLocation file) {
        String path = file.getPath();
        if (!path.startsWith(TAG_DIR_PREFIX) || !path.endsWith(JSON_SUFFIX)) {
            return null;
        }
        String sub = path.substring(TAG_DIR_PREFIX.length(), path.length() - JSON_SUFFIX.length());
        return sub.isEmpty() ? null : ResourceLocation.tryParse(file.getNamespace() + ":" + sub);
    }
}
