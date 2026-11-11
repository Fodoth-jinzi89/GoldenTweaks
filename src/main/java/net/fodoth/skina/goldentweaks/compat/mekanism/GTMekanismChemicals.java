package net.fodoth.skina.goldentweaks.compat.mekanism;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.registration.impl.ChemicalDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.reactors.GTReactorMaterial;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

import java.util.EnumMap;
import java.util.Map;

public final class GTMekanismChemicals {

    private static final ResourceLocation OXYGEN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mekanism", "liquid/liquid");

    public static final ChemicalDeferredRegister CHEMICALS =
            new ChemicalDeferredRegister(GoldenTweaks.MODID);

    private static final Map<GTReactorMaterial, DeferredChemical<Chemical>> CHEMICAL_BY_MATERIAL =
            new EnumMap<>(GTReactorMaterial.class);

    static {
        for (GTReactorMaterial material : GTReactorMaterial.values()) {
            if (!material.hasChemical()) {
                continue;
            }
            ResourceLocation texture = material.chemicalTexture() == null
                    ? OXYGEN_TEXTURE
                    : ResourceLocation.parse(material.chemicalTexture());
            CHEMICAL_BY_MATERIAL.put(material, CHEMICALS.register(material.id(),
                    () -> new Chemical(ChemicalBuilder.builder(texture).tint(material.color()))));
        }
    }

    private GTMekanismChemicals() {
    }

    public static void register(IEventBus eventBus) {
        CHEMICALS.register(eventBus);
    }
}
