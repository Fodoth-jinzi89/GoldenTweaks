package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.ae2autopatternupload.ProviderIconCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record S2CProviderIconsPacket(List<String> names, List<ItemStack> icons) implements CustomPacketPayload {
    public static final Type<S2CProviderIconsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "provider_icons"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CProviderIconsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            S2CProviderIconsPacket::names,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()),
            S2CProviderIconsPacket::icons,
            S2CProviderIconsPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CProviderIconsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ProviderIconCache.set(packet.names, packet.icons));
    }
}
