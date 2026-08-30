package net.fodoth.skina.goldentweaks.compat.lootr;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.fodoth.skina.goldentweaks.util.ItemPickupUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.DefaultLootFiller;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import noobanidus.mods.lootr.common.api.data.inventory.ILootrInventory;
import noobanidus.mods.lootr.common.block.entity.LootrBarrelBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrChestBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrShulkerBlockEntity;

import java.util.HashMap;
import java.util.Map;

public final class LootrQuickLootEvent {
    private static final long MIN_OPEN_TICKS = 10L;
    private static final Map<Key, Session> SESSIONS = new HashMap<>();

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!GoldenTweaksCommonConfig.LOOTR_QUICK_LOOT.get()
                || event.getHand() != InteractionHand.MAIN_HAND
                || event.getLevel().isClientSide()) return;
        BlockEntity container = event.getLevel().getBlockEntity(event.getPos());
        if (!(container instanceof ILootrBlockEntity lootr)
                || !(event.getEntity() instanceof ServerPlayer player)) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        long now = player.level().getGameTime();
        Key key = new Key(player, event.getPos());
        Session session = SESSIONS.get(key);
        GoldenTweaks.LOGGER.info("[LootrQuickLoot] right-click player={} pos={} tick={} session={} lastPickup={} lastInput={}",
                player.getGameProfile().getName(), event.getPos(), now, session == null ? "new" : "existing",
                session == null ? "n/a" : session.lastPickup, session == null ? "n/a" : session.lastInput);
        if (session == null || session.container != container) {
            session = new Session(container, player, now,
                    ((LootrOpenedStateAccess) container).gt$hasOpened(player.getUUID()));
            SESSIONS.put(key, session);
            open(container, lootr, player);
            GoldenTweaks.LOGGER.info("[LootrQuickLoot] session opened player={} pos={}", player.getGameProfile().getName(), event.getPos());
            return;
        } else {
            session.lastInput = now;
            GoldenTweaks.LOGGER.debug("[LootrQuickLoot] session input refreshed player={} pos={} tick={}", player.getGameProfile().getName(), event.getPos(), now);
        }
        if (session.lastPickup != Long.MIN_VALUE
                && now - session.lastPickup < GoldenTweaksCommonConfig.LOOTR_HOLD_PICKUP_INTERVAL.get()) {
            GoldenTweaks.LOGGER.debug("[LootrQuickLoot] pickup skipped due interval player={} pos={} delta={} interval={}",
                    player.getGameProfile().getName(), event.getPos(), now - session.lastPickup,
                    GoldenTweaksCommonConfig.LOOTR_HOLD_PICKUP_INTERVAL.get());
            return;
        }
        session.lastPickup = now;
        GoldenTweaks.LOGGER.info("[LootrQuickLoot] pickup triggered player={} pos={} tick={}", player.getGameProfile().getName(), event.getPos(), now);
        boolean exhausted = loot(container, lootr, player, event.getPos());
        persistOpened((LootrOpenedStateAccess) container, player, exhausted, event.getPos());
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onServerTick(ServerTickEvent.Post event) {
        long interval = Math.max(5L, GoldenTweaksCommonConfig.LOOTR_HOLD_PICKUP_INTERVAL.get());
        for (Map.Entry<Key, Session> entry : SESSIONS.entrySet().toArray(Map.Entry[]::new)) {
            Session session = entry.getValue();
            long sessionNow = session.player.level().getGameTime();
            long sinceInput = sessionNow - session.lastInput;
            long sinceOpened = sessionNow - session.openedAt;
            if (sinceInput > Math.max(MIN_OPEN_TICKS, interval + 1L)
                    && sessionNow - session.openedAt >= MIN_OPEN_TICKS) {
                GoldenTweaks.LOGGER.info("[LootrQuickLoot] session closed after input timeout player={} pos={} tick={} sinceInput={}",
                        session.player.getGameProfile().getName(), session.container.getBlockPos(), sessionNow, sinceInput);
                close(session);
                SESSIONS.remove(entry.getKey());
                continue;
            }
            if (sinceOpened >= MIN_OPEN_TICKS
                    && sinceInput <= interval && sessionNow - session.lastPickup >= interval) {
                session.lastPickup = sessionNow;
                GoldenTweaks.LOGGER.info("[LootrQuickLoot] scheduled pickup player={} pos={} tick={} sinceInput={} sincePickup={}",
                        session.player.getGameProfile().getName(), session.container.getBlockPos(), sessionNow,
                        sinceInput, sessionNow - session.lastPickup);
                boolean exhausted = loot(session.container, (ILootrBlockEntity) session.container, session.player,
                        session.container.getBlockPos());
                ILootrBlockEntity lootr = (ILootrBlockEntity) session.container;
                persistOpened((LootrOpenedStateAccess) session.container, session.player, exhausted, session.container.getBlockPos());
            }
        }
    }

    private static void open(BlockEntity container, ILootrBlockEntity lootr, ServerPlayer player) {
        GoldenTweaks.LOGGER.debug("[LootrQuickLoot] open container={} player={}", container.getBlockPos(), player.getGameProfile().getName());
        lootr.performOpen(player);
        if (container instanceof LootrChestBlockEntity chest) chest.startOpen(player);
        else if (container instanceof LootrBarrelBlockEntity barrel) barrel.startOpen(player);
        else if (container instanceof LootrShulkerBlockEntity shulker) shulker.startOpen(player);
    }

    private static void close(Session session) {
        GoldenTweaks.LOGGER.debug("[LootrQuickLoot] close container={} player={}", session.container.getBlockPos(), session.player.getGameProfile().getName());
        ILootrBlockEntity lootr = (ILootrBlockEntity) session.container;
        lootr.performClose(session.player);
        switch (session.container) {
            case LootrChestBlockEntity chest -> chest.stopOpen(session.player);
            case LootrBarrelBlockEntity barrel -> barrel.stopOpen(session.player);
            case LootrShulkerBlockEntity shulker -> shulker.stopOpen(session.player);
            default -> {
            }
        }
        lootr.updatePacketViaForce();
        session.container.setChanged();
    }

    private static void persistOpened(LootrOpenedStateAccess state, ServerPlayer player, boolean exhausted, BlockPos pos) {
        if (!exhausted) return;
        state.gt$markOpened(player.getUUID());
        GoldenTweaks.LOGGER.info("[LootrQuickLoot] opened-state kept/persisted pos={} exhausted={}",
                pos, exhausted);
    }

    private static boolean loot(BlockEntity container, ILootrBlockEntity lootr, ServerPlayer player, BlockPos pos) {
        ILootrInfoProvider provider;
        ILootrInventory inventory;
        try {
            provider = ILootrInfoProvider.of(pos, player.level());
            if (container instanceof LootrChestBlockEntity chest) chest.unpackLootTable(player);
            else if (container instanceof LootrBarrelBlockEntity barrel) barrel.unpackLootTable(player);
            else if (container instanceof LootrShulkerBlockEntity shulker) shulker.unpackLootTable(player);
            inventory = LootrAPI.getInventory(provider, player, DefaultLootFiller.getInstance());
        } catch (Throwable ignored) {
            GoldenTweaks.LOGGER.warn("[LootrQuickLoot] loot generation failed player={} pos={}",
                    player.getGameProfile().getName(), pos, ignored);
            return false;
        }
        if (inventory == null) {
            GoldenTweaks.LOGGER.warn("[LootrQuickLoot] inventory is null player={} pos={}", player.getGameProfile().getName(), pos);
            return false;
        }
        int limit = GoldenTweaksCommonConfig.LOOTR_PICKUP_GROUPS.get();
        int interval = GoldenTweaksCommonConfig.LOOTR_HOLD_PICKUP_INTERVAL.get();
        if (limit != 0 && interval < 5) limit = Math.min(64, limit * ((5 + interval - 1) / interval));
        int taken = 0;
        for (int slot = 0; slot < inventory.getContainerSize() && (limit == 0 || taken < limit); slot++) {
            var stack = inventory.removeItemNoUpdate(slot);
            if (stack.isEmpty()) continue;
            ItemEntity item = new ItemEntity(player.level(), pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, stack);
            player.level().addFreshEntity(item);
            if (GoldenTweaksCommonConfig.LOOTR_SHOW_FLYING_ITEMS.get()) ItemPickupUtil.pullToPlayer(player, item);
            else ItemPickupUtil.pickup(player, item);
            taken++;
        }
        inventory.setChanged();
        boolean exhausted = true;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) {
                exhausted = false;
                break;
            }
        }
        GoldenTweaks.LOGGER.info("[LootrQuickLoot] loot result player={} pos={} taken={} limit={} exhausted={}",
                player.getGameProfile().getName(), pos, taken, limit, exhausted);
        // Keep the opened flag sticky: partial pickup must not reset it, while
        // exhausting the inventory persists the state for future saves/loads.
        if (exhausted) {
            provider.setHasBeenOpened(true);
            provider.markDataChanged();
            GoldenTweaks.LOGGER.info("[LootrQuickLoot] opened-state persisted pos={}", pos);
        }
        provider.performTrigger(player);
        provider.performUpdate(player);
        lootr.updatePacketViaForce();
        container.setChanged();
        player.level().sendBlockUpdated(pos, player.level().getBlockState(pos), player.level().getBlockState(pos), 3);
        return exhausted;
    }

    private record Key(java.util.UUID player, BlockPos pos) {
        Key(ServerPlayer player, BlockPos pos) { this(player.getUUID(), pos.immutable()); }
    }

    private static final class Session {
        final BlockEntity container;
        final ServerPlayer player;
        final long openedAt;
        final boolean initiallyOpened;
        long lastInput;
        long lastPickup = Long.MIN_VALUE;
        Session(BlockEntity container, ServerPlayer player, long now, boolean initiallyOpened) {
            this.container = container;
            this.player = player;
            this.openedAt = now;
            this.initiallyOpened = initiallyOpened;
            this.lastInput = now;
        }
    }

    private LootrQuickLootEvent() {}
}
