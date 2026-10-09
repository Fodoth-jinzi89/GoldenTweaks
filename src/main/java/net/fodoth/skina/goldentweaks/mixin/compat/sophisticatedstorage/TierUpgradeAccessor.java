package net.fodoth.skina.goldentweaks.mixin.compat.sophisticatedstorage;

import java.util.Optional;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 暴露 SS {@code StorageTierUpgradeItem$TierUpgrade} 的私有映射方法
 * {@code getBlockUpgradeDefinition(Block)}，用于在装置上自行求 tier 升级的目标方块。
 */
@Mixin(targets = "net.p3pp3rf1y.sophisticatedstorage.item.StorageTierUpgradeItem$TierUpgrade", remap = false)
public interface TierUpgradeAccessor {

    @Invoker("getBlockUpgradeDefinition")
    Optional<?> gt$getBlockUpgradeDefinition(Block block);
}
