package net.fodoth.skina.goldentweaks.content.reactors;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class GTReactorCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoldenTweaks.MODID);

    /** 红瓶：催化剂。 */
    private static final List<GTReactorSolid> RED_BOTTLE = List.of(
            GTReactorSolid.ZIEGLER_NATTA_CATALYST,
            GTReactorSolid.METALLOCENE_CATALYST,
            GTReactorSolid.METHYLALUMINOXANE,
            GTReactorSolid.ZIRCONOCENE_DICHLORIDE,
            GTReactorSolid.ZIRCONIUM_TETRACHLORIDE,
            GTReactorSolid.PALLADIUM_ACETATE,
            GTReactorSolid.TETRAKIS_TRIPHENYLPHOSPHINE_PALLADIUM,
            GTReactorSolid.ORGANOPHOSPHINE_LIGAND,
            GTReactorSolid.ALUMINUM_TRICHLORIDE,
            GTReactorSolid.DIBUTYLTIN_DILAURATE,
            GTReactorSolid.SULFURIC_ACID_ION_EXCHANGE_RESIN,
            GTReactorSolid.POTASSIUM_PERSULFATE,
            GTReactorSolid.CUPROUS_CHLORIDE);

    /** 黄瓶：光刻胶与环对苯撑（带光环）。 */
    private static final List<GTReactorSolid> YELLOW_BOTTLE = List.of(
            GTReactorSolid.G_I_LINE_PHOTORESIST,
            GTReactorSolid.ISOPRENE_CROSSLINKED_PHOTORESIST,
            GTReactorSolid.KRF_PHOTORESIST,
            GTReactorSolid.NANO_ACTIVATED_PARTICLES,
            GTReactorSolid.CYCLOPARAPHENYLENE_8);

    /** 蓝瓶：单质。 */
    private static final List<GTReactorSolid> BLUE_BOTTLE = List.of(
            GTReactorSolid.PALLADIUM_POWDER,
            GTReactorSolid.IODINE,
            GTReactorSolid.POTASSIUM,
            GTReactorSolid.SODIUM,
            GTReactorSolid.PHOSPHORUS,
            GTReactorSolid.ZIRCONIUM_POWDER,
            GTReactorSolid.CARBON,
            GTReactorSolid.CHROMIUM_POWDER);

    /** 紫瓶：聚合物塑料。 */
    private static final List<GTReactorSolid> PURPLE_BOTTLE = List.of(
            GTReactorSolid.LDPE, GTReactorSolid.LLDPE, GTReactorSolid.UHMWPE,
            GTReactorSolid.IPP, GTReactorSolid.SPP, GTReactorSolid.RCPP,
            GTReactorSolid.PVC, GTReactorSolid.POLYSTYRENE, GTReactorSolid.SPS,
            GTReactorSolid.HIPS, GTReactorSolid.ABS, GTReactorSolid.PC,
            GTReactorSolid.PC_ABS_ALLOY, GTReactorSolid.PSU, GTReactorSolid.PPO_PPE,
            GTReactorSolid.PPE_HIPS_ALLOY, GTReactorSolid.POM, GTReactorSolid.PTFE,
            GTReactorSolid.PET, GTReactorSolid.PBT, GTReactorSolid.PPS,
            GTReactorSolid.PVAC, GTReactorSolid.PVA, GTReactorSolid.PAN,
            GTReactorSolid.KEVLAR, GTReactorSolid.NYLON_66, GTReactorSolid.NYLON_6,
            GTReactorSolid.PEEK, GTReactorSolid.PPESK, GTReactorSolid.PI,
            GTReactorSolid.PBO, GTReactorSolid.PBI, GTReactorSolid.PLA,
            GTReactorSolid.POLYALGINIC_ACID, GTReactorSolid.PHENOLIC_RESIN,
            GTReactorSolid.EPOXY_RESIN, GTReactorSolid.PU,
            GTReactorSolid.PMS, GTReactorSolid.PHOST);

    /** 绿瓶：橡胶。 */
    private static final List<GTReactorSolid> GREEN_BOTTLE = List.of(
            GTReactorSolid.BUTADIENE_RUBBER_BLACK, GTReactorSolid.BUTADIENE_RUBBER_WHITE,
            GTReactorSolid.NATURAL_RUBBER_BLACK, GTReactorSolid.NATURAL_RUBBER_WHITE,
            GTReactorSolid.ISOPRENE_RUBBER_BLACK, GTReactorSolid.ISOPRENE_RUBBER_WHITE,
            GTReactorSolid.BUTYL_RUBBER_BLACK, GTReactorSolid.BUTYL_RUBBER_WHITE,
            GTReactorSolid.SOLUTION_SBR_BLACK, GTReactorSolid.SOLUTION_SBR_WHITE,
            GTReactorSolid.EPR_BLACK, GTReactorSolid.EPR_WHITE,
            GTReactorSolid.EPDM_BLACK, GTReactorSolid.EPDM_WHITE,
            GTReactorSolid.EMULSION_SBR_BLACK, GTReactorSolid.EMULSION_SBR_WHITE,
            GTReactorSolid.NBR_BLACK, GTReactorSolid.NBR_WHITE,
            GTReactorSolid.SILICONE_RUBBER);

    /** 已经按瓶色单独排布的固体，普通瓶一栏会跳过它们。 */
    private static final Set<GTReactorSolid> STYLED = styledSolids();

    private static Set<GTReactorSolid> styledSolids() {
        Set<GTReactorSolid> all = EnumSet.noneOf(GTReactorSolid.class);
        all.addAll(RED_BOTTLE);
        all.addAll(YELLOW_BOTTLE);
        all.addAll(BLUE_BOTTLE);
        all.addAll(GREEN_BOTTLE);
        all.addAll(PURPLE_BOTTLE);
        return all;
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> REACTORS_TAB =
            CREATIVE_MODE_TABS.register("reactors", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goldentweaks.reactors"))
                    .icon(() -> new ItemStack(GTReactorItems.item(GTReactorSolid.CYCLOPARAPHENYLENE_8).get()))
                    .displayItems((parameters, output) -> {
                        acceptSolids(output, RED_BOTTLE);
                        acceptSolids(output, YELLOW_BOTTLE);
                        acceptSolids(output, BLUE_BOTTLE);
                        acceptSolids(output, PURPLE_BOTTLE);
                        acceptSolids(output, GREEN_BOTTLE);
                        for (GTReactorSolid solid : GTReactorSolid.values()) {
                            if (!STYLED.contains(solid)) {
                                output.accept(GTReactorItems.item(solid).get());
                            }
                        }
                        for (GTReactorMaterial material : GTReactorMaterial.values()) {
                            if (material.hasFluid()) {
                                output.accept(GTReactorFluids.bucket(material).get());
                            }
                        }
                    })
                    .build());

    private static void acceptSolids(CreativeModeTab.Output output, List<GTReactorSolid> solids) {
        for (GTReactorSolid solid : solids) {
            output.accept(GTReactorItems.item(solid).get());
        }
    }

    private GTReactorCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
