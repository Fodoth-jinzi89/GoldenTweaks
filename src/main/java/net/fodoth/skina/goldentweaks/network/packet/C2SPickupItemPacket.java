package net.fodoth.skina.goldentweaks.network.packet;


import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record C2SPickupItemPacket(int entityId) implements CustomPacketPayload {

    public static final Type<C2SPickupItemPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "pickup_item"));

    public static final StreamCodec<FriendlyByteBuf, C2SPickupItemPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    C2SPickupItemPacket::entityId,
                    C2SPickupItemPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SPickupItemPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();

            if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                var level = sp.level();
                var e = level.getEntity(pkt.entityId());

                if (e instanceof net.minecraft.world.entity.item.ItemEntity item) {

                    if (sp.distanceTo(item) <= 6.0D) {

                        var stack = item.getItem();
                        var copy = stack.copy();

                        if (sp.getInventory().add(stack)) {

                            sp.awardStat(net.minecraft.stats.Stats.ITEM_PICKED_UP.get(copy.getItem()), copy.getCount());
                            sp.take(item, copy.getCount());

                            if (stack.isEmpty()) {
                                item.discard();
                            }
                        }
                    }
                }
            }
        });
    }
}