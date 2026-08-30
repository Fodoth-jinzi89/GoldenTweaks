package net.fodoth.skina.goldentweaks.compat.lootr;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.fodoth.skina.goldentweaks.util.ItemPickupUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
import java.util.UUID;

/**
 * Lootr 快速拾取（按住右键持续拿取战利品）的事件处理。
 * <p>
 * 玩家右键 Lootr 容器时不会打开原版界面，而是直接生成物品实体并吸入玩家背包；
 * 持续按住右键会按配置的间隔自动拾取，直到容器被清空或玩家停止交互。
 * <p>
 * 该事件类仅在检测到 Lootr 加载时由 {@link GoldenTweaks} 注册。
 */
public final class LootrQuickLootEvent {

    /** 会话至少要持续的最小 tick 数，避免把短暂的点击误判为“已停止交互”。 */
    private static final long MIN_OPEN_TICKS = 10L;

    /** 开箱动画完成所需的最小 tick 数，首次拾取要等盖子完全打开后再触发。 */
    private static final long OPEN_ANIMATION_TICKS = 10L;

    /** 取空容器后，等待物品飞出再关箱的延迟 tick 数。 */
    private static final long CLOSE_DELAY_TICKS = 5L;

    /** 进行中的快速拾取会话，键为 (玩家 UUID, 容器坐标)。 */
    private static final Map<Key, Session> SESSIONS = new HashMap<>();

    /** 右键 Lootr 容器：开始新会话，或刷新已有会话。 */
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        // 功能开关、主手、服务端检查
        if (!GoldenTweaksCommonConfig.LOOTR_QUICK_LOOT.get()
                || event.getHand() != InteractionHand.MAIN_HAND
                || event.getLevel().isClientSide()) {
            return;
        }
        BlockEntity container = event.getLevel().getBlockEntity(event.getPos());
        // 只处理有自定义“已开启”状态的容器（箱子/桶/潜影盒），
        // 装饰陶罐、刷子方块等不参与快速拾取，避免后续强转 LootrOpenedStateAccess 失败。
        if (!(container instanceof ILootrBlockEntity lootr)
                || !(container instanceof LootrOpenedStateAccess)
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // 拦截原版打开容器逻辑
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        long now = player.level().getGameTime();
        Key key = new Key(player, event.getPos());
        Session session = SESSIONS.get(key);

        if (session == null || session.container != container) {
            // 玩家换了一个容器：先收掉旧会话
            if (session != null) {
                close(session, session.emptied || session.wasOpened);
                SESSIONS.remove(key);
            }
            session = new Session(container, player, now, isLooted(container));
            SESSIONS.put(key, session);
            open(container, lootr, player);
            // 首次拾取由 onServerTick 在开箱动画播完后触发，这里不立即蹦物品
            return;
        }

        // 持续右键：刷新输入时间
        session.lastInput = now;
        if (session.lastPickup == Long.MIN_VALUE) {
            // 开箱动画尚未完成，等待 tick 触发首次拾取
            return;
        }
        if (now - session.lastPickup < GoldenTweaksCommonConfig.LOOTR_HOLD_PICKUP_INTERVAL.get()) {
            return;
        }
        session.lastPickup = now;
        handlePickup(session, container, lootr, player, event.getPos());
    }

