package net.fodoth.skina.goldentweaks.content.craftsmen;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GTCraftsmenItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredHolder<Item, Item> LDPE_PLATE =
            ITEMS.register("ldpe_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LDPE_ROD =
            ITEMS.register("ldpe_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LDPE_BALL =
            ITEMS.register("ldpe_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LDPE_NUGGET =
            ITEMS.register("ldpe_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LLDPE_PLATE =
            ITEMS.register("lldpe_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LLDPE_ROD =
            ITEMS.register("lldpe_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LLDPE_BALL =
            ITEMS.register("lldpe_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LLDPE_NUGGET =
            ITEMS.register("lldpe_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> UHMWPE_PLATE =
            ITEMS.register("uhmwpe_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> UHMWPE_ROD =
            ITEMS.register("uhmwpe_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> UHMWPE_FIBER =
            ITEMS.register("uhmwpe_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> UHMWPE_BALL =
            ITEMS.register("uhmwpe_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> UHMWPE_NUGGET =
            ITEMS.register("uhmwpe_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> IPP_PLATE =
            ITEMS.register("ipp_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> IPP_ROD =
            ITEMS.register("ipp_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> IPP_BALL =
            ITEMS.register("ipp_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> IPP_NUGGET =
            ITEMS.register("ipp_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPP_PLATE =
            ITEMS.register("spp_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPP_ROD =
            ITEMS.register("spp_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPP_FIBER =
            ITEMS.register("spp_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPP_BALL =
            ITEMS.register("spp_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPP_NUGGET =
            ITEMS.register("spp_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RCPP_PLATE =
            ITEMS.register("rcpp_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RCPP_ROD =
            ITEMS.register("rcpp_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RCPP_BALL =
            ITEMS.register("rcpp_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RCPP_NUGGET =
            ITEMS.register("rcpp_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> HDPE_NUGGET =
            ITEMS.register("hdpe_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVC_PLATE =
            ITEMS.register("pvc_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVC_ROD =
            ITEMS.register("pvc_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVC_FIBER =
            ITEMS.register("pvc_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVC_BALL =
            ITEMS.register("pvc_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVC_NUGGET =
            ITEMS.register("pvc_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POLYSTYRENE_PLATE =
            ITEMS.register("polystyrene_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POLYSTYRENE_ROD =
            ITEMS.register("polystyrene_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POLYSTYRENE_BALL =
            ITEMS.register("polystyrene_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POLYSTYRENE_NUGGET =
            ITEMS.register("polystyrene_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PC_PLATE =
            ITEMS.register("pc_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PC_ROD =
            ITEMS.register("pc_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PC_BALL =
            ITEMS.register("pc_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PC_NUGGET =
            ITEMS.register("pc_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PSU_PLATE =
            ITEMS.register("psu_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PSU_ROD =
            ITEMS.register("psu_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PSU_BALL =
            ITEMS.register("psu_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PSU_NUGGET =
            ITEMS.register("psu_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ABS_PLATE =
            ITEMS.register("abs_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ABS_ROD =
            ITEMS.register("abs_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ABS_BALL =
            ITEMS.register("abs_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ABS_NUGGET =
            ITEMS.register("abs_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POM_PLATE =
            ITEMS.register("pom_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POM_ROD =
            ITEMS.register("pom_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POM_BALL =
            ITEMS.register("pom_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POM_NUGGET =
            ITEMS.register("pom_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPO_PPE_PLATE =
            ITEMS.register("ppo_ppe_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPO_PPE_ROD =
            ITEMS.register("ppo_ppe_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPO_PPE_BALL =
            ITEMS.register("ppo_ppe_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPO_PPE_NUGGET =
            ITEMS.register("ppo_ppe_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPS_PLATE =
            ITEMS.register("sps_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPS_ROD =
            ITEMS.register("sps_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPS_BALL =
            ITEMS.register("sps_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPS_NUGGET =
            ITEMS.register("sps_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PHENOLIC_RESIN_PLATE =
            ITEMS.register("phenolic_resin_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PHENOLIC_RESIN_ROD =
            ITEMS.register("phenolic_resin_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PHENOLIC_RESIN_BALL =
            ITEMS.register("phenolic_resin_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PHENOLIC_RESIN_NUGGET =
            ITEMS.register("phenolic_resin_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPOXY_RESIN_PLATE =
            ITEMS.register("epoxy_resin_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPOXY_RESIN_ROD =
            ITEMS.register("epoxy_resin_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPOXY_RESIN_BALL =
            ITEMS.register("epoxy_resin_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPOXY_RESIN_NUGGET =
            ITEMS.register("epoxy_resin_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PU_PLATE =
            ITEMS.register("pu_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PU_ROD =
            ITEMS.register("pu_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PU_FIBER =
            ITEMS.register("pu_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PU_BALL =
            ITEMS.register("pu_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PU_NUGGET =
            ITEMS.register("pu_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PC_ABS_ALLOY_PLATE =
            ITEMS.register("pc_abs_alloy_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PC_ABS_ALLOY_ROD =
            ITEMS.register("pc_abs_alloy_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PC_ABS_ALLOY_BALL =
            ITEMS.register("pc_abs_alloy_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PC_ABS_ALLOY_NUGGET =
            ITEMS.register("pc_abs_alloy_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> HIPS_PLATE =
            ITEMS.register("hips_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> HIPS_ROD =
            ITEMS.register("hips_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> HIPS_BALL =
            ITEMS.register("hips_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> HIPS_NUGGET =
            ITEMS.register("hips_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPE_HIPS_ALLOY_PLATE =
            ITEMS.register("ppe_hips_alloy_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPE_HIPS_ALLOY_ROD =
            ITEMS.register("ppe_hips_alloy_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPE_HIPS_ALLOY_BALL =
            ITEMS.register("ppe_hips_alloy_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPE_HIPS_ALLOY_NUGGET =
            ITEMS.register("ppe_hips_alloy_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PTFE_PLATE =
            ITEMS.register("ptfe_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PTFE_ROD =
            ITEMS.register("ptfe_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PTFE_BALL =
            ITEMS.register("ptfe_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PTFE_NUGGET =
            ITEMS.register("ptfe_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NYLON_66_PLATE =
            ITEMS.register("nylon_66_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NYLON_66_ROD =
            ITEMS.register("nylon_66_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NYLON_66_FIBER =
            ITEMS.register("nylon_66_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NYLON_66_BALL =
            ITEMS.register("nylon_66_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NYLON_66_NUGGET =
            ITEMS.register("nylon_66_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NYLON_6_PLATE =
            ITEMS.register("nylon_6_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NYLON_6_ROD =
            ITEMS.register("nylon_6_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NYLON_6_FIBER =
            ITEMS.register("nylon_6_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NYLON_6_BALL =
            ITEMS.register("nylon_6_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NYLON_6_NUGGET =
            ITEMS.register("nylon_6_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PEEK_PLATE =
            ITEMS.register("peek_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PEEK_ROD =
            ITEMS.register("peek_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PEEK_BALL =
            ITEMS.register("peek_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PEEK_NUGGET =
            ITEMS.register("peek_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPESK_PLATE =
            ITEMS.register("ppesk_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPESK_ROD =
            ITEMS.register("ppesk_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPESK_BALL =
            ITEMS.register("ppesk_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPESK_NUGGET =
            ITEMS.register("ppesk_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PET_PLATE =
            ITEMS.register("pet_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PET_ROD =
            ITEMS.register("pet_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PET_FIBER =
            ITEMS.register("pet_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PET_BALL =
            ITEMS.register("pet_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PET_NUGGET =
            ITEMS.register("pet_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBT_PLATE =
            ITEMS.register("pbt_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBT_ROD =
            ITEMS.register("pbt_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBT_BALL =
            ITEMS.register("pbt_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBT_NUGGET =
            ITEMS.register("pbt_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPS_PLATE =
            ITEMS.register("pps_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPS_ROD =
            ITEMS.register("pps_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPS_BALL =
            ITEMS.register("pps_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPS_NUGGET =
            ITEMS.register("pps_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVAC_PLATE =
            ITEMS.register("pvac_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVAC_ROD =
            ITEMS.register("pvac_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVAC_BALL =
            ITEMS.register("pvac_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVAC_NUGGET =
            ITEMS.register("pvac_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVA_PLATE =
            ITEMS.register("pva_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVA_ROD =
            ITEMS.register("pva_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVA_FIBER =
            ITEMS.register("pva_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVA_BALL =
            ITEMS.register("pva_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PVA_NUGGET =
            ITEMS.register("pva_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PAN_PLATE =
            ITEMS.register("pan_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PAN_ROD =
            ITEMS.register("pan_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PAN_FIBER =
            ITEMS.register("pan_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PAN_BALL =
            ITEMS.register("pan_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PAN_NUGGET =
            ITEMS.register("pan_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> KEVLAR_PLATE =
            ITEMS.register("kevlar_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> KEVLAR_ROD =
            ITEMS.register("kevlar_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> KEVLAR_FIBER =
            ITEMS.register("kevlar_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> KEVLAR_BALL =
            ITEMS.register("kevlar_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> KEVLAR_NUGGET =
            ITEMS.register("kevlar_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PI_PLATE =
            ITEMS.register("pi_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PI_ROD =
            ITEMS.register("pi_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PI_BALL =
            ITEMS.register("pi_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PI_NUGGET =
            ITEMS.register("pi_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBO_PLATE =
            ITEMS.register("pbo_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBO_ROD =
            ITEMS.register("pbo_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBO_BALL =
            ITEMS.register("pbo_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBO_NUGGET =
            ITEMS.register("pbo_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PLA_PLATE =
            ITEMS.register("pla_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PLA_ROD =
            ITEMS.register("pla_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PLA_BALL =
            ITEMS.register("pla_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PLA_NUGGET =
            ITEMS.register("pla_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POLYALGINIC_ACID_PLATE =
            ITEMS.register("polyalginic_acid_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POLYALGINIC_ACID_ROD =
            ITEMS.register("polyalginic_acid_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POLYALGINIC_ACID_BALL =
            ITEMS.register("polyalginic_acid_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POLYALGINIC_ACID_NUGGET =
            ITEMS.register("polyalginic_acid_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTADIENE_RUBBER_BLACK_PLATE =
            ITEMS.register("butadiene_rubber_black_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTADIENE_RUBBER_BLACK_ROD =
            ITEMS.register("butadiene_rubber_black_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTADIENE_RUBBER_BLACK_BALL =
            ITEMS.register("butadiene_rubber_black_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTADIENE_RUBBER_BLACK_NUGGET =
            ITEMS.register("butadiene_rubber_black_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTADIENE_RUBBER_WHITE_PLATE =
            ITEMS.register("butadiene_rubber_white_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTADIENE_RUBBER_WHITE_ROD =
            ITEMS.register("butadiene_rubber_white_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTADIENE_RUBBER_WHITE_BALL =
            ITEMS.register("butadiene_rubber_white_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTADIENE_RUBBER_WHITE_NUGGET =
            ITEMS.register("butadiene_rubber_white_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NATURAL_RUBBER_BLACK_PLATE =
            ITEMS.register("natural_rubber_black_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NATURAL_RUBBER_BLACK_ROD =
            ITEMS.register("natural_rubber_black_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NATURAL_RUBBER_BLACK_BALL =
            ITEMS.register("natural_rubber_black_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NATURAL_RUBBER_BLACK_NUGGET =
            ITEMS.register("natural_rubber_black_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NATURAL_RUBBER_WHITE_PLATE =
            ITEMS.register("natural_rubber_white_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NATURAL_RUBBER_WHITE_ROD =
            ITEMS.register("natural_rubber_white_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NATURAL_RUBBER_WHITE_BALL =
            ITEMS.register("natural_rubber_white_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NATURAL_RUBBER_WHITE_NUGGET =
            ITEMS.register("natural_rubber_white_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ISOPRENE_RUBBER_BLACK_PLATE =
            ITEMS.register("isoprene_rubber_black_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ISOPRENE_RUBBER_BLACK_ROD =
            ITEMS.register("isoprene_rubber_black_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ISOPRENE_RUBBER_BLACK_BALL =
            ITEMS.register("isoprene_rubber_black_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ISOPRENE_RUBBER_BLACK_NUGGET =
            ITEMS.register("isoprene_rubber_black_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ISOPRENE_RUBBER_WHITE_PLATE =
            ITEMS.register("isoprene_rubber_white_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ISOPRENE_RUBBER_WHITE_ROD =
            ITEMS.register("isoprene_rubber_white_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ISOPRENE_RUBBER_WHITE_BALL =
            ITEMS.register("isoprene_rubber_white_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ISOPRENE_RUBBER_WHITE_NUGGET =
            ITEMS.register("isoprene_rubber_white_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTYL_RUBBER_BLACK_PLATE =
            ITEMS.register("butyl_rubber_black_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTYL_RUBBER_BLACK_ROD =
            ITEMS.register("butyl_rubber_black_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTYL_RUBBER_BLACK_BALL =
            ITEMS.register("butyl_rubber_black_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTYL_RUBBER_BLACK_NUGGET =
            ITEMS.register("butyl_rubber_black_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTYL_RUBBER_WHITE_PLATE =
            ITEMS.register("butyl_rubber_white_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTYL_RUBBER_WHITE_ROD =
            ITEMS.register("butyl_rubber_white_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTYL_RUBBER_WHITE_BALL =
            ITEMS.register("butyl_rubber_white_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BUTYL_RUBBER_WHITE_NUGGET =
            ITEMS.register("butyl_rubber_white_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SOLUTION_SBR_BLACK_PLATE =
            ITEMS.register("solution_sbr_black_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SOLUTION_SBR_BLACK_ROD =
            ITEMS.register("solution_sbr_black_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SOLUTION_SBR_BLACK_BALL =
            ITEMS.register("solution_sbr_black_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SOLUTION_SBR_BLACK_NUGGET =
            ITEMS.register("solution_sbr_black_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SOLUTION_SBR_WHITE_PLATE =
            ITEMS.register("solution_sbr_white_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SOLUTION_SBR_WHITE_ROD =
            ITEMS.register("solution_sbr_white_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SOLUTION_SBR_WHITE_BALL =
            ITEMS.register("solution_sbr_white_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SOLUTION_SBR_WHITE_NUGGET =
            ITEMS.register("solution_sbr_white_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPR_BLACK_PLATE =
            ITEMS.register("epr_black_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPR_BLACK_ROD =
            ITEMS.register("epr_black_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPR_BLACK_BALL =
            ITEMS.register("epr_black_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPR_BLACK_NUGGET =
            ITEMS.register("epr_black_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPR_WHITE_PLATE =
            ITEMS.register("epr_white_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPR_WHITE_ROD =
            ITEMS.register("epr_white_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPR_WHITE_BALL =
            ITEMS.register("epr_white_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPR_WHITE_NUGGET =
            ITEMS.register("epr_white_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPDM_BLACK_PLATE =
            ITEMS.register("epdm_black_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPDM_BLACK_ROD =
            ITEMS.register("epdm_black_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPDM_BLACK_BALL =
            ITEMS.register("epdm_black_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPDM_BLACK_NUGGET =
            ITEMS.register("epdm_black_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPDM_WHITE_PLATE =
            ITEMS.register("epdm_white_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPDM_WHITE_ROD =
            ITEMS.register("epdm_white_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPDM_WHITE_BALL =
            ITEMS.register("epdm_white_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPDM_WHITE_NUGGET =
            ITEMS.register("epdm_white_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EMULSION_SBR_BLACK_PLATE =
            ITEMS.register("emulsion_sbr_black_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EMULSION_SBR_BLACK_ROD =
            ITEMS.register("emulsion_sbr_black_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EMULSION_SBR_BLACK_BALL =
            ITEMS.register("emulsion_sbr_black_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EMULSION_SBR_BLACK_NUGGET =
            ITEMS.register("emulsion_sbr_black_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EMULSION_SBR_WHITE_PLATE =
            ITEMS.register("emulsion_sbr_white_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EMULSION_SBR_WHITE_ROD =
            ITEMS.register("emulsion_sbr_white_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EMULSION_SBR_WHITE_BALL =
            ITEMS.register("emulsion_sbr_white_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EMULSION_SBR_WHITE_NUGGET =
            ITEMS.register("emulsion_sbr_white_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NBR_BLACK_PLATE =
            ITEMS.register("nbr_black_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NBR_BLACK_ROD =
            ITEMS.register("nbr_black_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NBR_BLACK_BALL =
            ITEMS.register("nbr_black_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NBR_BLACK_NUGGET =
            ITEMS.register("nbr_black_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NBR_WHITE_PLATE =
            ITEMS.register("nbr_white_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NBR_WHITE_ROD =
            ITEMS.register("nbr_white_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NBR_WHITE_BALL =
            ITEMS.register("nbr_white_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NBR_WHITE_NUGGET =
            ITEMS.register("nbr_white_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SILICONE_RUBBER_PLATE =
            ITEMS.register("silicone_rubber_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SILICONE_RUBBER_ROD =
            ITEMS.register("silicone_rubber_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SILICONE_RUBBER_BALL =
            ITEMS.register("silicone_rubber_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SILICONE_RUBBER_NUGGET =
            ITEMS.register("silicone_rubber_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PMS_PLATE =
            ITEMS.register("pms_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PMS_ROD =
            ITEMS.register("pms_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PMS_BALL =
            ITEMS.register("pms_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PMS_NUGGET =
            ITEMS.register("pms_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PHOST_PLATE =
            ITEMS.register("phost_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PHOST_ROD =
            ITEMS.register("phost_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PHOST_BALL =
            ITEMS.register("phost_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PHOST_NUGGET =
            ITEMS.register("phost_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBI_PLATE =
            ITEMS.register("pbi_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBI_ROD =
            ITEMS.register("pbi_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBI_BALL =
            ITEMS.register("pbi_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBI_NUGGET =
            ITEMS.register("pbi_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBO_FIBER =
            ITEMS.register("pbo_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PI_FIBER =
            ITEMS.register("pi_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PPS_FIBER =
            ITEMS.register("pps_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PBI_FIBER =
            ITEMS.register("pbi_fiber", () -> new Item(new Item.Properties()));

    private GTCraftsmenItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
