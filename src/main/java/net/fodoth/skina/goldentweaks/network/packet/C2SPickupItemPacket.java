package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.fodoth.skina.goldentweaks.mixin.feature.vanilla.accessor.ItemEntityAccessor;
import net.fodoth.skina.goldentweaks.util.ItemPickupUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record C2SPickupItemPacket(int entityId) implements CustomPacketPayload {
    public static final Type<C2SPickupItemPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "pickup_item"));
    public static final StreamCodec<FriendlyByteBuf, C2SPickupItemPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, C2SPickupItemPacket::entityId, C2SPickupItemPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SPickupItemPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player instanceof ServerPlayer sp) {
                var level = sp.level();
                var e = level.getEntity(pkt.entityId());
                if (e instanceof ExperienceOrb orb) {
                    if (GoldenTweaksCommonConfig.ALLOW_EXPERIENCE_ORB_PICKUP.get()
                            && sp.distanceTo(orb) <= ItemPickupUtil.getMaxReach(player)) {
                        orb.playerTouch(sp);
                    }
                } else if (e instanceof ItemEntity item) {
                    int delay = ((ItemEntityAccessor) item).goldentweaks$getPickupDelay();
                    boolean canPickup = GoldenTweaksCommonConfig.ALLOW_INFINITE_DELAY.get()
                            || delay <= GoldenTweaksCommonConfig.PICKUP_DELAY_THRESHOLD.get();
                    if (canPickup && sp.distanceTo(item) <= ItemPickupUtil.getMaxReach(player)) {
                        ItemPickupUtil.pickup(sp, item);
                    }
                }
            }
        });
    }
}
