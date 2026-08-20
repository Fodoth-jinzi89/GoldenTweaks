package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.ThaumometerStorageScanQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.network.TCNetwork;
import thaumcraft.common.research.ThaumometerScanManager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Mixin(ThaumometerScanManager.class)
public abstract class ThaumometerScanManagerMixin {

    @Unique
    private static final ThreadLocal<ItemStack> gt$SCANNED_ITEM = ThreadLocal.withInitial(() -> ItemStack.EMPTY);

    @Shadow
    private static Optional<ThaumometerScanManager.ScanTarget> itemTarget(ItemStack stack) {
        throw new AssertionError();
    }

    @Inject(method = "completeRequestedScan", at = @At("HEAD"))
    private static void gt$clearScannedItem(ServerPlayer player, String key, CallbackInfoReturnable<Boolean> cir) {
        gt$SCANNED_ITEM.remove();
    }

    @Inject(method = "entityTarget", at = @At("HEAD"))
    private static void gt$captureScannedItem(Entity entity, CallbackInfoReturnable<ThaumometerScanManager.ScanTarget> cir) {
        if (entity instanceof ItemEntity itemEntity) {
            gt$SCANNED_ITEM.set(itemEntity.getItem());
        }
    }

    @WrapOperation(
            method = "completeRequestedScan",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/research/ThaumometerScanManager;completeScan(Lnet/minecraft/world/entity/player/Player;Lthaumcraft/common/research/ThaumometerScanManager$ScanTarget;)Z"
            )
    )
    private static boolean gt$scanContainerContents(
            Player player,
            ThaumometerScanManager.ScanTarget target,
            Operation<Boolean> original
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return original.call(player, target);
        }

        if (!ThaumometerStorageScanQueue.isUnlocked(serverPlayer)) {
            gt$SCANNED_ITEM.remove();
            return original.call(player, target);
        }

        if (target.type() == ThaumometerScanManager.ScanType.ENTITY) {
            boolean scanned = original.call(player, target);
            boolean enqueued = ThaumometerStorageScanQueue.enqueue(serverPlayer, gt$SCANNED_ITEM.get());
            gt$SCANNED_ITEM.remove();
            return scanned || enqueued;
        }

        if (target.type() != ThaumometerScanManager.ScanType.BLOCK) {
            return original.call(player, target);
        }

        BlockPos pos = BlockPos.containing(target.effectX(), target.effectY(), target.effectZ());
        IItemHandler itemHandler = serverPlayer.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (itemHandler == null) {
            return original.call(player, target);
        }

        int slots = itemHandler.getSlots();
        Set<String> scannedKeys = new HashSet<>(Math.min(slots, 64));
        boolean scanned = original.call(player, target);
        boolean scannedContents = false;
        boolean enqueuedContents = false;
        ThaumometerStorageScanQueue.setBatching(true);
        try {
            for (int slot = 0; slot < slots; slot++) {
                ItemStack stack = itemHandler.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }

                Optional<ThaumometerScanManager.ScanTarget> itemTarget = itemTarget(stack);
                if (itemTarget.isPresent() && scannedKeys.add(itemTarget.get().key())) {
                    scannedContents |= ThaumometerScanManager.completeScan(serverPlayer, itemTarget.get());
                }
                enqueuedContents |= ThaumometerStorageScanQueue.enqueue(serverPlayer, stack);
            }
        } finally {
            ThaumometerStorageScanQueue.setBatching(false);
        }

        if (scannedContents) {
            TCNetwork.sendScanSync(serverPlayer);
            TCNetwork.sendResearchSync(serverPlayer, false);
        }
        return scanned || scannedContents || enqueuedContents;
    }

    @Redirect(
            method = "completeScan",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/network/TCNetwork;sendScanSync(Lnet/minecraft/server/level/ServerPlayer;)V"
            )
    )
    private static void gt$deferScanSync(ServerPlayer player) {
        if (!ThaumometerStorageScanQueue.isBatching()) {
            TCNetwork.sendScanSync(player);
        }
    }

    @Redirect(
            method = "completeScan",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/network/TCNetwork;sendResearchSync(Lnet/minecraft/server/level/ServerPlayer;Z)V"
            )
    )
    private static void gt$deferResearchSync(ServerPlayer player, boolean fullSync) {
        if (!ThaumometerStorageScanQueue.isBatching()) {
            TCNetwork.sendResearchSync(player, fullSync);
        }
    }

    @Redirect(
            method = "completeScan",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private static void gt$deferScanMessage(Player player, net.minecraft.network.chat.Component message, boolean actionBar) {
        if (!ThaumometerStorageScanQueue.isBatching()) {
            player.displayClientMessage(message, actionBar);
        }
    }
}
