package net.fodoth.skina.goldentweaks.content.reactors;

public enum GTReactorSolid {

    PALLADIUM_POWDER("palladium_powder", 0x2F3433),
    SODIUM_HYDRIDE("sodium_hydride", 0x515151),
    BENT_MOLECULE_3_DIBROMO_DIOL("bent_molecule_3_dibromo_diol", 0x574C44),
    BENT_MOLECULE_3_DIBROMO("bent_molecule_3_dibromo", 0xE7E7E7),
    P_BROMOBIPHENOL("p_bromobiphenol", 0xFAE89E),
    IODOBENZENE_DIACETATE("iodobenzene_diacetate", 0xE8E7E2),
    ORGANOPHOSPHINE_LIGAND("organophosphine_ligand", 0xEDD36D),
    PALLADIUM_ACETATE("palladium_acetate", 0x70310E),
    IODINE("iodine", 0x1E001E),
    TETRABUTYLAMMONIUM_BROMIDE("tetrabutylammonium_bromide", 0xDBA5DC),
    BENT_MOLECULE_2_METHOXY("bent_molecule_2_methoxy", 0xFB8007),
    BORAX("borax", 0xD2CC9C),
    BIS_PINACOLATO_DIBORON("bis_pinacolato_diboron", 0xB9B5CA),
    BENT_MOLECULE_7_DIBROMO_DIOL("bent_molecule_7_dibromo_diol", 0xFCF09F),
    BENT_MOLECULE_7_DIBROMO("bent_molecule_7_dibromo", 0xE9E8E4),
    BENT_MOLECULE_7_BIS_PINACOLATO_DIBORON("bent_molecule_7_bis_pinacolato_diboron", 0xF8F8F5),
    STANNOUS_CHLORIDE("stannous_chloride", 0xF2F2F2),
    CYCLOPARAPHENYLENE_8("cycloparaphenylene_8", 0xEFFF3E),
    ROCK_SALT("rock_salt", 0xF7B2F8),
    POTASSIUM("potassium", 0xACACAC),
    POTASSIUM_ACETATE("potassium_acetate", 0xB49DB4),
    SODIUM_ACETATE("sodium_acetate", 0xAB9DB4),
    SODIUM("sodium", 0xBAE3F4),
    POTASSIUM_HYDROXIDE("potassium_hydroxide", 0xDAE0E2),
    SODIUM_HYDROXIDE("sodium_hydroxide", 0xDAE8EE),
    PHOSPHORUS("phosphorus", 0xF21448),
    TRIPHENYLPHOSPHINE("triphenylphosphine", 0x252709),
    TETRAKIS_TRIPHENYLPHOSPHINE_PALLADIUM("tetrakis_triphenylphosphine_palladium", 0xC6ED68),
    NOBLE_GAS("noble_gas", 0xEAF0DB),
    CYCLOPARAPHENYLENE_8_HEXAMETHOXY("cycloparaphenylene_8_hexamethoxy", 0xE9E4B4),
    TIN_DIOXIDE("tin_dioxide", 0xFFF2A0),
    NANO_ACTIVATED_PARTICLES("nano_activated_particles", 0xC8C8CC),
    LDPE("ldpe", 0xE0E0E0),
    ZIEGLER_NATTA_CATALYST("ziegler_natta_catalyst", 0xAD179D),
    LLDPE("lldpe", 0xF4E8D1),
    UHMWPE("uhmwpe", 0xFDE10B),
    IPP("ipp", 0xBFAAF8),
    SPP("spp", 0xAB93EE),
    RCPP("rcpp", 0x7959D1),
    ZIRCONIUM_POWDER("zirconium_powder", 0x909086),
    ZIRCONIUM_TETRACHLORIDE("zirconium_tetrachloride", 0xF9FBCB),
    ZIRCONOCENE_DICHLORIDE("zirconocene_dichloride", 0xC1C46F),
    ALUMINUM_TRICHLORIDE("aluminum_trichloride", 0xACE9D4),
    METHYLALUMINOXANE("methylaluminoxane", 0x9DB8AF),
    METALLOCENE_CATALYST("metallocene_catalyst", 0xD6CE88),
    HDPE("hdpe", 0xE0FFFE);

    private final String id;
    private final int color;

    GTReactorSolid(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public int color() {
        return color;
    }
}
