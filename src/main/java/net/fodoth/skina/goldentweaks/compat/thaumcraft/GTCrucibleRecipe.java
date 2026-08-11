package net.fodoth.skina.goldentweaks.compat.thaumcraft;

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
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.io.Reader;
import java.util.*;

/**
 * 神秘时代坩埚配方管理器
 * 功能：配方定义、JSON解析、条件检查、自动加载
 * 支持：NeoForge条件系统、模组依赖检查、多种催化剂类型（物品/标签/字符串）
 */
public final class GTCrucibleRecipe {

    // ==================== 常量 ====================
    private static final String TYPE = "thaumcraft:crucible";              // 配方类型
    private static final String CONDITIONS = "neoforge:conditions";        // 条件字段
    private static final String RECIPE_PATH = "recipe/thaumcraft/crucible"; // 文件路径

    // ==================== 配方属性 ====================
    private final String research;          // 研究名称
    private final ItemStack output;         // 产出物品
    private final Object catalyst;          // 催化剂（ItemStack / TagKey / String）
    private final AspectList aspects = new AspectList(); // 源质需求

    // ==================== 构造方法 ====================
    private GTCrucibleRecipe(String research, ItemStack output, Object catalyst) {
        this.research = research;
        this.output = output.copy();
        this.catalyst = catalyst;
    }

    // ==================== 工厂方法 ====================
    public static GTCrucibleRecipe create(String research, ItemLike output, Object catalyst) {
        return create(research, new ItemStack(output), catalyst);
    }

    public static GTCrucibleRecipe create(String research, ItemStack output, Object catalyst) {
        return new GTCrucibleRecipe(research, output, catalyst);
    }

    // ==================== 链式配置 ====================
    public GTCrucibleRecipe aspect(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return this;
        aspects.add(aspect, amount);
        return this;
    }

    // ==================== 配方注册 ====================
    public void register() {
        if (output.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft crucible recipe '{}': empty output.", research);
            return;
        }

        if (catalyst == null) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft crucible recipe '{}': missing catalyst.", research);
            return;
        }

