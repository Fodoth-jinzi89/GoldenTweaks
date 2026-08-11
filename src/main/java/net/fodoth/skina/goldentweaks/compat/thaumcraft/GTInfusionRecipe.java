package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
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
 * 神秘时代注魔配方管理器
 * 功能：配方定义、JSON解析、条件检查、自动加载
 * 支持：NeoForge条件系统、模组依赖检查、链式调用
 */
public final class GTInfusionRecipe {

    // ==================== 常量 ====================
    private static final String TYPE = "thaumcraft:infusion_matrix";           // 配方类型
    private static final String CONDITIONS = "neoforge:conditions";            // 条件字段
    private static final String RECIPE_PATH = "recipe/thaumcraft/infusion_matrix"; // 文件路径

    // ==================== 配方属性 ====================
    private final String research;          // 研究名称
    private final ItemStack output;         // 产出物品
    private final int instability;          // 不稳定度
    private final AspectList aspects = new AspectList();          // 源质需求
    private final List<ItemStack> components = new ArrayList<>(); // 普通材料
    private ItemStack catalyst;             // 催化剂

    // ==================== 构造方法 ====================
    private GTInfusionRecipe(String research, ItemStack output, int instability) {
        this.research = research;
        this.output = output.copy();
        this.instability = instability;
    }

    // ==================== 工厂方法 ====================
    public static GTInfusionRecipe create(String research, ItemLike output, int instability) {
        return create(research, new ItemStack(output), instability);
    }

    public static GTInfusionRecipe create(String research, ItemStack output, int instability) {
        return new GTInfusionRecipe(research, output, instability);
    }

    // ==================== 链式配置 ====================
    public GTInfusionRecipe aspect(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return this;
        aspects.add(aspect, amount);
        return this;
    }

    public GTInfusionRecipe catalyst(ItemLike item) {
        if (item != null) catalyst = new ItemStack(item);
        return this;
    }

