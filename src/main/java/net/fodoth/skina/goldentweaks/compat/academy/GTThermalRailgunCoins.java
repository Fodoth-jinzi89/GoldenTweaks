package net.fodoth.skina.goldentweaks.compat.academy;

import com.mohistmc.academy.world.entity.CoinEntity;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 热力（Thermal）硬币作为学院超电磁炮（{@code railgun}）弹药时的兼容数据。
 *
 * <p>学院原本只认自己抛出的 {@code academy:coin}；这里把热力的各金属硬币也纳入弹药，
 * 并为每种硬币给出「伤害 / 破坏 / 光线直径 / 光线长度」的统一倍率（见 {@link #MULTIPLIERS}）。
 * 硬币的贴图直接取热力自己的 {@code thermal:textures/item/<metal>_coin_<帧>.png}，
 * 因此每种硬币都会显示其对应颜色。</p>
 */
public final class GTThermalRailgunCoins {

    /** 各金属硬币的倍率（伤害、破坏能量、破坏半径、射程、光束粗细统一乘该值）。 */
    private static final Map<String, Double> MULTIPLIERS = Map.ofEntries(
            Map.entry("thermal:iron_coin", 1.0D),
            Map.entry("thermal:gold_coin", 2.0D),
            Map.entry("thermal:copper_coin", 0.8D),
            Map.entry("thermal:netherite_coin", 8.0D),
            Map.entry("thermal:tin_coin", 1.2D),
            Map.entry("thermal:lead_coin", 1.4D),
            Map.entry("thermal:silver_coin", 1.5D),
            Map.entry("thermal:nickel_coin", 2.4D),
            Map.entry("thermal:signalum_coin", 6.0D),
            Map.entry("thermal:lumium_coin", 6.0D),
            Map.entry("thermal:enderium_coin", 10.0D),
            Map.entry("thermal:bronze_coin", 1.6D),
            Map.entry("thermal:electrum_coin", 2.5D),
            Map.entry("thermal:invar_coin", 2.7D),
            Map.entry("thermal:constantan_coin", 1.7D)
    );

    public static boolean isThermalCoin(ItemStack stack) {
        return !stack.isEmpty() && MULTIPLIERS.containsKey(idOf(stack.getItem()));
    }

    /** 返回该硬币对应的物品 id（{@code thermal:xxx_coin}）；非热力硬币返回空串。 */
    public static String variantOf(ItemStack stack) {
        String id = idOf(stack.getItem());
        return MULTIPLIERS.containsKey(id) ? id : "";
    }

    /** 返回抛出硬币对应的倍率；非热力硬币（含学院币）返回 1.0。 */
    public static double multiplierOf(CoinEntity coin) {
        Double value = MULTIPLIERS.get(((GTCoinVariant) coin).gt$getCoinVariant());
        return value != null ? value : 1.0D;
    }

    /** 按变体 id 还原出硬币物品；无法还原时返回空堆。 */
    public static ItemStack stackForVariant(String variant) {
        if (variant == null || variant.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(variant);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        return item == null || item == net.minecraft.world.item.Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    /** 飞行硬币的贴图：热力硬币用「单枚」帧（各种金属不同颜色），学院币保持原贴图。 */
    public static ResourceLocation entityTexture(CoinEntity coin, ResourceLocation fallback) {
        String variant = ((GTCoinVariant) coin).gt$getCoinVariant();
        if (variant.isEmpty()) {
            return fallback;
        }
        ResourceLocation id = ResourceLocation.tryParse(variant);
        if (id == null) {
            return fallback;
        }
        // 热力硬币的贴图按堆叠数量分帧：_0=1 枚、_1=2 枚、_2=16 枚、_3=32 枚、_4=一整堆。
        // 飞出去的是一枚硬币，固定用 _0 单枚贴图，避免看起来像飞出一组。
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/item/" + id.getPath() + "_0.png");
    }

    private static String idOf(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return id == null ? "" : id.toString();
    }

    private GTThermalRailgunCoins() {
    }
}
