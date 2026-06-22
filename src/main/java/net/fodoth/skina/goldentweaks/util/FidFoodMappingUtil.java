package net.fodoth.skina.goldentweaks.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModBlocks;
import net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * FID 模组食物映射工具类
 * 用于管理食物物品与对应方块的双向映射关系
 */
public class FidFoodMappingUtil {

    // 物品 -> 方块的映射
    private static final Map<Item, Block> ITEM_TO_BLOCK_MAP = new HashMap<>();

    // 方块 -> 物品的映射
    private static final Map<Block, Item> BLOCK_TO_ITEM_MAP = new HashMap<>();

    static {
        // 注册所有 FID 食物映射
        registerMapping(
                FlavorImmersedDailyModItems.DRAWNEGGPLANT.get(),
                FlavorImmersedDailyModBlocks.BA_SI_QIE_ZI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.BOILED_CHICKENWITH_SAUCE.get(),
                FlavorImmersedDailyModBlocks.BAI_QIE_JI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.MIXEDCOLDDISHES.get(),
                FlavorImmersedDailyModBlocks.BAN_SI_XIAO_LIANG_CAI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.FRIEDLIVERTIPWITHSPINACH.get(),
                FlavorImmersedDailyModBlocks.BO_CAI_CHAO_GAN_JIAN.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.PINEAPPLE_SWEETAND_SOUR_PORK.get(),
                FlavorImmersedDailyModBlocks.BO_LUO_GU_LU_ROU.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.FRIEDCOWPEA.get(),
                FlavorImmersedDailyModBlocks.CHAO_DOU_JUE_ZI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.FRIEDSPICYCHICKEN.get(),
                FlavorImmersedDailyModBlocks.CHAO_LA_ZI_JI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.CHINESECABBAGEINSOUP.get(),
                FlavorImmersedDailyModBlocks.KAI_SHUI_BAI_CAI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.PLEUROTUSERYNGIIWITHSALTANDPEPPER.get(),
                FlavorImmersedDailyModBlocks.JIAO_YAN_CHAO_GU.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.STIRFRIEDBOILEDPORKSLICESINHOTSAUCE.get(),
                FlavorImmersedDailyModBlocks.HUI_GUO_ROU.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.KUNGPAOCHICKEN.get(),
                FlavorImmersedDailyModBlocks.GONG_BAO_JI_DING.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.GAN_BIAN_SI_JI_DOU.get(),
                FlavorImmersedDailyModBlocks.GAN_BIAN_SI_JI_DOU.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.SAUTEED_POTATO_GREEN_PEPPER_EGGPLANT.get(),
                FlavorImmersedDailyModBlocks.DI_SAN_XIAN.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.SALTEDEGGYOLKFRIEDCAULIFLOWER.get(),
                FlavorImmersedDailyModBlocks.XIAN_DAN_HUANG_CHAO_CAI_HUA.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.CHICKENWITH_SCALLION_OIL.get(),
                FlavorImmersedDailyModBlocks.CONG_YOU_JI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.COLACHICKENWINGS.get(),
                FlavorImmersedDailyModBlocks.KE_LE_JI_CHI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.STEAMED_CHICKENWITH_CHILI_SAUCE.get(),
                FlavorImmersedDailyModBlocks.KOU_SHUI_JI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.PRESERVEDEGGSALAD.get(),
                FlavorImmersedDailyModBlocks.LIANG_BAN_PI_DAN.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.TOMATOSALAD.get(),
                FlavorImmersedDailyModBlocks.LIANG_BAN_XI_HONG_SHI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.LINYIFRIEDCHICKEN.get(),
                FlavorImmersedDailyModBlocks.LIN_YI_CHAO_JI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.SPICYCABBAGE.get(),
                FlavorImmersedDailyModBlocks.MA_LA_JUAN_XIN_CAI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.SPICYTOFU.get(),
                FlavorImmersedDailyModBlocks.MA_PO_DOU_FU.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.BEANWITHSESAMESAUCE.get(),
                FlavorImmersedDailyModBlocks.MA_ZHI_DOU_JIAO.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.SCRAMBLEDEGGSWITHFUNGUSANDCUCUMBER.get(),
                FlavorImmersedDailyModBlocks.MU_ER_HUANG_GUA_JI_DAN.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.JAPANESEBRAISEDTOFU.get(),
                FlavorImmersedDailyModBlocks.RI_BEN_HONG_SHAO_DOU_FU.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.POACHED_SPICY_SLICESOF_PORK.get(),
                FlavorImmersedDailyModBlocks.SHUI_ZHU_ROU_PIAN.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.SLICED_FISHIN_HOT_CHILI_OIL.get(),
                FlavorImmersedDailyModBlocks.SHUI_ZHU_YU.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.FRIEDMEATWITHCUMINONION.get(),
                FlavorImmersedDailyModBlocks.ZI_RAN_YANG_CONG_CHAO_ROU_PIAN.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.FOURJOYMEATBALLS.get(),
                FlavorImmersedDailyModBlocks.SI_XI_WAN_ZI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.BOILED_FISHWITH_PICKLED_CABBAGEAND_CHILI.get(),
                FlavorImmersedDailyModBlocks.SUAN_CAI_YU.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.MEATBALLSOUP.get(),
                FlavorImmersedDailyModBlocks.WAN_ZI_TANG.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.ZUCCHINISNACKMEAT.get(),
                FlavorImmersedDailyModBlocks.XI_HU_LU_CHAO_ROU.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.SAUTEEDMUSHROOMSWITHRAPESEED.get(),
                FlavorImmersedDailyModBlocks.YOU_CAI_CHAO_XIANG_GU.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.FRIEDSHREDDEDPORKWITHSWEETANDSOURSAUCE.get(),
                FlavorImmersedDailyModBlocks.YU_XIANG_ROU_SI.get()
        );

        registerMapping(
                FlavorImmersedDailyModItems.STEAMEDFISH.get(),
                FlavorImmersedDailyModBlocks.QING_ZHENG_YU.get()
        );

    }

