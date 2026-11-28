package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTReactionProducts;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTReactionStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The products of the reaction a machine is currently running.
 *
 * <p>Everything the GUI needs is resolved on the server and sent as plain data: each output with its
 * per-craft amount, plus how many ticks one craft takes. The screen therefore never touches a recipe
 * class — it just draws what it is handed, so another machine with its own recipe type can reuse the
 * same list.</p>
 *
 * <p>An empty list means "nothing running", which is what stops the GUI listing output rates.</p>
 */
public record S2CReactionProductsPacket(BlockPos pos, List<GTReactionStack> outputs, int processingTime)
        implements CustomPacketPayload {

    public static final Type<S2CReactionProductsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    GoldenTweaks.MODID, "reaction_products"));

    public static final StreamCodec<FriendlyByteBuf, S2CReactionProductsPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public S2CReactionProductsPacket decode(FriendlyByteBuf buffer) {
                    BlockPos pos = buffer.readBlockPos();
                    int processingTime = buffer.readVarInt();
                    List<GTReactionStack> outputs = buffer.readList(GTReactionStack.STREAM_CODEC);
                    return new S2CReactionProductsPacket(pos, outputs, processingTime);
                }

                @Override
                public void encode(FriendlyByteBuf buffer, S2CReactionProductsPacket packet) {
                    buffer.writeBlockPos(packet.pos());
                    buffer.writeVarInt(packet.processingTime());
                    buffer.writeCollection(packet.outputs(), GTReactionStack.STREAM_CODEC);
                }
            };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CReactionProductsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.outputs().isEmpty()) {
                GTReactionProducts.put(packet.pos().asLong(), List.of());
                return;
            }
            List<GTReactionProducts.Line> lines = new ArrayList<>(packet.outputs().size());
            for (GTReactionStack output : packet.outputs()) {
                lines.add(new GTReactionProducts.Line(
                        output, output.amount(), packet.processingTime()));
            }
            GTReactionProducts.put(packet.pos().asLong(), lines);
        });
    }
}
