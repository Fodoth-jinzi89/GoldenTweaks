package net.fodoth.skina.goldentweaks.compat.ftbquests.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务书章节图片（{@code ChapterImage.image}）在本包里是一串<b>贴图路径</b>，例如
 * {@code "minecraft:item/melon_slice"}、{@code "mekanism:item/bio_fuel"}、
 * {@code "minecraft:block/melon_side"}、{@code "northstar:fluid/liquid_oxygen_flow"}，
 * 于是 FTB 只会把它当成 {@code AtlasSpriteIcon}（从方块图集里抠一张贴图）画出来 ——
 * 没有 tooltip，也不能 R/U 查配方。
 *
 * <p>这里把贴图路径还原成真正的物品 / 流体：</p>
 * <ol>
 *     <li>按 id 猜：{@code <ns>:item/xxx} → {@code <ns>:xxx}；{@code block/xxx} 再去掉
 *     {@code _side}/{@code _still} 之类的后缀、取末段、调换末两段词序（{@code block/asphalt/white}
 *     → {@code white_asphalt}）。</li>
 *     <li>猜不到时用 {@link GTItemTextureIndex} 按「物品模型真正引用的贴图」反查，
 *     所以像 {@code dndesires:block/asphalt/white} 这种命名也能落到 {@code dndesires:white_asphalt}。</li>
 *     <li>流体/气体：按流体的静止/流动贴图精确匹配流体注册表，得到 {@link FluidStack}
 *     （{@code minecraft:block/water_still} → 水，{@code northstar:fluid/liquid_oxygen_flow} → 液氧）。</li>
 * </ol>
 *
 * <p>都找不到就返回空，调用方会退回原来的贴图绘制。结果按贴图路径缓存（只缓存命中的结果，
 * 避免注册表/模型还没准备好时把空结果永久缓存下来）；只在客户端渲染/交互线程使用。</p>
 */
public final class GTQuestImageItems {

    private static final String[] TEXTURE_SUFFIXES = {
            "_still", "_flowing", "_flow", "_side", "_top", "_bottom",
            "_front", "_back", "_end", "_inner", "_outer", "_source"
    };

    private static final Map<String, ItemStack> ITEM_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, FluidStack> FLUID_CACHE = new ConcurrentHashMap<>();

    private GTQuestImageItems() {
    }

    public static ItemStack resolveItem(Icon icon) {
        if (icon == null || icon.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (icon.getIngredient() instanceof ItemStack stack && !stack.isEmpty()) {
            return stack;
        }
        return fromItemTexture(icon.toString());
    }

    public static FluidStack resolveFluid(Icon icon) {
        if (icon == null || icon.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return fromFluidTexture(icon.toString());
    }

    private static ItemStack fromItemTexture(String path) {
        if (path == null || path.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack cached = ITEM_CACHE.get(path);
        if (cached != null) {
            return cached;
        }

        ItemStack resolved = lookupItem(path);
        if (!resolved.isEmpty()) {
            ITEM_CACHE.put(path, resolved);
        }
        return resolved;
    }

    private static FluidStack fromFluidTexture(String path) {
        if (path == null || path.isEmpty()) {
            return FluidStack.EMPTY;
        }

        FluidStack cached = FLUID_CACHE.get(path);
        if (cached != null) {
            return cached;
        }

        // 流体的贴图匹配要遍历流体注册表，比较贵；而且流体注册表在游戏启动时就已经固定，
        // 所以这里连「没匹配到」也一起缓存，避免每帧重复扫描。
        FluidStack resolved = lookupFluid(path);
        FLUID_CACHE.put(path, resolved);
        return resolved;
    }

    private static ItemStack lookupItem(String path) {
        int colon = path.indexOf(':');
        if (colon <= 0) {
            return ItemStack.EMPTY;
        }

        String namespace = path.substring(0, colon);
        String rest = path.substring(colon + 1);

        int slash = rest.indexOf('/');
        if (slash > 0) {
            if ("fluid".equals(rest.substring(0, slash))) {
                return ItemStack.EMPTY;
            }
            rest = rest.substring(slash + 1);
        }

        for (String candidate : candidates(namespace, rest)) {
            ItemStack stack = item(candidate);
            if (!stack.isEmpty()) {
                return stack;
            }
        }

        // 看起来是流体贴图（fluid/… 或 *_still / *_flow）就别去建物品反查表了，那是几千个物品的模型扫描，
        // 交给 resolveFluid 按流体注册表匹配更准也更便宜。
        if (looksLikeFluid(path)) {
            return ItemStack.EMPTY;
        }

        // 猜不出来时按物品模型实际引用的贴图反查
        return GTItemTextureIndex.findByTexture(path);
    }

    private static boolean looksLikeFluid(String path) {
        if (path.contains("/fluid/")) {
            return true;
        }
        int slash = path.lastIndexOf('/');
        String leaf = slash >= 0 ? path.substring(slash + 1) : path;
        return leaf.endsWith("_still") || leaf.endsWith("_flow") || leaf.endsWith("_flowing");
    }

    private static FluidStack lookupFluid(String path) {
        ResourceLocation wanted = ResourceLocation.tryParse(path);
        if (wanted == null) {
            return FluidStack.EMPTY;
        }

        for (Fluid fluid : BuiltInRegistries.FLUID) {
            IClientFluidTypeExtensions extensions;
            try {
                extensions = IClientFluidTypeExtensions.of(fluid);
            } catch (Throwable ignored) {
                continue;
            }
            if (extensions == null) {
                continue;
            }

            if (wanted.equals(extensions.getStillTexture()) || wanted.equals(extensions.getFlowingTexture())) {
                FluidStack stack = new FluidStack(fluid, 1000);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }
        return FluidStack.EMPTY;
    }

    private static List<String> candidates(String namespace, String texturePath) {
        List<String> result = new ArrayList<>(4);
        addCandidate(result, namespace, texturePath);

        String[] parts = texturePath.split("/");
        String leaf = parts[parts.length - 1];
        addCandidate(result, namespace, leaf);
        if (parts.length >= 2) {
            // block/asphalt/white → white_asphalt
            addCandidate(result, namespace, leaf + "_" + parts[parts.length - 2]);
        }

        String stripped = stripSuffix(leaf);
        if (stripped != null) {
            addCandidate(result, namespace, stripped);
        }
        return result;
    }

    private static void addCandidate(List<String> candidates, String namespace, String path) {
        String id = namespace + ":" + path;
        if (!path.isEmpty() && !candidates.contains(id)) {
            candidates.add(id);
        }
    }

    private static String stripSuffix(String path) {
        for (String suffix : TEXTURE_SUFFIXES) {
            if (path.length() > suffix.length() && path.endsWith(suffix)) {
                return path.substring(0, path.length() - suffix.length());
            }
        }
        return null;
    }

    private static ItemStack item(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null) {
            return ItemStack.EMPTY;
        }

        Item item = BuiltInRegistries.ITEM.get(key);
        return item == Items.AIR ? ItemStack.EMPTY : item.getDefaultInstance();
    }
}
