package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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

    public static void handle(S2COpenMaterialBookPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> clazz = Class.forName(
                        "net.silentchaos512.gear.client.gui.book.MaterialBookScreen"
                );

                Object instance = clazz.getDeclaredConstructor().newInstance();

                if (instance instanceof Screen screen) {
                    Minecraft.getInstance().setScreen(screen);
                } else {
                    GoldenTweaks.LOGGER.warn(
                            "MaterialBookScreen is not a Screen instance"
                    );
                }
            } catch (Exception e) {
                GoldenTweaks.LOGGER.warn(
                        "Failed to open MaterialBookScreen",
                        e
                );
            }
        });
    }
}