        try {
            ThaumcraftApi.addCrucibleRecipe(research, output, catalyst, aspects);
            GoldenTweaks.LOGGER.debug("Registered Thaumcraft crucible recipe '{}'.", research);
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft crucible recipe '{}': {}", research, e.getMessage());
        }
    }

    // ==================== JSON解析 ====================
    /**
     * 从JSON解析坩埚配方
     * 支持三种催化剂类型：
     *   - item: { "type": "item", "id": "minecraft:diamond", "count": 1 }
     *   - tag:  { "type": "tag", "id": "minecraft:planks" }
     *   - string: { "type": "string", "value": "ore:ingotGold" }
     */
    public static GTCrucibleRecipe fromJson(JsonObject json) {
        if (json == null) return null;

        try {
            // 验证配方类型
            JsonElement typeElement = json.get("type");
            if (typeElement == null || !typeElement.isJsonPrimitive() ||
                    !typeElement.getAsJsonPrimitive().isString() || !TYPE.equals(typeElement.getAsString())) {
                return null;
            }

            // 解析研究名称
            String research = getRequiredString(json, "research");
            if (research == null || research.isBlank()) return null;

            // 解析产出物品
            JsonObject resultObject = getRequiredObject(json, "result");
            if (resultObject == null) return null;
            ItemStack output = parseStack(resultObject, "result");
            if (output.isEmpty()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': result is empty.", research);
                return null;
            }

            // 解析催化剂
            JsonObject catalystObject = getRequiredObject(json, "catalyst");
            if (catalystObject == null) return null;
            Object catalyst = parseCatalyst(catalystObject, research);
            if (catalyst == null) return null;

            GTCrucibleRecipe recipe = create(research, output, catalyst);

            // 解析源质需求
            JsonObject aspectObject = getRequiredObject(json, "aspects");
            if (aspectObject == null || aspectObject.isEmpty()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': aspects must not be empty.", research);
                return null;
            }

            for (Map.Entry<String, JsonElement> entry : aspectObject.entrySet()) {
                JsonElement value = entry.getValue();
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': aspect '{}' amount must be a number.", research, entry.getKey());
                    return null;
                }

                int amount = value.getAsInt();
                if (amount <= 0) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': aspect '{}' amount must be positive.", research, entry.getKey());
                    return null;
                }

                Aspect aspect = Aspect.get(entry.getKey());
                if (aspect == null) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': unknown aspect '{}'.", research, entry.getKey());
                    return null;
                }

                recipe.aspect(aspect, amount);
            }

            return recipe;

        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to parse Thaumcraft crucible recipe: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 催化剂解析 ====================
    /**
     * 解析催化剂JSON对象
     * 支持三种类型：item、tag、string
     */
    private static Object parseCatalyst(JsonObject json, String research) {
        JsonElement typeElement = json.get("type");

        if (typeElement == null || !typeElement.isJsonPrimitive() || !typeElement.getAsJsonPrimitive().isString()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': catalyst has no valid type.", research);
            return null;
        }

        String type = typeElement.getAsString();

        switch (type) {
            case "item" -> {
                ItemStack stack = parseStack(json, "catalyst");
                if (stack.isEmpty()) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': catalyst item is empty.", research);
                    return null;
                }
                return stack;
            }

            case "tag" -> {
                JsonElement idElement = json.get("id");
                if (idElement == null || !idElement.isJsonPrimitive() || !idElement.getAsJsonPrimitive().isString()) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': catalyst tag has no valid id.", research);
                    return null;
                }

                try {
                    ResourceLocation id = ResourceLocation.parse(idElement.getAsString());
                    return TagKey.create(Registries.ITEM, id);
                } catch (Exception e) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': invalid catalyst tag '{}'.", research, idElement.getAsString());
                    return null;
                }
            }

            case "string" -> {
                JsonElement valueElement = json.get("value");
                if (valueElement == null || !valueElement.isJsonPrimitive() || !valueElement.getAsJsonPrimitive().isString()) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': catalyst string has no valid value.", research);
                    return null;
                }

                String value = valueElement.getAsString();
                if (value.isBlank()) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': catalyst string value is blank.", research);
                    return null;
                }

                return value;
            }

            default -> {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': unsupported catalyst type '{}'.", research, type);
                return null;
            }
        }
    }

    // ==================== 批量加载 ====================
    /**
     * 从资源管理器加载所有坩埚配方
     */
    public static void load(ResourceManager resourceManager) {
        if (resourceManager == null) return;

        Map<ResourceLocation, Resource> resources;
        try {
            resources = resourceManager.listResources(RECIPE_PATH, location -> location.getPath().endsWith(".json"));
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to scan Thaumcraft crucible recipes: {}", e.getMessage());
            return;
        }

        int loaded = 0, skipped = 0;

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            try (Reader reader = resource.openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);

                if (!element.isJsonObject()) {
                    GoldenTweaks.LOGGER.warn("Skipped Thaumcraft crucible recipe '{}': root is not an object.", location);
                    skipped++;
                    continue;
                }

                JsonObject json = element.getAsJsonObject();

                // 检查NeoForge条件
                if (!checkConditions(json, location)) {
                    skipped++;
                    continue;
                }

                // 检查模组依赖
                if (!checkRequiredMods(json, location)) {
                    skipped++;
                    continue;
                }

                GTCrucibleRecipe recipe = fromJson(json);
                if (recipe == null) {
                    GoldenTweaks.LOGGER.warn("Skipped invalid Thaumcraft crucible recipe '{}'.", location);
                    skipped++;
                    continue;
                }

                recipe.register();
                loaded++;
                GoldenTweaks.LOGGER.debug("Loaded Thaumcraft crucible recipe '{}'.", location);

            } catch (Exception e) {
                GoldenTweaks.LOGGER.warn("Failed to load Thaumcraft crucible recipe '{}': {}", location, e.getMessage());
                skipped++;
            }
        }

        GoldenTweaks.LOGGER.info("Thaumcraft crucible recipes: {} loaded, {} skipped.", loaded, skipped);
    }

    // ==================== 条件检查 ====================
    /**
     * 检查NeoForge条件系统
     */
    private static boolean checkConditions(JsonObject json, ResourceLocation location) {
        JsonElement element = json.get(CONDITIONS);
        if (element == null) return true;

        if (!element.isJsonArray()) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft crucible recipe '{}': '{}' must be an array.", location, CONDITIONS);
            return false;
        }

        return ICondition.LIST_CODEC
                .parse(JsonOps.INSTANCE, element)
                .resultOrPartial(error ->
                        GoldenTweaks.LOGGER.warn("Failed to decode conditions for Thaumcraft crucible recipe '{}': {}", location, error)
                )
                .map(conditions -> {
                    for (ICondition condition : conditions) {
                        if (!condition.test(ICondition.IContext.EMPTY)) {
                            GoldenTweaks.LOGGER.debug("Skipped Thaumcraft crucible recipe '{}': condition not met.", location);
                            return false;
                        }
                    }
                    return true;
                })
                .orElse(false);
    }

    /**
     * 检查配方中所有物品所属模组是否已加载
     * 支持嵌套结构（如 catalyst 的 items 数组）
     */
    private static boolean checkRequiredMods(JsonObject json, ResourceLocation location) {
        Set<String> requiredMods = new HashSet<>();

        // 检查 result
        collectItemMod(json.get("result"), requiredMods);

        // 检查 catalyst（支持嵌套结构）
        JsonElement catalystElement = json.get("catalyst");
        if (catalystElement != null && catalystElement.isJsonObject()) {
            collectCatalystMods(catalystElement.getAsJsonObject(), requiredMods);
        }

        // 验证所有模组都已加载
        for (String modId : requiredMods) {
            if (!ModList.get().isLoaded(modId)) {
                GoldenTweaks.LOGGER.debug("Skipped Thaumcraft crucible recipe '{}': required mod '{}' is not loaded.", location, modId);
                return false;
            }
        }

        return true;
    }

    /**
     * 递归收集催化剂中所有物品的模组ID
     * 支持：item、tag、string、items数组
     */
    private static void collectCatalystMods(JsonObject catalyst, Set<String> requiredMods) {
        JsonElement typeElement = catalyst.get("type");

        if (typeElement != null && typeElement.isJsonPrimitive() && typeElement.getAsJsonPrimitive().isString()) {
            String type = typeElement.getAsString();

            switch (type) {
                case "item" -> collectItemMod(catalyst, requiredMods);
                case "tag" -> collectIdMod(catalyst, requiredMods);
                case "items" -> {
                    JsonElement itemsElement = catalyst.get("items");
                    if (itemsElement != null && itemsElement.isJsonArray()) {
                        for (JsonElement item : itemsElement.getAsJsonArray()) {
                            collectItemMod(item, requiredMods);
                        }
                    }
                }
                case "string" -> {
                    // 字符串类型不包含模组ID，跳过
                }
            }
        } else {
            // 无 type 字段，按普通物品处理
            collectItemMod(catalyst, requiredMods);
        }
    }

    /**
     * 从JSON元素提取物品所属模组ID
     */
    private static void collectItemMod(JsonElement element, Set<String> requiredMods) {
        if (element == null || !element.isJsonObject()) return;
        collectIdMod(element.getAsJsonObject(), requiredMods);
    }

    /**
     * 从JSON对象提取ID中的模组命名空间
     */
    private static void collectIdMod(JsonObject json, Set<String> requiredMods) {
        JsonElement idElement = json.get("id");
        if (idElement == null || !idElement.isJsonPrimitive() || !idElement.getAsJsonPrimitive().isString()) return;

        try {
            ResourceLocation id = ResourceLocation.parse(idElement.getAsString());
            requiredMods.add(id.getNamespace());
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Invalid resource location '{}' while checking recipe dependencies.", idElement.getAsString());
        }
    }

    // ==================== 辅助解析方法 ====================
    private static ItemStack parseStack(JsonObject json, String path) {
        try {
            JsonElement idElement = json.get("id");
            if (idElement == null || !idElement.isJsonPrimitive() || !idElement.getAsJsonPrimitive().isString()) {
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

    private static String getRequiredString(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            GoldenTweaks.LOGGER.warn("Missing or invalid string field '{}'.", key);
            return null;
        }
        return element.getAsString();
    }

    private static JsonObject getRequiredObject(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonObject()) {
            GoldenTweaks.LOGGER.warn("Missing or invalid object field '{}'.", key);
            return null;
        }
        return element.getAsJsonObject();
    }
}