package net.fodoth.skina.goldentweaks.network;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.network.packet.C2SPickupItemPacket;
import net.fodoth.skina.goldentweaks.network.packet.S2COpenMaterialBookPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = GoldenTweaks.MODID)
public class ModNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {

        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                C2SPickupItemPacket.TYPE,
                C2SPickupItemPacket.STREAM_CODEC,
                C2SPickupItemPacket::handle
        );

        registrar.playToClient(
                S2COpenMaterialBookPacket.TYPE,
                S2COpenMaterialBookPacket.STREAM_CODEC,
                S2COpenMaterialBookPacket::handle
        );
    }
}