    /** 服务端 tick：等待动画后首次拾取、按间隔自动拾取、超时/取空后关箱。 */
    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onServerTick(ServerTickEvent.Post event) {
        long interval = Math.max(5L, GoldenTweaksCommonConfig.LOOTR_HOLD_PICKUP_INTERVAL.get());
        for (Map.Entry<Key, Session> entry : SESSIONS.entrySet().toArray(Map.Entry[]::new)) {
            Session session = entry.getValue();
            long now = session.player.level().getGameTime();
            long sinceInput = now - session.lastInput;
            long sinceOpened = now - session.openedAt;

            // 已取空：延迟几 tick 让物品先飞出，再关箱
            if (session.closeAt >= 0 && now >= session.closeAt) {
                close(session, true);
                SESSIONS.remove(entry.getKey());
                continue;
            }

            // 首次拾取：等开箱动画播完再触发（无论玩家是否仍在输入）
            if (session.lastPickup == Long.MIN_VALUE) {
                if (now >= session.firstLootAt) {
                    session.lastPickup = now;
                    handlePickup(session, session.container,
                            (ILootrBlockEntity) session.container, session.player, session.container.getBlockPos());
                }
                continue;
            }

            // 后续拾取：玩家仍在按住右键且到达拾取间隔
            if (sinceInput <= interval && now - session.lastPickup >= interval) {
                session.lastPickup = now;
                handlePickup(session, session.container,
                        (ILootrBlockEntity) session.container, session.player, session.container.getBlockPos());
            }

            // 输入超时：关闭会话（取空的会话由 closeAt 负责关闭）
            // 只要不再持续右键就关闭，避免大间隔配置下点按后箱子长时间不关
            if (sinceInput > MIN_OPEN_TICKS && sinceOpened >= MIN_OPEN_TICKS) {
                close(session, session.emptied || session.wasOpened);
                SESSIONS.remove(entry.getKey());
            }
        }
    }

    /** 该容器是否已被 GoldenTweaks 标记为已开启（用于判断关闭后是否恢复“已开启”外观）。 */
    private static boolean isLooted(BlockEntity container) {
        return container instanceof LootrOpenedStateAccess access && access.gt$isOpened();
    }

    /** 拾取一批；若本次取空容器，则标记已开启并延迟关箱。 */
    private static void handlePickup(Session session, BlockEntity container,
                                     ILootrBlockEntity lootr, ServerPlayer player, BlockPos pos) {
        LootResult result = loot(container, lootr, player, pos);
        if (result.taken() > 0 && result.exhausted()) {
            markOpened(container, lootr, player, pos);
            session.emptied = true;
            session.closeAt = session.player.level().getGameTime() + CLOSE_DELAY_TICKS;
        }
    }

    /** 打开容器：先清掉客户端“已开启”外观，再播放开盖动画。 */
    private static void open(BlockEntity container, ILootrBlockEntity lootr, ServerPlayer player) {
        // 重置客户端“已开启”外观，确保以普通贴图播放开盖动画
        lootr.performClose(player);
        // 开盖动画期间不要把 Lootr 标记为已开启，取完全部物品后再标记
        lootr.setHasBeenOpened(false);
        playAnimation(container, player.level(), true);
    }

    /** 关闭容器：播放关盖动画，并按是否已清空来恢复“已开启”外观。 */
    private static void close(Session session, boolean opened) {
        ILootrBlockEntity lootr = (ILootrBlockEntity) session.container;
        playAnimation(session.container, session.player.level(), false);
        if (opened) {
            lootr.performOpen(session.player);
        } else {
            lootr.performClose(session.player);
        }
        lootr.updatePacketViaForce();
        session.container.setChanged();
    }

    /** 容器被清空：持久化自定义已开启状态，并同步服务端数据。 */
    private static void markOpened(BlockEntity container, ILootrBlockEntity lootr, ServerPlayer player, BlockPos pos) {
        ((LootrOpenedStateAccess) container).gt$markOpened(player.getUUID());
        ILootrInfoProvider provider = ILootrInfoProvider.of(pos, player.level());
        provider.setHasBeenOpened(true);
        provider.markDataChanged();
        lootr.performUpdate(player);
        lootr.updatePacketViaForce();
        container.setChanged();
        player.level().sendBlockUpdated(pos, player.level().getBlockState(pos), player.level().getBlockState(pos), 3);
    }

