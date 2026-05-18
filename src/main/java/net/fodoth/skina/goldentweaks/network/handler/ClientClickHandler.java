package net.fodoth.skina.goldentweaks.network.handler;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.fodoth.skina.goldentweaks.network.packet.C2SPickupItemPacket;
import net.fodoth.skina.goldentweaks.util.GetEntityHitResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class ClientClickHandler {

    private static int holdTickCounter;
    private static boolean rightDown;

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {

        if (event.getButton() != 1) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (event.getAction() == 1) {

            rightDown = true;

            boolean picked = tryPickup(mc);

            ItemStack mainHand = player != null
                    ? player.getMainHandItem()
                    : ItemStack.EMPTY;

            if (picked && (mainHand.isEmpty()
                    || GoldenTweaksCommonConfig.BLOCK_USE.get())) {

                event.setCanceled(true);
            }

            return;
        }

        if (event.getAction() == 0) {
            rightDown = false;
            holdTickCounter = 0;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null
                || mc.level == null
                || mc.screen != null) {

            return;
        }

        if (!rightDown) {
            holdTickCounter = 0;
            return;
        }

        if (++holdTickCounter
                < GoldenTweaksCommonConfig.CONTINUOUS_PICKUP_INTERVAL.get()) {

            return;
        }

        holdTickCounter = 0;

        tryPickup(mc);
    }

    private static boolean tryPickup(Minecraft mc) {

        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) {
            return false;
        }

        if (player.isShiftKeyDown()
                && !GoldenTweaksCommonConfig.ALLOW_SNEAK_PICKUP.get()) {

            return false;
        }

        float pt = mc.getTimer().getGameTimeDeltaPartialTick(true);

        Vec3 eye = player.getEyePosition(pt);
        Vec3 look = player.getViewVector(pt);

        double maxReach = getMaxReach(player);

        Vec3 reachEnd = eye.add(look.scale(maxReach));

        AABB searchBox = player.getBoundingBox()
                .expandTowards(look.scale(maxReach))
                .inflate(GoldenTweaksCommonConfig.SEARCH_BOX_INFLATE.get());

        List<GetEntityHitResult.TraceHit> hits =
                GetEntityHitResult.traceEntities(
                        player,
                        eye,
                        reachEnd,
                        searchBox,
                        e -> e instanceof ItemEntity item
                                && item.isAlive()
                                && !item.hasPickUpDelay(),
                        GoldenTweaksCommonConfig.TRACE_BIAS.get(),
                        GoldenTweaksCommonConfig.ALLOW_THROUGH_WALLS.get(),
                        GoldenTweaksCommonConfig.MAX_TARGETS.get()
                );

        if (hits.isEmpty()) {
            return false;
        }

        hits.forEach(hit ->
                PacketDistributor.sendToServer(
                        new C2SPickupItemPacket(hit.entity().getId())
                )
        );

        player.swing(InteractionHand.MAIN_HAND);

        return true;
    }

    public static double getMaxReach(Player player) {

        return GoldenTweaksCommonConfig.BASE_PICKUP_REACH.get()
                + ((player.isCreative() || player.isSpectator())
                ? GoldenTweaksCommonConfig.EXTENDED_REACH_BONUS.get()
                : 0.0D);
    }

    private ClientClickHandler() {
    }
}