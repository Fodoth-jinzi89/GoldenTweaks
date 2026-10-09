package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;

public final class SsStorageMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, GoldenTweaks.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SsStorageMenu>> STORAGE =
            MENU_TYPES.register("ss_storage", () -> IMenuTypeExtension.create(SsStorageMenus::createClientMenu));

    public static final DeferredHolder<MenuType<?>, MenuType<SsStorageSettingsMenu>> SETTINGS =
            MENU_TYPES.register("ss_storage_settings", () -> IMenuTypeExtension.create(SsStorageMenus::createClientSettingsMenu));

    private SsStorageMenus() {
    }

    public static MenuType<?> storageType() {
        return STORAGE.get();
    }

    public static MenuType<?> settingsType() {
        return SETTINGS.get();
    }

    private static SsStorageMenu createClientMenu(int windowId, Inventory inventory, RegistryFriendlyByteBuf data) {
        int entityId = data.readVarInt();
        BlockPos localPos = data.readBlockPos();
        return new SsStorageMenu(storageType(), windowId, inventory.player, resolveClientWrapper(inventory, entityId, localPos),
                entityId, localPos);
    }

    private static SsStorageSettingsMenu createClientSettingsMenu(int windowId, Inventory inventory, RegistryFriendlyByteBuf data) {
        int entityId = data.readVarInt();
        BlockPos localPos = data.readBlockPos();
        return new SsStorageSettingsMenu(settingsType(), windowId, inventory.player, resolveClientWrapper(inventory, entityId, localPos),
                entityId, localPos);
    }

    private static IStorageWrapper resolveClientWrapper(Inventory inventory, int entityId, BlockPos localPos) {
        if (inventory.player.level().getEntity(entityId) instanceof AbstractContraptionEntity contraptionEntity) {
            BlockEntity blockEntity = contraptionEntity.getContraption()
                    .getOrCreateClientContraptionLazy()
                    .getBlockEntity(localPos);
            if (blockEntity instanceof StorageBlockEntity storageBlockEntity) {
                return storageBlockEntity.getStorageWrapper();
            }
        }
        return NoopStorageWrapper.INSTANCE;
    }
}
