package net.fodoth.skina.goldentweaks.compat.flavorimmerseddaily;

import net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Root crop farming processor
 * Randomly produces various root vegetables and tubers
 * This class only loads when flavor_immersed_daily mod is present
 */
public class RootCropProcedure {

    private static final boolean MOD_LOADED;
    private static final Map<Integer, Item> CROP_MAP = new HashMap<>();

    static {
        // Check if the mod is loaded
        boolean modLoaded = false;
        try {
            Class.forName("net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModItems");
            modLoaded = true;
        } catch (ClassNotFoundException e) {
            // Mod not present
            System.out.println("[GoldenTweaks] flavor_immersed_daily mod not found, RootCropProcedure will be disabled.");
        }
        MOD_LOADED = modLoaded;

        if (MOD_LOADED) {
            initializeCropMap();
        }
    }

    private static void initializeCropMap() {
        CROP_MAP.put(1, FlavorImmersedDailyModItems.GARLIC.get());
        CROP_MAP.put(2, Items.POTATO);
        CROP_MAP.put(3, Items.CARROT);
        CROP_MAP.put(4, FlavorImmersedDailyModItems.LOTUSROOT.get());
        CROP_MAP.put(5, FlavorImmersedDailyModItems.RADISH.get());
        CROP_MAP.put(6, FlavorImmersedDailyModItems.MUSTARD.get());
        CROP_MAP.put(7, FlavorImmersedDailyModItems.SWEETPOTATO.get());
        CROP_MAP.put(8, FlavorImmersedDailyModItems.PURPLESWEETPOTATO.get());
        CROP_MAP.put(9, FlavorImmersedDailyModItems.CASSAVA.get());
        CROP_MAP.put(10, FlavorImmersedDailyModItems.GINGER.get());
        CROP_MAP.put(11, FlavorImmersedDailyModItems.ONION.get());
        CROP_MAP.put(12, FlavorImmersedDailyModItems.CHINESEYAM.get());
        CROP_MAP.put(13, BuiltInRegistries.ITEM.get(ResourceLocation.parse("flavor_immersed_daily_argo:konjac")));
        CROP_MAP.put(14, BuiltInRegistries.ITEM.get(ResourceLocation.parse("flavor_immersed_daily_argo:taro")));
        CROP_MAP.put(15, BuiltInRegistries.ITEM.get(ResourceLocation.parse("flavor_immersed_daily_argo:bamboo_shoots")));
    }

    private static final int MIN_SPAWN = 3;
    private static final int MAX_SPAWN = 5;
    private static final int SPAWN_OFFSET_Y = 1;
    private static final int PICKUP_DELAY_TICKS = 10;

    public RootCropProcedure() {
    }

    /**
     * Execute root crop spawning logic
     * @param world The world level
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public static void execute(LevelAccessor world, double x, double y, double z) {
        // Skip if mod is not loaded
        if (!MOD_LOADED) {
            return;
        }

        if (!(world instanceof ServerLevel serverLevel)) {
            return;
        }

        RandomSource random = RandomSource.create();
        int spawnCount = Mth.nextInt(random, MIN_SPAWN, MAX_SPAWN);

        for (int i = 0; i < spawnCount; i++) {
            int cropId = Mth.nextInt(random, 1, 15);
            Item crop = CROP_MAP.get(cropId);

            if (crop != null) {
                ItemEntity itemEntity = new ItemEntity(
                        serverLevel,
                        x,
                        y + SPAWN_OFFSET_Y,
                        z,
                        new ItemStack(crop)
                );
                itemEntity.setPickUpDelay(PICKUP_DELAY_TICKS);
                serverLevel.addFreshEntity(itemEntity);
            }
        }
    }

    /**
     * Get all possible crop items (for external query)
     */
    public static Map<Integer, Item> getCropMap() {
        return MOD_LOADED ? CROP_MAP : new HashMap<>();
    }

    /**
     * Check if the required mod is loaded
     */
    public static boolean isModLoaded() {
        return MOD_LOADED;
    }
}