    public GTInfusionRecipe catalyst(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) catalyst = stack.copy();
        return this;
    }

    public GTInfusionRecipe component(ItemLike item) {
        if (item != null) components.add(new ItemStack(item));
        return this;
    }

    public GTInfusionRecipe component(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) components.add(stack.copy());
        return this;
    }

    public GTInfusionRecipe components(ItemLike... items) {
        if (items != null) {
            for (ItemLike item : items) component(item);
        }
        return this;
    }

    public GTInfusionRecipe components(ItemStack... stacks) {
        if (stacks != null) {
            for (ItemStack stack : stacks) component(stack);
        }
        return this;
    }

    // ==================== 配方注册 ====================
    public void register() {
        if (output.isEmpty() || catalyst == null || catalyst.isEmpty() || components.isEmpty()) {
            return;
        }

        try {
            ThaumcraftApi.addInfusionCraftingRecipe(
                    research,
                    output,
                    instability,
                    aspects,
                    catalyst,
                    components.toArray(ItemStack[]::new)
            );
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft infusion recipe '{}': {}", research, e.getMessage());
        }
    }

    // ==================== JSON解析 ====================
    public static GTInfusionRecipe fromJson(JsonObject json) {
        if (json == null) return null;

        try {
            // 验证配方类型
            JsonElement typeElement = json.get("type");
            if (typeElement == null || !typeElement.isJsonPrimitive() ||
                    !typeElement.getAsJsonPrimitive().isString() || !TYPE.equals(typeElement.getAsString())) {
                return null;
            }

            // 解析基础信息
            String research = getRequiredString(json, "research");
            if (research == null) return null;

            int instability = getRequiredInt(json, "instability");
            if (instability < 0) return null;

            // 解析产出
            JsonObject resultObject = getRequiredObject(json, "result");
            if (resultObject == null) return null;
            ItemStack output = parseStack(resultObject, "result");
            if (output.isEmpty()) return null;

            GTInfusionRecipe recipe = create(research, output, instability);

            // 解析源质
            JsonObject aspectObject = getRequiredObject(json, "aspects");
            if (aspectObject == null) return null;

            for (Map.Entry<String, JsonElement> entry : aspectObject.entrySet()) {
                JsonElement value = entry.getValue();
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) return null;
                int amount = value.getAsInt();
                if (amount <= 0) return null;
                Aspect aspect = Aspect.get(entry.getKey());
                if (aspect == null) return null;
                recipe.aspect(aspect, amount);
            }

            // 解析催化剂
            JsonObject catalystObject = getRequiredObject(json, "catalyst");
            if (catalystObject == null) return null;
            ItemStack catalyst = parseStack(catalystObject, "catalyst");
            if (catalyst.isEmpty()) return null;
            recipe.catalyst(catalyst);

            // 解析材料
            JsonArray ingredients = getRequiredArray(json, "ingredients");
            if (ingredients == null || ingredients.isEmpty()) return null;

            for (int i = 0; i < ingredients.size(); i++) {
                JsonElement element = ingredients.get(i);
                if (!element.isJsonObject()) return null;
                ItemStack ingredient = parseStack(element.getAsJsonObject(), "ingredients[" + i + "]");
                if (ingredient.isEmpty()) return null;
                recipe.component(ingredient);
            }

            return recipe;

        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to parse Thaumcraft infusion recipe: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 批量加载 ====================
    /**
     * 从资源管理器加载所有配方
     */
    public static void load(ResourceManager resourceManager) {
        if (resourceManager == null) return;

        Map<ResourceLocation, Resource> resources;
        try {
            resources = resourceManager.listResources(RECIPE_PATH, location -> location.getPath().endsWith(".json"));
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to scan Thaumcraft infusion recipes: {}", e.getMessage());
            return;
        }

        int loaded = 0, skipped = 0;

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            try (Reader reader = resource.openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);

                if (!element.isJsonObject()) {
                    GoldenTweaks.LOGGER.warn("Skipped Thaumcraft infusion recipe '{}': root is not an object.", location);
                    skipped++;
                    continue;
                }

                JsonObject json = element.getAsJsonObject();

                // 检查条件
                if (!checkConditions(json, location)) {
                    skipped++;
                    continue;
                }

                // 检查模组依赖
                if (!checkRequiredMods(json, location)) {
                    skipped++;
                    continue;
                }

                GTInfusionRecipe recipe = fromJson(json);
                if (recipe == null) {
                    GoldenTweaks.LOGGER.warn("Skipped invalid Thaumcraft infusion recipe '{}'.", location);
                    skipped++;
                    continue;
                }

                recipe.register();
                loaded++;
                GoldenTweaks.LOGGER.debug("Registered Thaumcraft infusion recipe '{}'.", location);

            } catch (Exception e) {
                GoldenTweaks.LOGGER.warn("Failed to load Thaumcraft infusion recipe '{}': {}", location, e.getMessage());
                skipped++;
            }
        }

        GoldenTweaks.LOGGER.info("Thaumcraft infusion recipes: {} loaded, {} skipped.", loaded, skipped);
    }

    private static boolean checkConditions(JsonObject json, ResourceLocation location) {
        JsonElement element = json.get(CONDITIONS);

        if (element == null) {
            return true;
        }

        if (!element.isJsonArray()) {
            GoldenTweaks.LOGGER.warn(
                    "Skipped Thaumcraft infusion recipe '{}': '{}' must be an array.",
                    location,
                    CONDITIONS
            );
            return false;
        }

        return ICondition.LIST_CODEC
                .parse(JsonOps.INSTANCE, element)
                .resultOrPartial(error ->
                        GoldenTweaks.LOGGER.warn(
                                "Failed to decode conditions for Thaumcraft infusion recipe '{}': {}",
                                location,
                                error
                        )
                )
                .map(conditions -> {
                    for (ICondition condition : conditions) {
                        if (!condition.test(ICondition.IContext.EMPTY)) {
                            GoldenTweaks.LOGGER.debug(
                                    "Skipped Thaumcraft infusion recipe '{}': condition not met.",
                                    location
                            );
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

        collectItemMod(json.get("result"), requiredMods);
        collectItemMod(json.get("catalyst"), requiredMods);

        JsonElement ingredientsElement = json.get("ingredients");
        if (ingredientsElement != null && ingredientsElement.isJsonArray()) {
            for (JsonElement element : ingredientsElement.getAsJsonArray()) {
                collectItemMod(element, requiredMods);
            }
        }

        for (String modId : requiredMods) {
            if (!ModList.get().isLoaded(modId)) {
                GoldenTweaks.LOGGER.debug("Skipped Thaumcraft infusion recipe '{}': required item from mod '{}' is not loaded.", location, modId);
                return false;
            }
        }

        return true;
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
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Invalid item id '{}' while checking recipe dependencies.", idElement.getAsString());
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

    private static int getRequiredInt(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            GoldenTweaks.LOGGER.warn("Missing or invalid number field '{}'.", key);
            return -1;
        }
        return element.getAsInt();
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