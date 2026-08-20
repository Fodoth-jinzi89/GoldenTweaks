package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.neoecoae.NeoEcoAeStorageCompat;
import net.fodoth.skina.goldentweaks.compat.mekanism.MekanismQIOScanCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.common.network.TCNetwork;
import thaumcraft.common.research.TCResearchManager;
import thaumcraft.common.research.ThaumometerScanManager;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ThaumometerStorageScanQueue {

    private static final int SCANS_PER_TICK = 32;
    public static final String RESEARCH_KEY = "GT_THAUMIC_MICROSCOPY";
    private static final Map<UUID, ScanJob> JOBS = new HashMap<>();
    private static final ThreadLocal<Boolean> BATCHING = ThreadLocal.withInitial(() -> false);

    private ThaumometerStorageScanQueue() {
    }

    public static boolean enqueue(ServerPlayer player, ItemStack stack) {
        if (!isUnlocked(player) || !isStorageMedium(stack)) {
            return false;
        }

        ScanJob job = JOBS.computeIfAbsent(player.getUUID(), ignored -> new ScanJob());
        ItemStack medium = stack.copyWithCount(1);
        if (job.media.stream().noneMatch(queued -> ItemStack.isSameItemSameComponents(queued, medium))) {
            job.media.add(medium);
        }
        return true;
    }

    public static boolean isBatching() {
        return BATCHING.get();
    }

    public static boolean isUnlocked(ServerPlayer player) {
        return TCResearchManager.has(player, RESEARCH_KEY);
    }

    public static void setBatching(boolean batching) {
        if (batching) {
            BATCHING.set(true);
        } else {
            BATCHING.remove();
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<UUID, ScanJob>> jobs = JOBS.entrySet().iterator();
        while (jobs.hasNext()) {
            Map.Entry<UUID, ScanJob> entry = jobs.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || !process(player, entry.getValue())) {
                jobs.remove();
            }
        }
    }

    private static boolean process(ServerPlayer player, ScanJob job) {
        if (!isUnlocked(player)) {
            return false;
        }
        int processed = 0;
        boolean changed = false;
        BATCHING.set(true);
        try {
            while (processed < SCANS_PER_TICK) {
                if (job.items == null || !job.items.hasNext()) {
                    if (!loadNextMedium(job)) {
                        break;
                    }
                }

                ItemStack stack = job.items.next();
                processed++;
                var target = ThaumometerScanManager.itemTarget(stack);
                if (target.isPresent() && job.scanKeys.add(target.get().key())) {
                    changed |= ThaumometerScanManager.completeScan(player, target.get());
                }
            }
        } finally {
            BATCHING.remove();
        }

        if (changed) {
            TCNetwork.sendScanSync(player);
            TCNetwork.sendResearchSync(player, false);
        }
        return job.items != null && job.items.hasNext() || !job.media.isEmpty();
    }

    private static boolean loadNextMedium(ScanJob job) {
        while (!job.media.isEmpty()) {
            ItemStack medium = job.media.removeFirst();
            if (ModList.get().isLoaded("mekanism") && MekanismQIOScanCompat.isDrive(medium)) {
                job.items = MekanismQIOScanCompat.getContents(medium);
                if (job.items.hasNext()) {
                    return true;
                }
                continue;
            }

            StorageCell cell = getCellInventory(medium);
            if (cell == null) {
                continue;
            }

            KeyCounter contents = cell.getAvailableStacks();
            if (!contents.isEmpty()) {
                job.items = contents.keySet().stream()
                        .filter(AEItemKey.class::isInstance)
                        .map(AEItemKey.class::cast)
                        .map(AEItemKey::getReadOnlyStack)
                        .iterator();
                return true;
            }
        }
        job.items = null;
        return false;
    }

    private static boolean isStorageMedium(ItemStack stack) {
        try {
            return StorageCells.isCellHandled(stack)
                    || ModList.get().isLoaded("neoecoae") && NeoEcoAeStorageCompat.isStorageCell(stack)
                    || ModList.get().isLoaded("mekanism") && MekanismQIOScanCompat.isDrive(stack);
        } catch (RuntimeException e) {
            GoldenTweaks.LOGGER.warn("Failed to inspect storage cell {}", stack.getHoverName().getString(), e);
            return false;
        }
    }

    private static StorageCell getCellInventory(ItemStack stack) {
        try {
            StorageCell cell = StorageCells.getCellInventory(stack, null);
            if (cell == null && ModList.get().isLoaded("neoecoae")) {
                cell = NeoEcoAeStorageCompat.getCellInventory(stack);
            }
            return cell;
        } catch (RuntimeException e) {
            GoldenTweaks.LOGGER.warn("Failed to read storage cell {}", stack.getHoverName().getString(), e);
            return null;
        }
    }

    private static final class ScanJob {
        private final ArrayDeque<ItemStack> media = new ArrayDeque<>();
        private final Set<String> scanKeys = new HashSet<>();
        private Iterator<ItemStack> items;
    }
}
