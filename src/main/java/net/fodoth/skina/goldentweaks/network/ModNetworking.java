package net.fodoth.skina.goldentweaks.network;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.network.packet.C2SSelectArcaneRecipePacket;
import net.fodoth.skina.goldentweaks.network.packet.C2SOpenSsContraptionStoragePacket;
import net.fodoth.skina.goldentweaks.network.packet.C2SPickupItemPacket;
import net.fodoth.skina.goldentweaks.network.packet.C2SSsContraptionAttackPacket;
import net.fodoth.skina.goldentweaks.network.packet.C2SUpdatePickupSettingsPacket;
import net.fodoth.skina.goldentweaks.network.packet.S2CConsumableSyncPacket;
import net.fodoth.skina.goldentweaks.network.packet.S2COpenMaterialBookPacket;
import net.fodoth.skina.goldentweaks.network.packet.S2CSsStorageRenderPacket;
import net.fodoth.skina.goldentweaks.network.packet.S2CProviderIconsPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = GoldenTweaks.MODID)
public class ModNetworking {


    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {

        PayloadRegistrar registrar = event.registrar("2");

        registrar.playToServer(
                C2SPickupItemPacket.TYPE,
                C2SPickupItemPacket.STREAM_CODEC,
                C2SPickupItemPacket::handle
        );

        registrar.playToServer(
                C2SUpdatePickupSettingsPacket.TYPE,
                C2SUpdatePickupSettingsPacket.STREAM_CODEC,
                C2SUpdatePickupSettingsPacket::handle
        );

        registrar.playToServer(
                C2SSelectArcaneRecipePacket.TYPE,
                C2SSelectArcaneRecipePacket.STREAM_CODEC,
                C2SSelectArcaneRecipePacket::handle
        );

        registrar.playToServer(
                C2SSsContraptionAttackPacket.TYPE,
                C2SSsContraptionAttackPacket.STREAM_CODEC,
                C2SSsContraptionAttackPacket::handle
        );

        registrar.playToServer(
                C2SOpenSsContraptionStoragePacket.TYPE,
                C2SOpenSsContraptionStoragePacket.STREAM_CODEC,
                C2SOpenSsContraptionStoragePacket::handle
        );

        registrar.playToClient(
                S2COpenMaterialBookPacket.TYPE,
                S2COpenMaterialBookPacket.STREAM_CODEC,
                S2COpenMaterialBookPacket::handle
        );

        registrar.playToClient(
                S2CSsStorageRenderPacket.TYPE,
                S2CSsStorageRenderPacket.STREAM_CODEC,
                S2CSsStorageRenderPacket::handle
        );

        registrar.playToClient(
                S2CConsumableSyncPacket.TYPE,
                S2CConsumableSyncPacket.STREAM_CODEC,
                S2CConsumableSyncPacket::handle
        );

        registrar.playToClient(
                S2CProviderIconsPacket.TYPE,
                S2CProviderIconsPacket.STREAM_CODEC,
                S2CProviderIconsPacket::handle
        );
    }
}
