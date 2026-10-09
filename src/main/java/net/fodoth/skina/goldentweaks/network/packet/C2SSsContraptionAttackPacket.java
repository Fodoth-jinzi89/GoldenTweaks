package net.fodoth.skina.goldentweaks.network.packet;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SophisticatedStorageMountedStorage;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SsContraptionInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/** 客户端在移动结构上左键 SS 限类桶时回传，服务端执行「取出一份」。 */
public record C2SSsContraptionAttackPacket(int entityId, BlockPos localPos, Direction side) implements CustomPacketPayload {

    public static final Type<C2SSsContraptionAttackPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "ss_contraption_attack"));

    public static final StreamCodec<FriendlyByteBuf, C2SSsContraptionAttackPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SSsContraptionAttackPacket::entityId,
            BlockPos.STREAM_CODEC, C2SSsContraptionAttackPacket::localPos,
            Direction.STREAM_CODEC, C2SSsContraptionAttackPacket::side,
            C2SSsContraptionAttackPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SSsContraptionAttackPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.level().getEntity(packet.entityId()) instanceof AbstractContraptionEntity contraptionEntity)) {
                return;
            }
            Contraption contraption = contraptionEntity.getContraption();
            if (contraption == null) {
                return;
            }
            if (!(contraption.getStorage().getAllItemStorages().get(packet.localPos()) instanceof SophisticatedStorageMountedStorage mounted)) {
                return;
            }
            SsContraptionInteraction.takeItem(player, contraption, mounted, packet.localPos(), packet.side());
        });
    }
}
