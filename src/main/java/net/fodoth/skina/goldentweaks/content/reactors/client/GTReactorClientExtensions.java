package net.fodoth.skina.goldentweaks.content.reactors.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.reactors.GTReactorFluids;
import net.fodoth.skina.goldentweaks.content.reactors.GTReactorItems;
import net.fodoth.skina.goldentweaks.content.reactors.GTReactorMaterial;
import net.fodoth.skina.goldentweaks.content.reactors.GTReactorSolid;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;

@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTReactorClientExtensions {

    private GTReactorClientExtensions() {
    }

    @SubscribeEvent
    public static void register(RegisterClientExtensionsEvent event) {
        for (GTReactorMaterial material : GTReactorMaterial.values()) {
            if (material.hasFluid()) {
                event.registerFluidType(new TintedFluidExtensions(material.color()), GTReactorFluids.fluidType(material).get());
            }
        }
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (GTReactorMaterial material : GTReactorMaterial.values()) {
            if (material.hasFluid()) {
                event.register(new DynamicFluidContainerModel.Colors(), GTReactorFluids.bucket(material).get());
            }
        }
        for (GTReactorSolid solid : GTReactorSolid.values()) {
            int tint = 0xFF000000 | solid.color();
            event.register((stack, tintIndex) -> tintIndex == 1 ? tint : 0xFFFFFFFF,
                    GTReactorItems.item(solid).get());
        }
    }

    private record TintedFluidExtensions(int color) implements IClientFluidTypeExtensions {

        private static final ResourceLocation STILL_TEXTURE =
                ResourceLocation.fromNamespaceAndPath("mekanism", "liquid/liquid");

        private static final ResourceLocation FLOWING_TEXTURE =
                ResourceLocation.fromNamespaceAndPath("mekanism", "liquid/liquid_flow");

        @Override
        public ResourceLocation getStillTexture() {
            return STILL_TEXTURE;
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return FLOWING_TEXTURE;
        }

        @Override
        public int getTintColor() {
            return 0xFF000000 | color;
        }
    }
}
