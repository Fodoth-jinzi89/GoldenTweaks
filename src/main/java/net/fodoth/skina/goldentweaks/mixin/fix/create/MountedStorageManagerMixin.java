package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import com.simibubi.create.content.contraptions.MountedStorageSyncPacket;
import java.util.Map;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.ItemStorageReplacer;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SophisticatedStorageMountedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MountedStorageManager.class)
public abstract class MountedStorageManagerMixin implements ItemStorageReplacer {

    @Shadow
    private Map<BlockPos, MountedItemStorage> itemsBuilder;

    // 挂载存储的编解码拿不到 HolderLookup.Provider，而把 SS 的升级/设置 NBT 装回影子 BE 需要它。
    // read/handleSync 里解码出的存储此时还没有影子 BE（getSlots() 会返回 0），而随后 initialize() 会用
    // getSlots() 构建 CombinedInvWrapper 的槽位映射——若在 initialize 之后才补影子，映射里我们的存储就是
    // 0 槽，自动化（漏斗/溜槽等）根本够不到它（手动界面直连影子，所以只有自动化 I/O 失效）。
    // 因此在 initialize() 调用前就把影子建好。
    @Inject(method = "read", at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/content/contraptions/MountedStorageManager;initialize()V"))
    private void gt$ensureShadowsBeforeInitializeRead(CompoundTag nbt, HolderLookup.Provider registries,
                                                      boolean clientPacket, Contraption contraption, CallbackInfo ci) {
        gt$ensureSophisticatedStorageShadows(registries);
    }

    @Inject(method = "handleSync", at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/content/contraptions/MountedStorageManager;initialize()V"))
    private void gt$ensureShadowsBeforeInitializeSync(MountedStorageSyncPacket packet, AbstractContraptionEntity entity,
                                                      CallbackInfo ci) {
        gt$ensureSophisticatedStorageShadows(entity.level().registryAccess());
    }

    @Unique
    private void gt$ensureSophisticatedStorageShadows(HolderLookup.Provider registries) {
        if (itemsBuilder == null) {
            return;
        }
        for (MountedItemStorage storage : itemsBuilder.values()) {
            if (storage instanceof SophisticatedStorageMountedStorage sophisticatedStorage) {
                sophisticatedStorage.ensureShadow(registries);
            }
        }
    }

    @Invoker("reset")
    abstract void gt$reset();

    @Invoker("addStorage")
    abstract void gt$addItemStorage(MountedItemStorage storage, BlockPos pos);

    @Invoker("addStorage")
    abstract void gt$addFluidStorage(MountedFluidStorage storage, BlockPos pos);

    // Create 的 allItemStorages 是不可变表，没有「替换单条」的公开 API。这里借用私有的
    // reset()/addStorage()/initialize()：保留其余物品与流体存储，替换指定位置的物品存储后重建派生结构。
    @Override
    public void gt$replaceItemStorage(BlockPos pos, MountedItemStorage replacement) {
        MountedStorageManager manager = (MountedStorageManager) (Object) this;
        ImmutableMap<BlockPos, MountedItemStorage> items = manager.getAllItemStorages();
        ImmutableMap<BlockPos, MountedFluidStorage> fluids = manager.getFluids().storages;
        gt$reset();
        items.forEach((itemPos, storage) -> {
            if (!itemPos.equals(pos)) {
                gt$addItemStorage(storage, itemPos);
            }
        });
        fluids.forEach((fluidPos, storage) -> gt$addFluidStorage(storage, fluidPos));
        gt$addItemStorage(replacement, pos);
        manager.initialize();
    }
}
