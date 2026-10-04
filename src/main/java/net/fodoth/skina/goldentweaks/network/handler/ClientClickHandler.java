package net.fodoth.skina.goldentweaks.network.handler;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.network.PickupSettings;
import net.fodoth.skina.goldentweaks.network.packet.C2SPickupItemPacket;
import net.fodoth.skina.goldentweaks.network.packet.C2SUpdatePickupSettingsPacket;
import net.fodoth.skina.goldentweaks.util.GetEntityHitResult;
import net.fodoth.skina.goldentweaks.util.ItemPickupUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(
        modid = GoldenTweaks.MODID,
        value = Dist.CLIENT
)
public final class ClientClickHandler {

    private static int holdTickCounter = 0;
    private static boolean rightDown = false;

    /** 最近一次同步给服务端的拾取设定；为 null 表示本次会话尚未同步。 */
    private static PickupSettings lastSyncedSettings = null;

    // =========================
    // Settings sync
    // =========================
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        // 登录后重新同步一次，避免沿用上一次会话的缓存
        lastSyncedSettings = null;
    }

    // =========================
    // Mouse input tracking
    // =========================
    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {

        if (event.getButton() != 1) {
            return;
        }

        // 松开一定要处理，且必须放在界面判断之前：
        // 打开容器界面时如果吞掉松开事件，rightDown 会卡在 true，
        // 关界面的那一刻就会触发连续拾取，表现为“没按右键也被拾取”。
        if (event.getAction() == 0) {
            rightDown = false;
            holdTickCounter = 0;
            return;
        }

        if (event.getAction() != 1) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        // 打开任意界面（背包、物品栏等）时不触发右键拾取，也不拦截界面内的点击
        if (mc.screen != null) {
            return;
        }

        LocalPlayer player = mc.player;

        rightDown = true;

        boolean picked = tryPickup(mc);

        ItemStack mainHand = player != null
                ? player.getMainHandItem()
                : ItemStack.EMPTY;

        // ✔ 关键修复：只在“发生拾取”时阻止 use
        if (picked && (mainHand.isEmpty()
                || GoldenTweaksClientConfig.BLOCK_USE.get())) {

            event.setCanceled(true);
        }
    }

    // =========================
    // Tick continuous pickup
    // =========================
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) {
            return;
        }

        // 拾取设定是每个玩家独立的，变化时同步给服务端
        syncSettings(player);

        if (mc.screen != null) {
            // 界面打开期间彻底复位：关界面的瞬间不会残留按住状态而误触发连续拾取
            rightDown = false;
            holdTickCounter = 0;
            return;
        }

        if (!rightDown) {
            holdTickCounter = 0;
            return;
        }

        if (!GoldenTweaksClientConfig.ALLOW_CONTINUOUS_PICKUP.get()) {
            holdTickCounter = 0;
            return;
        }

        holdTickCounter++;

        if (holdTickCounter < GoldenTweaksClientConfig.CONTINUOUS_PICKUP_INTERVAL.get()) {
            return;
        }

        holdTickCounter = 0;

        tryPickup(mc);
    }

    // =========================
    // Settings
    // =========================
    /** 设定变化（或本次会话尚未同步）时，把客户端配置发给服务端。 */
    private static void syncSettings(LocalPlayer player) {

        PickupSettings settings = readSettings(player);

        if (settings.equals(lastSyncedSettings)) {
            return;
        }

        lastSyncedSettings = settings;

        PacketDistributor.sendToServer(
                new C2SUpdatePickupSettingsPacket(settings)
        );
    }

    /** 读取客户端拾取配置，构造要同步给服务端的设定。 */
    private static PickupSettings readSettings(LocalPlayer player) {

        double maxReach = GoldenTweaksClientConfig.BASE_PICKUP_REACH.get();

        if (player.isCreative() || player.isSpectator()) {
            maxReach += GoldenTweaksClientConfig.EXTENDED_REACH_BONUS.get();
        }

        return new PickupSettings(
                GoldenTweaksClientConfig.ENABLE_PICKUP.get(),
                GoldenTweaksClientConfig.ALLOW_STANDING_PICKUP.get(),
                GoldenTweaksClientConfig.ALLOW_SNEAK_PICKUP.get(),
                GoldenTweaksClientConfig.ALLOW_FULL_INVENTORY_PICKUP.get(),
                maxReach,
                GoldenTweaksClientConfig.ALLOW_INFINITE_DELAY.get(),
                GoldenTweaksClientConfig.PICKUP_DELAY_THRESHOLD.get(),
                GoldenTweaksClientConfig.ALLOW_EXPERIENCE_ORB_PICKUP.get(),
                GoldenTweaksClientConfig.LOOTR_QUICK_LOOT.get(),
                GoldenTweaksClientConfig.LOOTR_PLAY_OPEN_ANIMATION.get(),
                GoldenTweaksClientConfig.LOOTR_SHOW_FLYING_ITEMS.get(),
                GoldenTweaksClientConfig.LOOTR_PICKUP_GROUPS.get(),
                GoldenTweaksClientConfig.LOOTR_HOLD_PICKUP_INTERVAL.get()
        );
    }

    // =========================
    // Pickup logic
    // =========================
    private static boolean tryPickup(Minecraft mc) {

        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) {
            return false;
        }

        // 总开关关闭时不进行任何右键拾取
        if (!GoldenTweaksClientConfig.ENABLE_PICKUP.get()) {
            return false;
        }

        // 界面打开时（含连续拾取以外的触发路径）一律不拾取
        if (mc.screen != null) {
            return false;
        }

        // 潜行与非潜行分别控制是否允许右键拾取
        boolean allowWhileSneaking = player.isShiftKeyDown()
                ? GoldenTweaksClientConfig.ALLOW_SNEAK_PICKUP.get()
                : GoldenTweaksClientConfig.ALLOW_STANDING_PICKUP.get();

        if (!allowWhileSneaking) {
            return false;
        }

        float pt = mc.getTimer().getGameTimeDeltaPartialTick(true);

        Vec3 eye = player.getEyePosition(pt);
        Vec3 look = player.getViewVector(pt);

        double maxReach = readSettings(player).maxReach();

        Vec3 reachEnd = eye.add(look.scale(maxReach));

        AABB searchBox =
                player.getBoundingBox()
                        .expandTowards(look.scale(maxReach))
                        .inflate(GoldenTweaksClientConfig.SEARCH_BOX_INFLATE.get());

        boolean allowOrbs = GoldenTweaksClientConfig.ALLOW_EXPERIENCE_ORB_PICKUP.get();

        // 背包已满且未开启「背包已满时能够右键拾取」时，跳过放不下的物品（创造模式不受限）
        boolean requireSpace = !GoldenTweaksClientConfig.ALLOW_FULL_INVENTORY_PICKUP.get()
                && !player.getAbilities().instabuild;

        List<GetEntityHitResult.TraceHit> hits =
                GetEntityHitResult.traceEntities(
                        player,
                        eye,
                        reachEnd,
                        searchBox,
                        e -> {
                            if (e instanceof ItemEntity item
                                    && item.isAlive()
                                    && !item.hasPickUpDelay()) {
                                return !requireSpace || ItemPickupUtil.canFit(player, item.getItem());
                            }
                            return allowOrbs
                                    && e instanceof ExperienceOrb orb
                                    && orb.isAlive();
                        },
                        GoldenTweaksClientConfig.TRACE_BIAS.get(),
                        GoldenTweaksClientConfig.ALLOW_THROUGH_WALLS.get(),
                        GoldenTweaksClientConfig.MAX_TARGETS.get()
                );

        if (hits.isEmpty()) {
            return false;
        }

        for (GetEntityHitResult.TraceHit hit : hits) {
            PacketDistributor.sendToServer(
                    new C2SPickupItemPacket(hit.entity().getId())
            );
        }

        player.swing(InteractionHand.MAIN_HAND);

        return true;
    }

    private ClientClickHandler() {
    }
}
