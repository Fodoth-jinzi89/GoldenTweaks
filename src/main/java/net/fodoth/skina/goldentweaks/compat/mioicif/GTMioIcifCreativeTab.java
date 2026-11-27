package net.fodoth.skina.goldentweaks.compat.mioicif;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * SI 的「模式存储机」（{@code mio_icif:producer/block_pattern_storage}）注册了方块与方块实体，
 * 却既没有配方、也没有出现在它自己的创造栏 {@code mio_icif:icif_tab} 里，正常游戏根本拿不到。
 *
 * <p>本条只补创造栏；配方见 {@code data/goldentweaks/recipe/mio_icif/pattern_storage.json}。</p>
 *
 * <p>这里按注册表 id 取物品而不是直接引用 SI 的类：这样本类不链接任何 {@code mio_icif} 类型，
 * 即使整合包没装 SI 也不会在类校验阶段抛出 {@code NoClassDefFoundError}。</p>
 */
public final class GTMioIcifCreativeTab {

    private static final ResourceLocation ICIF_TAB =
            ResourceLocation.fromNamespaceAndPath("mio_icif", "icif_tab");

    private static final ResourceLocation PATTERN_STORAGE =
            ResourceLocation.fromNamespaceAndPath("mio_icif", "producer/block_pattern_storage");

    private GTMioIcifCreativeTab() {
    }

    public static void addPatternStorage(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().location().equals(ICIF_TAB)) {
            return;
        }

        Item block = BuiltInRegistries.ITEM.get(PATTERN_STORAGE);
        if (block == null) {
            return;
        }
        // 同一份条目重复添加时 NeoForge 会直接抛异常，这里按约定先查一遍
        ItemStack stack = new ItemStack(block);
        if (!event.getParentEntries().contains(stack)) {
            event.accept(stack);
        }
    }
}
