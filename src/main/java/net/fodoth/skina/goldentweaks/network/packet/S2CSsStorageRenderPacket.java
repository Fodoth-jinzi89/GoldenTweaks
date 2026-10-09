package net.fodoth.skina.goldentweaks.network.packet;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;

/**
 * 服务端把移动结构上 SS 容器的渲染信息（物品显示/数量/填充）推给客户端 BE。
 *
 * <p>itemsChanged 只能在服务端线程生效，客户端也没有世界方块 BE 的常规更新包，所以外观必须由服务端
 * 算好后同步。这是装置上外观刷新的<b>唯一</b>写入方（客户端 afterSync 不再改 renderInfo），
 * 避免两路互写造成闪动。</p>
 */
public record S2CSsStorageRenderPacket(int entityId, BlockPos localPos, CompoundTag renderInfo) implements CustomPacketPayload {

    public static final Type<S2CSsStorageRenderPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "ss_storage_render"));

    public static final StreamCodec<FriendlyByteBuf, S2CSsStorageRenderPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, S2CSsStorageRenderPacket::entityId,
            BlockPos.STREAM_CODEC, S2CSsStorageRenderPacket::localPos,
            ByteBufCodecs.COMPOUND_TAG, S2CSsStorageRenderPacket::renderInfo,
            S2CSsStorageRenderPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CSsStorageRenderPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level().getEntity(packet.entityId()) instanceof AbstractContraptionEntity contraptionEntity)) {
                return;
            }
            Contraption contraption = contraptionEntity.getContraption();
            if (contraption == null) {
                return;
            }
            if (contraption.getBlockEntityClientSide(packet.localPos()) instanceof StorageBlockEntity storageBlockEntity) {
                storageBlockEntity.getStorageWrapper().getRenderInfo().deserializeFrom(packet.renderInfo());
                // 显示物是按方块模型（DISPLAY_ITEMS model data）烘焙进装置缓存的，光改 renderInfo 不会重建，
                // 必须让 Create 失效该装置的结构缓存，重画时才会带上新的物品图标。
                contraption.invalidateClientContraptionStructure();
            }
        });
    }
}
