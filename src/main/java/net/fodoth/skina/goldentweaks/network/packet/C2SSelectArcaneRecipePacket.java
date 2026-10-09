package net.fodoth.skina.goldentweaks.network.packet;

import io.netty.buffer.ByteBuf;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.polymorph.PolymorphArcaneCompat;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * 客户端把玩家在奥术工作台上选中的多态配方 id 回传服务端。
 *
 * <p>Polymorph 自带的玩家选择包会用 {@code RecipeManager.byKey} 还原配方，而奥术配方并不在
 * {@code RecipeManager} 里，所以这里用 GoldenTweaks 自己的通道写回选择。</p>
 */
public record C2SSelectArcaneRecipePacket(ResourceLocation recipe) implements CustomPacketPayload {

    public static final Type<C2SSelectArcaneRecipePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "select_arcane_recipe"));

    public static final StreamCodec<ByteBuf, C2SSelectArcaneRecipePacket> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(
                    C2SSelectArcaneRecipePacket::new,
                    C2SSelectArcaneRecipePacket::recipe
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SSelectArcaneRecipePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PolymorphArcaneCompat.applySelection(player, packet.recipe());
            }
        });
    }
}
