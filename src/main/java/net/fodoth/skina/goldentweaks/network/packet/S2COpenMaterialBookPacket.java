package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.network.handler.MaterialBookClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record S2COpenMaterialBookPacket() implements CustomPacketPayload {
    public static final Type<S2COpenMaterialBookPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            GoldenTweaks.MODID,
                            "open_material_book"
                    )
            );

    public static final StreamCodec<FriendlyByteBuf, S2COpenMaterialBookPacket> STREAM_CODEC =
            StreamCodec.unit(new S2COpenMaterialBookPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * S2C 处理器：实际界面逻辑在客户端专用的
     * {@link MaterialBookClientHandler}，这里只是协议壳 + 主线程调度。
     * <p>
     * 注意不要在 common 侧直接写 {@code net.minecraft.client.*}：该 packet 类在服务端也会被加载
     * （注册与发送都要它），一旦类里出现客户端类型，专用服务器上就会触发
     * {@code NoClassDefFoundError}。
     */
    public static void handle(S2COpenMaterialBookPacket payload, IPayloadContext context) {
        context.enqueueWork(MaterialBookClientHandler::openMaterialBook);
    }
}