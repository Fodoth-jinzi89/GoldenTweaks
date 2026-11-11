package net.fodoth.skina.goldentweaks.content.craftsmen.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.craftsmen.GTCraftsmenItems;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTCraftsmenClientExtensions {

    private static final int LDPE_TINT = 0xFFE0E0E0;
    private static final int LLDPE_TINT = 0xFFF4E8D1;
    private static final int UHMWPE_TINT = 0xFFFDE10B;
    private static final int IPP_TINT = 0xFFBFAAF8;
    private static final int SPP_TINT = 0xFFAB93EE;
    private static final int RCPP_TINT = 0xFF7959D1;
    private static final int HDPE_TINT = 0xFFE0FFFE;
    private static final int COIL_TINT = 0xFFFFDD3F;

    private GTCraftsmenClientExtensions() {
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        registerSimple(event, GTCraftsmenItems.LDPE_PLATE.get(), LDPE_TINT);
        registerSimple(event, GTCraftsmenItems.LDPE_ROD.get(), LDPE_TINT);
        registerSimple(event, GTCraftsmenItems.LDPE_BALL.get(), LDPE_TINT);
        registerSimple(event, GTCraftsmenItems.LDPE_NUGGET.get(), LDPE_TINT);
        registerFiber(event, GTCraftsmenItems.LDPE_FIBER.get(), LDPE_TINT);

        registerSimple(event, GTCraftsmenItems.LLDPE_PLATE.get(), LLDPE_TINT);
        registerSimple(event, GTCraftsmenItems.LLDPE_ROD.get(), LLDPE_TINT);
        registerSimple(event, GTCraftsmenItems.LLDPE_BALL.get(), LLDPE_TINT);
        registerSimple(event, GTCraftsmenItems.LLDPE_NUGGET.get(), LLDPE_TINT);
        registerFiber(event, GTCraftsmenItems.LLDPE_FIBER.get(), LLDPE_TINT);

        registerSimple(event, GTCraftsmenItems.UHMWPE_PLATE.get(), UHMWPE_TINT);
        registerSimple(event, GTCraftsmenItems.UHMWPE_ROD.get(), UHMWPE_TINT);
        registerSimple(event, GTCraftsmenItems.UHMWPE_BALL.get(), UHMWPE_TINT);
        registerSimple(event, GTCraftsmenItems.UHMWPE_NUGGET.get(), UHMWPE_TINT);
        registerFiber(event, GTCraftsmenItems.UHMWPE_FIBER.get(), UHMWPE_TINT);

        registerSimple(event, GTCraftsmenItems.IPP_PLATE.get(), IPP_TINT);
        registerSimple(event, GTCraftsmenItems.IPP_ROD.get(), IPP_TINT);
        registerSimple(event, GTCraftsmenItems.IPP_BALL.get(), IPP_TINT);
        registerSimple(event, GTCraftsmenItems.IPP_NUGGET.get(), IPP_TINT);
        registerFiber(event, GTCraftsmenItems.IPP_FIBER.get(), IPP_TINT);

        registerSimple(event, GTCraftsmenItems.SPP_PLATE.get(), SPP_TINT);
        registerSimple(event, GTCraftsmenItems.SPP_ROD.get(), SPP_TINT);
        registerSimple(event, GTCraftsmenItems.SPP_BALL.get(), SPP_TINT);
        registerSimple(event, GTCraftsmenItems.SPP_NUGGET.get(), SPP_TINT);
        registerFiber(event, GTCraftsmenItems.SPP_FIBER.get(), SPP_TINT);

        registerSimple(event, GTCraftsmenItems.RCPP_PLATE.get(), RCPP_TINT);
        registerSimple(event, GTCraftsmenItems.RCPP_ROD.get(), RCPP_TINT);
        registerSimple(event, GTCraftsmenItems.RCPP_BALL.get(), RCPP_TINT);
        registerSimple(event, GTCraftsmenItems.RCPP_NUGGET.get(), RCPP_TINT);
        registerFiber(event, GTCraftsmenItems.RCPP_FIBER.get(), RCPP_TINT);

        registerSimple(event, GTCraftsmenItems.HDPE_NUGGET.get(), HDPE_TINT);
        registerFiber(event, GTCraftsmenItems.HDPE_FIBER.get(), HDPE_TINT);
    }

    private static void registerSimple(RegisterColorHandlersEvent.Item event, Item item, int tint) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? tint : 0xFFFFFFFF, item);
    }

    private static void registerFiber(RegisterColorHandlersEvent.Item event, Item item, int tint) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? COIL_TINT
                : tintIndex == 1 ? tint : 0xFFFFFFFF, item);
    }
}
