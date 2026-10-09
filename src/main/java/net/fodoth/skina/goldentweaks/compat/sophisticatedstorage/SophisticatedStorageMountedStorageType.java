package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;

public class SophisticatedStorageMountedStorageType extends MountedItemStorageType<SophisticatedStorageMountedStorage> {

    public SophisticatedStorageMountedStorageType() {
        super(SophisticatedStorageMountedStorage.CODEC);
    }

    @Nullable
    @Override
    public SophisticatedStorageMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity blockEntity) {
        if (!(blockEntity instanceof StorageBlockEntity storageBlockEntity)) {
            return null;
        }
        HolderLookup.Provider registries = level.registryAccess();
        CompoundTag tag = storageBlockEntity.saveWithoutMetadata(registries);
        SophisticatedStorageMountedStorage storage = new SophisticatedStorageMountedStorage(state, tag);
        storage.ensureShadow(registries);
        return storage;
    }
}
