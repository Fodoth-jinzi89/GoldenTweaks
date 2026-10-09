package net.fodoth.skina.goldentweaks.network.packet;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SophisticatedStorageMountedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/** 客户端从设置界面返回时，请求服务端重新打开装置上的挂载存储菜单。 */
public record C2SOpenSsContraptionStoragePacket(int entityId, BlockPos localPos) implements CustomPacketPayload {

    public static final Type<C2SOpenSsContraptionStoragePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "open_ss_contraption_storage"));

    public static final StreamCodec<FriendlyByteBuf, C2SOpenSsContraptionStoragePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SOpenSsContraptionStoragePacket::entityId,
            BlockPos.STREAM_CODEC, C2SOpenSsContraptionStoragePacket::localPos,
            C2SOpenSsContraptionStoragePacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SOpenSsContraptionStoragePacket packet, IPayloadContext context) {
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
            StructureBlockInfo info = contraption.getBlocks().get(packet.localPos());
            if (info == null) {
                return;
            }
            if (!(contraption.getStorage().getAllItemStorages().get(packet.localPos()) instanceof SophisticatedStorageMountedStorage mounted)) {
                return;
            }
            mounted.handleInteraction(player, contraption, info);
        });
    }
}
