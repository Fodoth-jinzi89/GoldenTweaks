package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.network.PickupSettings;
import net.fodoth.skina.goldentweaks.network.PickupSettingsManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * 客户端把当前的右键拾取设定同步给服务端。
 * <p>
 * 玩家登录后以及设定变化时发送；服务端保存并按 {@link PickupSettings#clamped()} 裁剪后使用，
 * 从而让每个玩家的配置彼此独立。
 */
public record C2SUpdatePickupSettingsPacket(PickupSettings settings) implements CustomPacketPayload {

    public static final Type<C2SUpdatePickupSettingsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "update_pickup_settings"));

    public static final StreamCodec<FriendlyByteBuf, C2SUpdatePickupSettingsPacket> STREAM_CODEC =
            PickupSettings.STREAM_CODEC.map(
                    C2SUpdatePickupSettingsPacket::new,
                    C2SUpdatePickupSettingsPacket::settings
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SUpdatePickupSettingsPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                PickupSettingsManager.update(player.getUUID(), pkt.settings());
            }
        });
    }
}
