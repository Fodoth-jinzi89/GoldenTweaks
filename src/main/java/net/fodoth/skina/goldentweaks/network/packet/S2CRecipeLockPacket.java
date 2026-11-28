package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTRecipeLocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tells the client which recipe a machine has pinned, or that the pin was released.
 *
 * <p>Sent on the transitions only — locked and unlocked — because the id does not change while the
 * lock holds. The tooltip reads it from {@link GTRecipeLocks}, so the screen never touches the block
 * entity on the client.</p>
 */
public record S2CRecipeLockPacket(BlockPos pos, @Nullable ResourceLocation recipe)
        implements CustomPacketPayload {

    public static final Type<S2CRecipeLockPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    GoldenTweaks.MODID, "recipe_lock"));

    public static final StreamCodec<FriendlyByteBuf, S2CRecipeLockPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public S2CRecipeLockPacket decode(FriendlyByteBuf buffer) {
                    BlockPos pos = buffer.readBlockPos();
                    // A leading flag keeps "no lock" explicit instead of relying on a sentinel id.
                    ResourceLocation recipe = buffer.readBoolean()
                            ? buffer.readResourceLocation()
                            : null;
                    return new S2CRecipeLockPacket(pos, recipe);
                }

                @Override
                public void encode(FriendlyByteBuf buffer, S2CRecipeLockPacket packet) {
                    buffer.writeBlockPos(packet.pos());
                    buffer.writeBoolean(packet.recipe() != null);
                    if (packet.recipe() != null) {
                        buffer.writeResourceLocation(packet.recipe());
                    }
                }
            };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CRecipeLockPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.recipe() == null) {
                GTRecipeLocks.remove(packet.pos().asLong());
            } else {
                GTRecipeLocks.put(packet.pos().asLong(), packet.recipe());
            }
        });
    }
}
