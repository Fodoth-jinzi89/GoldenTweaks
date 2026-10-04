package net.fodoth.skina.goldentweaks.compat.ftbquests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 「贴图路径 → 物品」的反查表：遍历所有物品的烘焙模型，把模型实际用到的贴图（粒子贴图 + 各方向
 * 四边形上的 sprite）登记下来。
 *
 * <p>任务书里的图片写的是贴图路径，靠给物品 id 猜（去目录、去后缀、调换词序）总会有漏网之鱼——
 * 例如 Create: Dreams n' Desires 的白色沥青是 {@code dndesires:white_asphalt}，而贴图叫
 * {@code dndesires:block/asphalt/white}，两个词是反的，猜不出来。反查表是按「物品模型真正引用的
 * 贴图」建的，所以这类命名照样能命中，也不会猜错物品。</p>
 *
 * <p>代价是要扫一遍物品注册表（几千个物品的方块/物品模型），所以只在按 id 猜不到时才构建，
 * 构建一次后常驻；必须在客户端渲染线程调用。</p>
 */
public final class GTItemTextureIndex {

    private static Map<String, ItemStack> index;

    private GTItemTextureIndex() {
    }

    public static ItemStack findByTexture(String texture) {
        if (texture == null || texture.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Map<String, ItemStack> cached = index;
        if (cached == null) {
            // 物品模型要资源包加载完才有意义；还没进世界时不建表、也不缓存，
            // 免得把一堆 missing model 的结果永久留在表里。
            if (Minecraft.getInstance().level == null) {
                return ItemStack.EMPTY;
            }
            cached = build();
            index = cached;
        }
        return cached.getOrDefault(texture, ItemStack.EMPTY);
    }

    private static Map<String, ItemStack> build() {
        Map<String, ItemStack> result = new HashMap<>(1 << 14);

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer renderer = minecraft.getItemRenderer();
        if (renderer == null) {
            return result;
        }

        RandomSource random = RandomSource.create();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (stack.isEmpty()) {
                continue;
            }

            BakedModel model;
            try {
                model = renderer.getModel(stack, minecraft.level, minecraft.player, 0);
            } catch (Throwable ignored) {
                continue;
            }
            if (model == null) {
                continue;
            }

            try {
                put(result, model.getParticleIcon(ModelData.EMPTY), stack);
                for (Direction direction : Direction.values()) {
                    putAll(result, model.getQuads(null, direction, random, ModelData.EMPTY, null), stack);
                }
                putAll(result, model.getQuads(null, null, random, ModelData.EMPTY, null), stack);
            } catch (Throwable ignored) {
                // 个别模组的模型取贴图时会炸，跳过即可
            }
        }
        return result;
    }

    private static void putAll(Map<String, ItemStack> map, List<BakedQuad> quads, ItemStack stack) {
        for (BakedQuad quad : quads) {
            put(map, quad.getSprite(), stack);
        }
    }

    private static void put(Map<String, ItemStack> map, TextureAtlasSprite sprite, ItemStack stack) {
        if (sprite == null) {
            return;
        }
        String name = sprite.contents().name().toString();
        if (!name.isEmpty()) {
            map.putIfAbsent(name, stack);
        }
    }
}
