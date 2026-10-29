package net.fodoth.skina.goldentweaks.compat.academy.network;

import com.mohistmc.academy.skill.AcademyAttachments;
import com.mohistmc.academy.skill.PlayerAbilityData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端请求把某个曲目从玩家的「已加载媒体」里移除（媒体播放器界面的「删除歌曲」按钮使用）。
 *
 * <p>曲目列表本身来自 {@code PlayerAbilityData.getLoadedMedia()}，因此移除后曲目会自动从播放器列表消失，
 * 且对应的 {@code MediaItem} 不再显示「已加载」，可以重新加载。</p>
 */
public record GTMediaUnloadPacket(String trackId) implements CustomPacketPayload {

    public static final Type<GTMediaUnloadPacket> TYPE = new Type(
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "media_unload"));

    public static final StreamCodec<ByteBuf, GTMediaUnloadPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), GTMediaUnloadPacket::trackId, GTMediaUnloadPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GTMediaUnloadPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            PlayerAbilityData data = player.getData(AcademyAttachments.PLAYER_ABILITY);
            if (data.getLoadedMedia().remove(packet.trackId())) {
                data.syncTo(player);
            }
        });
    }
}
