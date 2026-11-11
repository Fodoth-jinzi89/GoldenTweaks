package net.fodoth.skina.goldentweaks.content.reactors;

public enum GTReactorMaterial {

    BENZENE("benzene", 0x111111),
    PHENOL("phenol", 0x281605),
    IODINE("iodine", 0xA016A2),
    BROMINE("bromine", 0xA14507),
    P_DIBROMOBENZENE("p_dibromobenzene", 0x684833),
    IODOBENZENE("iodobenzene", 0xE383F8),
    BUTANE("butane", 0xF17E32),
    BUTYLLITHIUM("butyllithium", 0xF5EAE3),
    WINE("wine", 0xB2B2B2),
    METHANOL("methanol", 0x927958),
    ACETIC_ACID("acetic_acid", 0xF2D49F),
    BUTANEDIOL("butanediol", 0xF2B79F),
    ETHYLENE_GLYCOL("ethylene_glycol", 0x7FDDD1),
    TETRAHYDROFURAN("tetrahydrofuran", 0xC1ECCD),
    DIOXANE("dioxane", 0xC1CCEC),
    ETHANOL("ethanol", 0x9DEDE3, false),
    METHANE("methane", 0x8CA955, false),
    IODOMETHANE("iodomethane", 0x8038C3),
    CRACKED_LIGHT_OIL("cracked_light_oil", 0xF2B26D),
    CRACKED_HEAVY_OIL("cracked_heavy_oil", 0xC71419),
    BENT_MOLECULE_3_DIBROMO_DIOL("bent_molecule_3_dibromo_diol", 0x574C44),
    BENT_MOLECULE_7_DIBROMO_DIOL("bent_molecule_7_dibromo_diol", 0xFCF09F),
    P_BROMOBIPHENOL_METHANOL_SOLUTION("p_bromobiphenol_methanol_solution", 0xC7B240, true, false),
    BENT_MOLECULE_2_METHOXY_METHANOL_SOLUTION("bent_molecule_2_methoxy_methanol_solution", 0x250202, true, false),
    ETHYL_ACETATE("ethyl_acetate", 0xBA9CDB),
    BENT_MOLECULE_7_DIBROMO_REACTION_SOLUTION("bent_molecule_7_dibromo_reaction_solution", 0xF1F4A8, true, false),
    SODIUM_ACETATE("sodium_acetate", 0xAB9DB4),
    TETRAHYDROFURAN_WATER_SOLUTION("tetrahydrofuran_water_solution", 0x79F9FE, true, false),
    ETHYLENE_OXIDE("ethylene_oxide", 0x79FE88),
    POTASSIUM("potassium", 0xACACAC),
    ROCK_SALT_WATER("rock_salt_water", 0xC6DCC9),
    POTASSIUM_HYDROXIDE("potassium_hydroxide", 0xDAE0E2),
    SODIUM_HYDROXIDE("sodium_hydroxide", 0xDAE8EE),
    POTASSIUM_ACETATE("potassium_acetate", 0xB49DB4),
    BORIC_ACID("boric_acid", 0x890CB8),
    PHOSPHORIC_ACID("phosphoric_acid", 0xD2DD1D),
    FLUOROBORIC_ACID("fluoroboric_acid", 0x3C4B0F),
    BROMOBUTANE("bromobutane", 0x814913),
    BIS_PINACOLATO_DIBORON("bis_pinacolato_diboron", 0xB9B5CA),
    TETRABUTYLAMMONIUM_BROMIDE("tetrabutylammonium_bromide", 0xDBA5DC),
    NOBLE_GAS("noble_gas", 0xEAF0DB),
    NITROGEN("nitrogen", 0xFF849E, false),
    AMMONIA("ammonia", 0xC4C2C2),
    NITRIC_ACID("nitric_acid", 0xF7FC64),
    AQUA_REGIA("aqua_regia", 0xFCC864),
    CONCENTRATED_PLATINUM("concentrated_platinum", 0xE2E2C2, true, true),
    CONCENTRATED_PALLADIUM("concentrated_palladium", 0xAEAEA2, true, true),
    CONCENTRATED_IRIDIUM_OSMIUM("concentrated_iridium_osmium", 0x279CC3, true, true),
    CONCENTRATED_IRIDIUM("concentrated_iridium", 0xC6F3EF, true, true),
    CONCENTRATED_PALLADIUM_NIOBIUM("concentrated_palladium_niobium", 0x8E9AA6),
    CONCENTRATED_NIOBIUM("concentrated_niobium", 0xB5B9C1),
    CONCENTRATED_OSMIUM("concentrated_osmium", 0xA8D8E8),
    STANNOUS_CHLORIDE("stannous_chloride", 0xD0D7D2),
    TETRACHLOROSTANNATE_II_ACID("tetrachlorostannate_ii_acid", 0xC3C8C5),
    CARBON_DIOXIDE("carbon_dioxide", 0x868887),
    ARGON("argon", 0xD958D7),
    GASEOUS_CRYSTAL("gaseous_crystal", 0xFFFFFF, false, true, "goldentweaks:chemical/gaseous_crystal"),
    MIDNIGHT_GAS("midnight_gas", 0xFFFFFF, false, true, "goldentweaks:chemical/midnight_gas"),
    DRAGONROT_BIOGAS("dragonrot_biogas", 0xFFFFFF, false, true, "goldentweaks:chemical/dragonrot_biogas"),
    LDPE("ldpe", 0xE0E0E0),
    HDPE("hdpe", 0xE0FFFE),
    TRIETHYLALUMINIUM("triethylaluminium", 0xB59F51),
    TITANIUM_TETRACHLORIDE("titanium_tetrachloride", 0x747474, false),
    ZIEGLER_NATTA_CATALYST("ziegler_natta_catalyst", 0xAD179D),
    BUTANOL("butanol", 0xF5B6B6),
    LLDPE("lldpe", 0xF4E8D1),
    BUTENE("butene", 0xC78362),
    UHMWPE("uhmwpe", 0xFDE10B),
    CRACKED_REFINED_OIL("cracked_refined_oil", 0xFD9C0B),
    PROPYLENE("propylene", 0xE9B1EB),
    HEXANE("hexane", 0xCCB1EB),
    IPP("ipp", 0xBFAAF8),
    SPP("spp", 0xAB93EE),
    RCPP("rcpp", 0x7959D1),
    COAL_TAR("coal_tar", 0x090909),
    CRACKED_COAL_TAR("cracked_coal_tar", 0x393939),
    CYCLOPENTADIENE("cyclopentadiene", 0x2AA9DE),
    BENZENE_HOMOLOGS("benzene_homologs", 0x020404),
    TOLUENE("toluene", 0x532506),
    P_XYLENE("p_xylene", 0x135306),
    M_XYLENE("m_xylene", 0x2A8F16),
    O_XYLENE("o_xylene", 0x5AC046),
    CONCENTRATED_ZIRCONIUM("concentrated_zirconium", 0x8B7F93),
    ZIRCONIUM_TETRACHLORIDE("zirconium_tetrachloride", 0xF9FBCB),
    ZIRCONOCENE_DICHLORIDE("zirconocene_dichloride", 0xC1C46F),
    ALUMINUM_TRICHLORIDE("aluminum_trichloride", 0xACE9D4),
    TRIMETHYLALUMINUM("trimethylaluminum", 0x9A9D9C),
    METHYLALUMINOXANE("methylaluminoxane", 0x9DB8AF);

    private final String id;
    private final int color;
    private final boolean fluid;
    private final boolean chemical;
    private final String chemicalTexture;

    GTReactorMaterial(String id, int color) {
        this(id, color, true, true, null);
    }

    GTReactorMaterial(String id, int color, boolean fluid) {
        this(id, color, fluid, true, null);
    }

    GTReactorMaterial(String id, int color, boolean fluid, boolean chemical) {
        this(id, color, fluid, chemical, null);
    }

    GTReactorMaterial(String id, int color, boolean fluid, boolean chemical, String chemicalTexture) {
        this.id = id;
        this.color = color;
        this.fluid = fluid;
        this.chemical = chemical;
        this.chemicalTexture = chemicalTexture;
    }

    public String id() {
        return id;
    }

    public int color() {
        return color;
    }

    public boolean hasFluid() {
        return fluid;
    }

    public boolean hasChemical() {
        return chemical;
    }

    public String chemicalTexture() {
        return chemicalTexture;
    }

    public String bucketId() {
        return id + "_bucket";
    }
}
