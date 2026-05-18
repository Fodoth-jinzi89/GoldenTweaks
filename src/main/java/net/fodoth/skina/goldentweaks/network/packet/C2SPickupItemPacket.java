package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static net.fodoth.skina.goldentweaks.network.handler.ClientClickHandler.getMaxReach;

public record C2SPickupItemPacket(int entityId) implements CustomPacketPayload {

    public static final Type<C2SPickupItemPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    GoldenTweaks.MODID,
                    "pickup_item"
            ));

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

            if (!(player instanceof ServerPlayer sp)) {
                return;
            }

            var level = sp.level();
            var entity = level.getEntity(pkt.entityId());

            if (!(entity instanceof ItemEntity item)) {
                return;
            }

            if (!item.isAlive() || item.hasPickUpDelay()) {
                return;
            }

            if (sp.distanceTo(item) > getMaxReach(sp)) {
                return;
            }

            ItemStack stack = item.getItem();
            ItemStack original = stack.copy();

            sp.getInventory().add(stack);

            int insertedCount =
                    original.getCount() - stack.getCount();

            if (insertedCount > 0) {

                sp.awardStat(
                        Stats.ITEM_PICKED_UP.get(original.getItem()),
                        insertedCount
                );

                sp.take(item, insertedCount);

                if (stack.isEmpty()) {
                    item.discard();
                    return;
                }
            }
            item.setExtendedLifetime();
            Vec3 targetPos = sp.position().add(0.0D, 0.3D, 0.0D);

            Vec3 motion = targetPos.subtract(item.position())
                    .normalize()
                    .scale(0.35D);

            item.setDeltaMovement(motion);

            item.hasImpulse = true;
        });
    }
}