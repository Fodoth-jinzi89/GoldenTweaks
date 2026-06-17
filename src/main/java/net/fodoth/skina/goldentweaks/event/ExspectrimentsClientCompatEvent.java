package net.fodoth.skina.goldentweaks.event;

import io.github.chromonym.exspectriments.ExspModelLayers;
import io.github.chromonym.exspectriments.armor.LabCoatModel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ExspectrimentsClientCompatEvent {

    @SubscribeEvent
    public static void registerLayers(
            EntityRenderersEvent.RegisterLayerDefinitions event
    ) {

        event.registerLayerDefinition(
                ExspModelLayers.LAB_COAT,
                LabCoatModel::getTexturedModelData
        );
    }
}
