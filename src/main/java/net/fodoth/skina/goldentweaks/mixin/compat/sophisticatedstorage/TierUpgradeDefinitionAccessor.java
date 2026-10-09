package net.fodoth.skina.goldentweaks.mixin.compat.sophisticatedstorage;

import java.util.List;
import net.minecraft.world.level.block.state.properties.Property;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 暴露 SS {@code StorageTierUpgradeItem$TierUpgradeDefinition}（私有内部类）的
 * {@code newBlock()} 与 {@code getPropertiesToCopy()}，用于在装置上合成升级后的方块状态。
 */
@Mixin(targets = "net.p3pp3rf1y.sophisticatedstorage.item.StorageTierUpgradeItem$TierUpgradeDefinition", remap = false)
public interface TierUpgradeDefinitionAccessor {

    @Invoker("newBlock")
    StorageBlockBase gt$newBlock();

    @Invoker("getPropertiesToCopy")
    List<Property<?>> gt$getPropertiesToCopy();
}