    /**
     * 注册一个物品到方块的映射
     */
    private static void registerMapping(Item item, Block block) {
        ITEM_TO_BLOCK_MAP.put(item, block);
        BLOCK_TO_ITEM_MAP.put(block, item);
    }

    /**
     * 根据物品获取对应的方块
     */
    public static Optional<Block> getBlockByItem(Item item) {
        return Optional.ofNullable(ITEM_TO_BLOCK_MAP.get(item));
    }

    /**
     * 根据物品堆获取对应的方块
     */
    public static Optional<Block> getBlockByItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return getBlockByItem(stack.getItem());
    }

    /**
     * 根据方块获取对应的物品
     */
    public static Optional<Item> getItemByBlock(Block block) {
        return Optional.ofNullable(BLOCK_TO_ITEM_MAP.get(block));
    }

    /**
     * 根据方块获取对应的物品堆
     */
    public static Optional<ItemStack> getItemStackByBlock(Block block) {
        return getItemByBlock(block).map(ItemStack::new);
    }

    /**
     * 根据方块状态获取对应的物品
     */
    public static Optional<Item> getItemByBlockState(net.minecraft.world.level.block.state.BlockState state) {
        if (state == null || state.isAir()) {
            return Optional.empty();
        }
        return getItemByBlock(state.getBlock());
    }

    /**
     * 判断是否为 FID 食物物品
     */
    public static boolean isFidFoodItem(Item item) {
        return ITEM_TO_BLOCK_MAP.containsKey(item);
    }

    /**
     * 判断是否为 FID 食物方块
     */
    public static boolean isFidFoodBlock(Block block) {
        return BLOCK_TO_ITEM_MAP.containsKey(block);
    }

    /**
     * 判断物品堆是否为 FID 食物
     */
    public static boolean isFidFoodItemStack(ItemStack stack) {
        return !stack.isEmpty() && isFidFoodItem(stack.getItem());
    }

    /**
     * 检查两个对象是否匹配（一个是物品，一个是方块）
     */
    public static boolean isMatching(Item item, Block block) {
        if (item == null || block == null) {
            return false;
        }
        Block mappedBlock = ITEM_TO_BLOCK_MAP.get(item);
        return mappedBlock != null && mappedBlock.equals(block);
    }

    /**
     * 获取所有已注册的食物物品
     */
    public static Map<Item, Block> getAllItemMappings() {
        return new HashMap<>(ITEM_TO_BLOCK_MAP);
    }

    /**
     * 获取所有已注册的食物方块
     */
    public static Map<Block, Item> getAllBlockMappings() {
        return new HashMap<>(BLOCK_TO_ITEM_MAP);
    }

    /**
     * 获取物品对应的方块，如果不存在则返回空气
     */
    public static Block getBlockOrAir(Item item) {
        return getBlockByItem(item).orElse(Blocks.AIR);
    }

    /**
     * 获取方块对应的物品，如果不存在则返回空物品堆
     */
    public static ItemStack getItemStackOrEmpty(Block block) {
        return getItemStackByBlock(block).orElse(ItemStack.EMPTY);
    }
}
