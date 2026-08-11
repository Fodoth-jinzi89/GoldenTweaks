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
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.io.Reader;
import java.util.*;

/**
 * 神秘时代奥术合成配方管理器
 * 功能：配方定义、JSON解析、条件检查、自动加载
 * 支持：NeoForge条件系统、模组依赖检查、多种配料类型（物品/标签/字符串）
 * 支持：有序（shaped）和无序（shapeless）两种合成模式
 */
public final class GTArcaneRecipe {

    // ==================== 常量 ====================
    private static final String TYPE = "thaumcraft:arcane_crafting";        // 配方类型
    private static final String CONDITIONS = "neoforge:conditions";         // 条件字段
    private static final String RECIPE_PATH = "recipe/thaumcraft/arcane_crafting"; // 文件路径

    // ==================== 配方属性 ====================
    private final String research;          // 研究名称
    private final ItemStack output;         // 产出物品
    private final AspectList aspects = new AspectList(); // 源质需求

    // ==================== 构造方法 ====================
    private GTArcaneRecipe(String research, ItemStack output) {
        this.research = research;
        this.output = output.copy();
    }

    // ==================== 工厂方法 ====================
    public static GTArcaneRecipe create(String research, ItemLike output) {
        return create(research, new ItemStack(output));
    }

    public static GTArcaneRecipe create(String research, ItemStack output) {
        return new GTArcaneRecipe(research, output);
    }

    // ==================== 链式配置 ====================
    public GTArcaneRecipe aspect(Aspect aspect, int amount) {
        if (aspect != null && amount > 0) {
            aspects.add(aspect, amount);
        }
        return this;
    }

    // ==================== 配方注册 ====================
    /**
     * 注册有序合成配方（shaped）
     *
     * @param mirrored 是否镜像
     * @param recipe   配方参数（模式字符串 + 键值对）
     */
    public void registerShaped(boolean mirrored, Object... recipe) {
        if (output.isEmpty() || recipe.length == 0) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft arcane shaped recipe '{}': invalid recipe.", research);
            return;
        }

