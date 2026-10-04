package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.mixin.feature.vanilla.accessor.ItemEntityAccessor;
import net.fodoth.skina.goldentweaks.network.PickupSettings;
import net.fodoth.skina.goldentweaks.network.PickupSettingsManager;
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
                // 使用该玩家同步过来的设定（已裁剪到合理范围），各玩家配置独立
                PickupSettings settings = PickupSettingsManager.get(sp.getUUID());
                // 总开关未开启时不处理任何拾取请求
                if (!settings.enablePickup()) {
                    return;
                }
                // 潜行与非潜行分别控制是否可以右键拾取
                boolean sneakAllowed = sp.isShiftKeyDown()
                        ? settings.allowSneakPickup()
                        : settings.allowStandingPickup();
                if (!sneakAllowed) {
                    return;
                }
                var level = sp.level();
                var e = level.getEntity(pkt.entityId());
                if (e instanceof ExperienceOrb orb) {
                    if (settings.allowExperienceOrbPickup()
                            && sp.distanceTo(orb) <= settings.maxReach()) {
                        orb.playerTouch(sp);
                    }
                } else if (e instanceof ItemEntity item) {
                    // 背包放不下且未开启「背包已满时能够右键拾取」时不拾取（创造模式不受限）
                    if (!settings.allowFullInventoryPickup()
                            && !sp.getAbilities().instabuild
                            && !ItemPickupUtil.canFit(sp, item.getItem())) {
                        return;
                    }
                    int delay = ((ItemEntityAccessor) item).goldentweaks$getPickupDelay();
                    boolean canPickup = settings.allowInfiniteDelay()
                            || delay <= settings.pickupDelayThreshold();
                    if (canPickup && sp.distanceTo(item) <= settings.maxReach()) {
                        ItemPickupUtil.pickup(sp, item);
                    }
                }
            }
        });
    }
}
