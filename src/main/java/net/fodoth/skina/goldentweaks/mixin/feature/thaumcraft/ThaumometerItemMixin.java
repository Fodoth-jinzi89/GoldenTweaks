package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.ThaumometerStorageScanQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import thaumcraft.common.items.TCItemBehaviors;
import thaumcraft.common.research.TCResearchManager;
import thaumcraft.common.research.ThaumometerScanManager;

@Mixin(TCItemBehaviors.ThaumometerItem.class)
public abstract class ThaumometerItemMixin {

    @WrapOperation(
            method = "lambda$use$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/research/ThaumometerScanManager;hasBeenScanned(Lnet/minecraft/world/entity/player/Player;Lthaumcraft/common/research/ThaumometerScanManager$ScanTarget;)Z"
            )
    )
    private static boolean gt$allowRescanningContainersInUse(
            Player player,
            ThaumometerScanManager.ScanTarget target,
            Operation<Boolean> original
    ) {
        return gt$allowRescanningContainers(player, target, original);
    }

    @WrapOperation(
            method = "onUseTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/research/ThaumometerScanManager;hasBeenScanned(Lnet/minecraft/world/entity/player/Player;Lthaumcraft/common/research/ThaumometerScanManager$ScanTarget;)Z"
            )
    )
    private boolean gt$allowRescanningContainersOnUseTick(
            Player player,
            ThaumometerScanManager.ScanTarget target,
            Operation<Boolean> original
    ) {
        return gt$allowRescanningContainers(player, target, original);
    }

    private static boolean gt$allowRescanningContainers(
            Player player,
            ThaumometerScanManager.ScanTarget target,
            Operation<Boolean> original
    ) {
        if (TCResearchManager.has(player, ThaumometerStorageScanQueue.RESEARCH_KEY)
                && gt$hasExtendedContents(player, target)) {
            return false;
        }
        return original.call(player, target);
    }

    private static boolean gt$hasExtendedContents(Player player, ThaumometerScanManager.ScanTarget target) {
        if (target.type() == ThaumometerScanManager.ScanType.ENTITY) {
            return target.key().startsWith("ITEM:");
        }
        if (target.type() != ThaumometerScanManager.ScanType.BLOCK) {
            return false;
        }

        BlockPos pos = BlockPos.containing(target.effectX(), target.effectY(), target.effectZ());
        return player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null) != null;
    }
}