        try {
            Object[] args = new Object[recipe.length + 1];
            args[0] = mirrored;
            System.arraycopy(recipe, 0, args, 1, recipe.length);

            ThaumcraftApi.addArcaneCraftingRecipe(research, output, aspects, args);
            GoldenTweaks.LOGGER.debug("Registered Thaumcraft arcane shaped recipe '{}'.", research);
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft arcane shaped recipe '{}': {}", research, e.getMessage());
        }
    }

    /**
     * 注册无序合成配方（shapeless）
     *
     * @param recipe 配料列表
     */
    public void registerShapeless(Object... recipe) {
        if (output.isEmpty() || recipe.length == 0) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft arcane shapeless recipe '{}': invalid recipe.", research);
            return;
        }

        try {
            ThaumcraftApi.addShapelessArcaneCraftingRecipe(research, output, aspects, recipe);
            GoldenTweaks.LOGGER.debug("Registered Thaumcraft arcane shapeless recipe '{}'.", research);
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft arcane shapeless recipe '{}': {}", research, e.getMessage());
        }
    }

    // ==================== JSON解析 ====================
    /**
     * 从JSON解析奥术合成配方
     * 支持两种模式：
     *   - shaped: { "mode": "shaped", "pattern": ["AB", "BA"], "key": { "A": {...}, "B": {...} } }
     *   - shapeless: { "mode": "shapeless", "ingredients": [...] }
     */
    public static GTArcaneRecipe fromJson(JsonObject json) {
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
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft arcane recipe '{}': result is empty.", research);
                return null;
            }

            GTArcaneRecipe recipe = create(research, output);

            // 解析源质需求
            JsonObject aspectObject = getRequiredObject(json, "aspects");
            if (aspectObject == null || aspectObject.isEmpty()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft arcane recipe '{}': aspects must not be empty.", research);
                return null;
            }

            for (Map.Entry<String, JsonElement> entry : aspectObject.entrySet()) {
                JsonElement value = entry.getValue();
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft arcane recipe '{}': aspect '{}' amount must be a number.", research, entry.getKey());
                    return null;
                }

                int amount = value.getAsInt();
                if (amount <= 0) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft arcane recipe '{}': aspect '{}' amount must be positive.", research, entry.getKey());
                    return null;
                }

                Aspect aspect = Aspect.get(entry.getKey());
                if (aspect == null) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft arcane recipe '{}': unknown aspect '{}'.", research, entry.getKey());
                    return null;
                }

                recipe.aspect(aspect, amount);
            }

            return recipe;

        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to parse Thaumcraft arcane recipe: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 批量加载 ====================
    /**
     * 从资源管理器加载所有奥术合成配方
     */
    public static void load(ResourceManager resourceManager) {
        if (resourceManager == null) return;

        Map<ResourceLocation, Resource> resources;
        try {
            resources = resourceManager.listResources(RECIPE_PATH, location -> location.getPath().endsWith(".json"));
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to scan Thaumcraft arcane recipes: {}", e.getMessage());
            return;
        }

        int loaded = 0, skipped = 0;

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            try (Reader reader = resource.openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);

                if (!element.isJsonObject()) {
                    GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': root is not an object.", location);
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

                GTArcaneRecipe recipe = fromJson(json);
                if (recipe == null) {
                    GoldenTweaks.LOGGER.warn("Skipped invalid Thaumcraft arcane recipe '{}'.", location);
                    skipped++;
                    continue;
                }

                // 解析合成模式
                JsonElement modeElement = json.get("mode");
                if (modeElement == null || !modeElement.isJsonPrimitive() || !modeElement.getAsJsonPrimitive().isString()) {
                    GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': missing or invalid 'mode'.", location);
                    skipped++;
                    continue;
                }

                String mode = modeElement.getAsString();

                boolean success;
                if ("shaped".equals(mode)) {
                    success = recipe.loadShaped(json, location);
                } else if ("shapeless".equals(mode)) {
                    success = recipe.loadShapeless(json, location);
                } else {
                    GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': unsupported mode '{}'.", location, mode);
                    success = false;
                }

                if (success) {
                    loaded++;
                    GoldenTweaks.LOGGER.debug("Loaded Thaumcraft arcane recipe '{}'.", location);
                } else {
                    skipped++;
                }

            } catch (Exception e) {
                GoldenTweaks.LOGGER.warn("Failed to load Thaumcraft arcane recipe '{}': {}", location, e.getMessage());
                skipped++;
            }
        }

        GoldenTweaks.LOGGER.info("Thaumcraft arcane recipes: {} loaded, {} skipped.", loaded, skipped);
    }

    // ==================== 有序合成加载 ====================
    /**
     * 加载有序合成配方
     */
    private boolean loadShaped(JsonObject json, ResourceLocation location) {
        JsonArray patternArray = getRequiredArray(json, "pattern");
        if (patternArray == null || patternArray.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': pattern is empty.", location);
            return false;
        }

        // 验证模式行
        String[] pattern = new String[patternArray.size()];
        int width = -1;

        for (int i = 0; i < patternArray.size(); i++) {
            JsonElement element = patternArray.get(i);
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': pattern[{}] is invalid.", location, i);
                return false;
            }

            pattern[i] = element.getAsString();
            if (pattern[i].isEmpty()) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': pattern[{}] is empty.", location, i);
                return false;
            }

            if (width == -1) {
                width = pattern[i].length();
            } else if (pattern[i].length() != width) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': pattern rows have different widths.", location);
                return false;
            }

            if (width > 3 || pattern.length > 3) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': pattern exceeds 3x3.", location);
                return false;
            }
        }

        // 解析键值映射
        JsonObject keyObject = getRequiredObject(json, "key");
        if (keyObject == null) return false;

        // 收集模式中使用的字符
        Set<Character> usedKeys = new HashSet<>();
        for (String row : pattern) {
            for (char character : row.toCharArray()) {
                if (character != ' ') {
                    usedKeys.add(character);
                }
            }
        }

        // 构建配方数组
        Object[] recipe = new Object[pattern.length + keyObject.size() * 2];
        int index = 0;

        for (String row : pattern) {
            recipe[index++] = row;
        }

        for (Map.Entry<String, JsonElement> entry : keyObject.entrySet()) {
            if (entry.getKey().length() != 1) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': invalid key '{}'.", location, entry.getKey());
                return false;
            }

            char key = entry.getKey().charAt(0);
            if (!usedKeys.contains(key)) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': key '{}' is not used by pattern.", location, key);
                return false;
            }

            Object ingredient = parseIngredient(entry.getValue(), location);
            if (ingredient == null) return false;

            recipe[index++] = key;
            recipe[index++] = ingredient;
        }

        // 解析镜像标记
        boolean mirrored = true;
        JsonElement mirroredElement = json.get("mirrored");
        if (mirroredElement != null) {
            if (!mirroredElement.isJsonPrimitive() || !mirroredElement.getAsJsonPrimitive().isBoolean()) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': 'mirrored' must be boolean.", location);
                return false;
            }
            mirrored = mirroredElement.getAsBoolean();
        }

        registerShaped(mirrored, recipe);
        return true;
    }

    // ==================== 无序合成加载 ====================
    /**
     * 加载无序合成配方
     */
    private boolean loadShapeless(JsonObject json, ResourceLocation location) {
        JsonArray ingredients = getRequiredArray(json, "ingredients");
        if (ingredients == null || ingredients.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': ingredients are empty.", location);
            return false;
        }

        if (ingredients.size() > 9) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': too many ingredients (max 9).", location);
            return false;
        }

        Object[] recipe = new Object[ingredients.size()];
        for (int i = 0; i < ingredients.size(); i++) {
            Object ingredient = parseIngredient(ingredients.get(i), location);
            if (ingredient == null) return false;
            recipe[i] = ingredient;
        }

        registerShapeless(recipe);
        return true;
    }

    // ==================== 配料解析 ====================
    /**
     * 解析配料JSON
     * 支持三种类型：item、tag、string
     */
    private static Object parseIngredient(JsonElement element, ResourceLocation location) {
        if (!element.isJsonObject()) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': ingredient must be an object.", location);
            return null;
        }

        JsonObject json = element.getAsJsonObject();

        JsonElement typeElement = json.get("type");
        if (typeElement == null || !typeElement.isJsonPrimitive() || !typeElement.getAsJsonPrimitive().isString()) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': ingredient has no valid type.", location);
            return null;
        }

        String type = typeElement.getAsString();

        return switch (type) {
            case "item" -> {
                ItemStack stack = parseStack(json, "ingredient");
                if (stack.isEmpty()) {
                    GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': invalid item ingredient.", location);
                    yield null;
                }
                yield stack;
            }

            case "tag" -> {
                JsonElement idElement = json.get("id");
                if (idElement == null || !idElement.isJsonPrimitive() || !idElement.getAsJsonPrimitive().isString()) {
                    GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': tag has no valid id.", location);
                    yield null;
                }

                try {
                    ResourceLocation id = ResourceLocation.parse(idElement.getAsString());
                    yield TagKey.create(Registries.ITEM, id);
                } catch (Exception e) {
                    GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': invalid tag '{}'.", location, idElement.getAsString());
                    yield null;
                }
            }

            case "string" -> {
                JsonElement valueElement = json.get("value");
                if (valueElement == null || !valueElement.isJsonPrimitive() || !valueElement.getAsJsonPrimitive().isString()) {
                    GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': string ingredient has no valid value.", location);
                    yield null;
                }

                String value = valueElement.getAsString();
                yield value.isBlank() ? null : value;
            }

            default -> {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': unsupported ingredient type '{}'.", location, type);
                yield null;
            }
        };
    }

    // ==================== 条件检查 ====================
    /**
     * 检查NeoForge条件系统
     */
    private static boolean checkConditions(JsonObject json, ResourceLocation location) {
        JsonElement element = json.get(CONDITIONS);
        if (element == null) return true;

        if (!element.isJsonArray()) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': '{}' must be an array.", location, CONDITIONS);
            return false;
        }

        return ICondition.LIST_CODEC
                .parse(JsonOps.INSTANCE, element)
                .resultOrPartial(error ->
                        GoldenTweaks.LOGGER.warn("Failed to decode conditions for Thaumcraft arcane recipe '{}': {}", location, error)
                )
                .map(conditions -> {
                    for (ICondition condition : conditions) {
                        if (!condition.test(ICondition.IContext.EMPTY)) {
                            GoldenTweaks.LOGGER.debug("Skipped Thaumcraft arcane recipe '{}': condition not met.", location);
                            return false;
                        }
                    }
                    return true;
                })
                .orElse(false);
    }

    /**
     * 检查配方中所有物品所属模组是否已加载
     */
    private static boolean checkRequiredMods(JsonObject json, ResourceLocation location) {
        Set<String> requiredMods = new HashSet<>();

        // 检查 result
        collectItemMod(json.get("result"), requiredMods);

        // 检查 key（有序合成）
        JsonElement keyElement = json.get("key");
        if (keyElement != null && keyElement.isJsonObject()) {
            for (JsonElement ingredient : keyElement.getAsJsonObject().entrySet().stream().map(Map.Entry::getValue).toList()) {
                collectIngredientMod(ingredient, requiredMods);
            }
        }

        // 检查 ingredients（无序合成）
        JsonElement ingredientsElement = json.get("ingredients");
        if (ingredientsElement != null && ingredientsElement.isJsonArray()) {
            for (JsonElement ingredient : ingredientsElement.getAsJsonArray()) {
                collectIngredientMod(ingredient, requiredMods);
            }
        }

        // 验证所有模组都已加载
        for (String modId : requiredMods) {
            if (!ModList.get().isLoaded(modId)) {
                GoldenTweaks.LOGGER.debug("Skipped Thaumcraft arcane recipe '{}': required mod '{}' is not loaded.", location, modId);
                return false;
            }
        }

        return true;
    }

    /**
     * 从配料中提取模组ID
     */
    private static void collectIngredientMod(JsonElement element, Set<String> requiredMods) {
        if (!element.isJsonObject()) return;

        JsonObject json = element.getAsJsonObject();
        JsonElement typeElement = json.get("type");

        if (typeElement == null || !typeElement.isJsonPrimitive() || !typeElement.getAsJsonPrimitive().isString()) {
            return;
        }

        String type = typeElement.getAsString();
        if ("item".equals(type) || "tag".equals(type)) {
            collectItemMod(json, requiredMods);
        }
    }

    /**
     * 从JSON元素提取物品所属模组ID
     */
    private static void collectItemMod(JsonElement element, Set<String> requiredMods) {
        if (element == null || !element.isJsonObject()) return;

        JsonElement idElement = element.getAsJsonObject().get("id");
        if (idElement == null || !idElement.isJsonPrimitive() || !idElement.getAsJsonPrimitive().isString()) return;

        try {
            ResourceLocation id = ResourceLocation.parse(idElement.getAsString());
            requiredMods.add(id.getNamespace());
        } catch (Exception ignored) {
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

    private static JsonArray getRequiredArray(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonArray()) {
            GoldenTweaks.LOGGER.warn("Missing or invalid array field '{}'.", key);
            return null;
        }
        return element.getAsJsonArray();
    }
}