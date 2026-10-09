package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.CreateRegistries;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SSCreateCompat {

    public static final DeferredRegister<MountedItemStorageType<?>> MOUNTED_STORAGE_TYPES =
            DeferredRegister.create(CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE, GoldenTweaks.MODID);

    public static final DeferredHolder<MountedItemStorageType<?>, SophisticatedStorageMountedStorageType> SOPHISTICATED_STORAGE =
            MOUNTED_STORAGE_TYPES.register("sophisticated_storage", SophisticatedStorageMountedStorageType::new);

    private static final String[] STORAGE_BLOCKS = {
            "chest", "copper_chest", "iron_chest", "gold_chest", "diamond_chest", "netherite_chest",
            "barrel", "copper_barrel", "iron_barrel", "gold_barrel", "diamond_barrel", "netherite_barrel",
            "limited_barrel_1", "limited_barrel_2", "limited_barrel_3", "limited_barrel_4",
            "limited_copper_barrel_1", "limited_copper_barrel_2", "limited_copper_barrel_3", "limited_copper_barrel_4",
            "limited_iron_barrel_1", "limited_iron_barrel_2", "limited_iron_barrel_3", "limited_iron_barrel_4",
            "limited_gold_barrel_1", "limited_gold_barrel_2", "limited_gold_barrel_3", "limited_gold_barrel_4",
            "limited_diamond_barrel_1", "limited_diamond_barrel_2", "limited_diamond_barrel_3", "limited_diamond_barrel_4",
            "limited_netherite_barrel_1", "limited_netherite_barrel_2", "limited_netherite_barrel_3", "limited_netherite_barrel_4",
            "shulker_box", "copper_shulker_box", "iron_shulker_box", "gold_shulker_box", "diamond_shulker_box", "netherite_shulker_box"
    };

    private static boolean associated;

    private SSCreateCompat() {
    }

    public static void register(IEventBus modEventBus) {
        MOUNTED_STORAGE_TYPES.register(modEventBus);
        SsStorageMenus.MENU_TYPES.register(modEventBus);
        modEventBus.addListener(SSCreateCompat::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(SSCreateCompat::associateBlocks);
    }

    private static void associateBlocks() {
        if (associated) {
            return;
        }
        associated = true;
        MountedItemStorageType<?> type = SOPHISTICATED_STORAGE.get();
        int registered = 0;
        for (String id : STORAGE_BLOCKS) {
            Block block = BuiltInRegistries.BLOCK.get(
                    ResourceLocation.fromNamespaceAndPath("sophisticatedstorage", id));
            if (block == null) {
                continue;
            }
            try {
                MountedItemStorageType.REGISTRY.register(block, type);
                registered++;
            } catch (IllegalArgumentException alreadyRegistered) {
                GoldenTweaks.LOGGER.warn("Mounted storage type already registered for {}", id);
            }
        }
        GoldenTweaks.LOGGER.info("Registered contraption storage type for {} Sophisticated Storage blocks", registered);
    }
}