    /**
     * 播放开/关盖动画与音效。
     * <p>
     * 不用 {@code startOpen}/{@code stopOpen}（ContainerOpenersCounter）控制：
     * 快速拾取没有打开容器菜单，vanilla 的 openers recheck 会在约 20 tick 后自动关盖。
     * 这里直接给客户端发方块事件（箱子/潜影盒）或改方块状态（桶）来控制动画。
     */
    private static void playAnimation(BlockEntity container, Level level, boolean open) {
        BlockPos pos = container.getBlockPos();
        BlockState state = level.getBlockState(pos);
        SoundEvent sound;
        if (container instanceof LootrChestBlockEntity chest) {
            chest.triggerEvent(1, open ? 1 : 0);
            level.blockEvent(pos, state.getBlock(), 1, open ? 1 : 0);
            sound = open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE;
        } else if (container instanceof LootrShulkerBlockEntity shulker) {
            shulker.triggerEvent(1, open ? 1 : 0);
            level.blockEvent(pos, state.getBlock(), 1, open ? 1 : 0);
            sound = open ? SoundEvents.SHULKER_BOX_OPEN : SoundEvents.SHULKER_BOX_CLOSE;
        } else if (container instanceof LootrBarrelBlockEntity) {
            if (state.hasProperty(BarrelBlock.OPEN)) {
                level.setBlock(pos, state.setValue(BarrelBlock.OPEN, open), 3);
            }
            sound = open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE;
        } else {
            return;
        }
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    /** 从 Lootr 容器取出至多一组物品，生成掉落物并吸入玩家。返回本次结果。 */
    private static LootResult loot(BlockEntity container, ILootrBlockEntity lootr, ServerPlayer player, BlockPos pos) {
        ILootrInfoProvider provider;
        ILootrInventory inventory;
        try {
            provider = ILootrInfoProvider.of(pos, player.level());
            if (container instanceof LootrChestBlockEntity chest) {
                chest.unpackLootTable(player);
            } else if (container instanceof LootrBarrelBlockEntity barrel) {
                barrel.unpackLootTable(player);
            } else if (container instanceof LootrShulkerBlockEntity shulker) {
                shulker.unpackLootTable(player);
            }
            inventory = LootrAPI.getInventory(provider, player, DefaultLootFiller.getInstance());
        } catch (Throwable ignored) {
            return new LootResult(0, false);
        }
        if (inventory == null) {
            return new LootResult(0, false);
        }

        int limit = GoldenTweaksCommonConfig.LOOTR_PICKUP_GROUPS.get();
        int interval = GoldenTweaksCommonConfig.LOOTR_HOLD_PICKUP_INTERVAL.get();
        // 拾取间隔越短，一次拿取的数量越少，避免“手速过快瞬间清空容器”
        if (limit != 0 && interval < 5) {
            limit = Math.min(64, limit * ((5 + interval - 1) / interval));
        }

        int taken = 0;
        for (int slot = 0; slot < inventory.getContainerSize() && (limit == 0 || taken < limit); slot++) {
            var stack = inventory.removeItemNoUpdate(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemEntity item = new ItemEntity(player.level(), pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, stack);
            player.level().addFreshEntity(item);
            if (GoldenTweaksCommonConfig.LOOTR_SHOW_FLYING_ITEMS.get()) {
                ItemPickupUtil.pullToPlayer(player, item);
            } else {
                ItemPickupUtil.pickup(player, item);
            }
            taken++;
        }
        inventory.setChanged();

        // 检查容器是否已被清空
        boolean exhausted = true;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) {
                exhausted = false;
                break;
            }
        }
        provider.performTrigger(player);
        provider.performUpdate(player);
        lootr.updatePacketViaForce();
        container.setChanged();
        player.level().sendBlockUpdated(pos, player.level().getBlockState(pos), player.level().getBlockState(pos), 3);
        return new LootResult(taken, exhausted);
    }

    /** 拾取结果。 */
    private record LootResult(int taken, boolean exhausted) {
    }

    /** 会话键：玩家 + 不可变容器坐标。 */
    private record Key(UUID player, BlockPos pos) {
        Key(ServerPlayer player, BlockPos pos) {
            this(player.getUUID(), pos.immutable());
        }
    }

    /** 单个玩家的快速拾取会话状态。 */
    private static final class Session {
        final BlockEntity container;
        final ServerPlayer player;
        final long openedAt;
        final boolean wasOpened;
        final long firstLootAt;
        long lastInput;
        long lastPickup = Long.MIN_VALUE;
        long closeAt = -1;
        boolean emptied;

        Session(BlockEntity container, ServerPlayer player, long now, boolean wasOpened) {
            this.container = container;
            this.player = player;
            this.openedAt = now;
            this.wasOpened = wasOpened;
            this.firstLootAt = now + OPEN_ANIMATION_TICKS;
            this.lastInput = now;
        }
    }

    private LootrQuickLootEvent() {
    }
}
