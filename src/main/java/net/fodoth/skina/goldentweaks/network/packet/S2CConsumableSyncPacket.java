package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableData;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record S2CConsumableSyncPacket(
        int entityId,
        GoldenTweaksConsumableData data
) implements CustomPacketPayload {

    public static final Type<S2CConsumableSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "consumable_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CConsumableSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    S2CConsumableSyncPacket::entityId,

                    ByteBufCodecs.COMPOUND_TAG,
                    pkt -> GoldenTweaksConsumableHelper.toNBT(pkt.data),

                    (id, tag) -> new S2CConsumableSyncPacket(
                            id,
                            GoldenTweaksConsumableHelper.fromNBT(tag)
                    )
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CConsumableSyncPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {

            var level = ctx.player().level();
            Entity e = level.getEntity(pkt.entityId());

            if (e == null) return;

            if (e instanceof net.alshanex.familiarslib.entity.AbstractSpellCastingPet pet) {

                GoldenTweaksConsumableHelper.applyClientData(pet, pkt.data);
            }
        });
    }
}