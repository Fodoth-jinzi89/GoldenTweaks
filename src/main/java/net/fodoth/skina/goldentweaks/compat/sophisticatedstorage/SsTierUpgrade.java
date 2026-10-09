package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import java.util.List;
import java.util.Optional;
import net.fodoth.skina.goldentweaks.mixin.compat.sophisticatedstorage.TierUpgradeAccessor;
import net.fodoth.skina.goldentweaks.mixin.compat.sophisticatedstorage.TierUpgradeDefinitionAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageTierUpgradeItem;

/**
 * 把 SS 的 tier 升级物品（基础→铁等）应用到移动装置上的存储方块。
 *
 * <p>SS 原生的升级逻辑全程基于世界方块（{@code level.getBlockEntity(pos)}），装置上没有世界方块，
 * 所以这里自行完成「求目标方块 → 迁移 wrapper NBT → 替换装置方块 → 重建挂载存储」。</p>
 */
public final class SsTierUpgrade {

    private SsTierUpgrade() {
    }

    /** @return 是否成功升级并消耗物品 */
    public static boolean tryUpgrade(ServerPlayer player, AbstractContraptionEntity entity, Contraption contraption,
                                     SophisticatedStorageMountedStorage mounted, StructureBlockInfo info, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof StorageTierUpgradeItem tierUpgradeItem)) {
            return false;
        }
        StorageBlockEntity shadow = mounted.getShadow();
        if (shadow == null) {
            return false;
        }
        BlockState state = mounted.getState();
        Object tier = tierUpgradeItem.getTier();
        Optional<?> definition = ((TierUpgradeAccessor) tier).gt$getBlockUpgradeDefinition(state.getBlock());
        if (definition.isEmpty()) {
            return false;
        }
        TierUpgradeDefinitionAccessor def = (TierUpgradeDefinitionAccessor) definition.get();
        StorageBlockBase newBlock = def.gt$newBlock();
        HolderLookup.Provider registries = player.serverLevel().registryAccess();

        BlockState newState = buildNewState(state, newBlock, def.gt$getPropertiesToCopy());
        CompoundTag migratedTag = buildMigratedTag(shadow, newBlock, info.pos(), newState, registries);

        // 替换装置上的方块（state 会随 ContraptionBlockChangedPacket 同步给客户端）
        entity.setBlock(info.pos(), new StructureBlockInfo(info.pos(), newState, migratedTag));

        // 重建该位置的挂载存储，并标记 dirty 让 Create 把实时内容同步给客户端
        SophisticatedStorageMountedStorage replacement = new SophisticatedStorageMountedStorage(newState, migratedTag);
        replacement.ensureShadow(registries);
        replacement.markDirty();
        ((ItemStorageReplacer) contraption.getStorage()).gt$replaceItemStorage(info.pos(), replacement);

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        return true;
    }

    private static BlockState buildNewState(BlockState source, StorageBlockBase newBlock, List<Property<?>> properties) {
        BlockState newState = newBlock.defaultBlockState();
        for (Property<?> property : properties) {
            newState = copyProperty(newState, source, property);
        }
        return newState;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState target, BlockState source, Property<T> property) {
        if (target.hasProperty(property) && source.hasProperty(property)) {
            return target.setValue(property, source.getValue(property));
        }
        return target;
    }

    /** 把旧方块的 wrapper/升级/设置 NBT 装进新 tier 的 BE，并按新 tier 的容量扩容。 */
    private static CompoundTag buildMigratedTag(StorageBlockEntity shadow, StorageBlockBase newBlock, BlockPos pos,
                                                BlockState newState, HolderLookup.Provider registries) {
        CompoundTag oldTag = shadow.saveWithoutMetadata(registries);
        StorageBlockEntity newShadow = newBlock.newBlockEntity(pos, newState);
        newShadow.setBeingUpgraded(true);
        newShadow.loadWithComponents(oldTag, registries);
        newShadow.changeStorageSize(
                newBlock.getNumberOfInventorySlots() - newShadow.getStorageWrapper().getInventoryHandler().getSlots(),
                newBlock.getNumberOfUpgradeSlots() - newShadow.getStorageWrapper().getUpgradeHandler().getSlots());
        newShadow.setBeingUpgraded(false);
        return newShadow.saveWithoutMetadata(registries);
    }
}
